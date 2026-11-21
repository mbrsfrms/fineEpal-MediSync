package com.medisync.app.manager;

import com.medisync.app.model.User;

import java.util.Locale;

/**
 * MediSync accounts: email/password sign-up and login, and Google sign-in through Firebase.
 * Both kinds live in the same users table; a Google account is recognised by its Firebase UID.
 *
 * <p>Google part: the Firebase UID is the only key used to recognise a returning user. This class never creates a second
 * account for a UID and never deletes anything.
 * Callers must pass an identity that came from a completed Firebase sign-in, never an email read from the device.
 */
public class AuthManager {

    /** Identity taken from a signed-in FirebaseUser. */
    public static final class GoogleIdentity {
        public final String uid;
        public final String email;
        public final boolean emailVerified;
        public final String displayName;
        public final String photoUrl;

        public GoogleIdentity(String uid, String email, boolean emailVerified, String displayName, String photoUrl) {
            this.uid = uid;
            this.email = email;
            this.emailVerified = emailVerified;
            this.displayName = displayName;
            this.photoUrl = photoUrl;
        }
    }

    public enum Outcome {
        SUCCESS,
        INVALID_IDENTITY,
        EMAIL_NOT_VERIFIED,
        EMAIL_CONFLICT,
        STORAGE_ERROR,
        // email/password
        INVALID_NAME,
        INVALID_EMAIL,
        EMAIL_ALREADY_REGISTERED,
        EMAIL_NOT_REGISTERED,
        WEAK_PASSWORD,
        PASSWORD_MISMATCH,
        EMPTY_PASSWORD,
        WRONG_PASSWORD,
        /** The account exists but has no password because it signs in with Google. */
        GOOGLE_ACCOUNT
    }

    public static final class Result {
        public final Outcome outcome;
        public final User user;
        /** True when this call created the MediSync account (first Google sign-in). */
        public final boolean newAccount;
        /** True when an earlier local account with the same verified email was attached to this Google account. */
        public final boolean linkedExisting;

        Result(Outcome outcome, User user, boolean newAccount, boolean linkedExisting) {
            this.outcome = outcome;
            this.user = user;
            this.newAccount = newAccount;
            this.linkedExisting = linkedExisting;
        }

        static Result fail(Outcome o) { return new Result(o, null, false, false); }

        public boolean isSuccess() { return outcome == Outcome.SUCCESS; }
    }

    private final AccountStore store;

    public AuthManager(AccountStore store) {
        this.store = store;
    }

    /** Creates an email/password account after validating every field. */
    public synchronized Result register(String name, String email, String password, String confirmPassword) {
        if (Validator.nameError(name) != null) return Result.fail(Outcome.INVALID_NAME);
        if (Validator.emailError(email) != null) return Result.fail(Outcome.INVALID_EMAIL);
        String normalized = normalizeEmail(email);
        if (store.findByEmail(normalized) != null) return Result.fail(Outcome.EMAIL_ALREADY_REGISTERED);
        if (Validator.passwordError(password) != null) return Result.fail(Outcome.WEAK_PASSWORD);
        if (!password.equals(confirmPassword)) return Result.fail(Outcome.PASSWORD_MISMATCH);
        long id = store.insertWithPassword(name.trim(), normalized, PasswordHasher.hash(password));
        if (id < 0) return Result.fail(Outcome.STORAGE_ERROR);
        return new Result(Outcome.SUCCESS, new User(id, name.trim(), normalized, ""), true, false);
    }

    /** Logs in with email and password, reporting exactly why it failed. */
    public synchronized Result login(String email, String password) {
        if (Validator.emailError(email) != null) return Result.fail(Outcome.INVALID_EMAIL);
        if (password == null || password.isEmpty()) return Result.fail(Outcome.EMPTY_PASSWORD);
        AccountStore.StoredAccount acc = store.findByEmail(normalizeEmail(email));
        if (acc == null) return Result.fail(Outcome.EMAIL_NOT_REGISTERED);
        if (acc.passwordHash.isEmpty()) return Result.fail(Outcome.GOOGLE_ACCOUNT);
        if (!PasswordHasher.verify(password, acc.passwordHash)) return Result.fail(Outcome.WRONG_PASSWORD);
        if (PasswordHasher.isLegacy(acc.passwordHash)) store.updatePasswordHash(acc.id, PasswordHasher.hash(password));
        return new Result(Outcome.SUCCESS, new User(acc.id, acc.name, acc.email, acc.avatar), false, false);
    }

    /** Changes the password after confirming the current one. Google-only accounts have no password to change. */
    public synchronized Result changePassword(long userId, String email, String currentPassword, String newPassword, String confirm) {
        AccountStore.StoredAccount acc = store.findByEmail(normalizeEmail(email));
        if (acc == null || acc.id != userId) return Result.fail(Outcome.EMAIL_NOT_REGISTERED);
        if (acc.passwordHash.isEmpty()) return Result.fail(Outcome.GOOGLE_ACCOUNT);
        if (!PasswordHasher.verify(currentPassword, acc.passwordHash)) return Result.fail(Outcome.WRONG_PASSWORD);
        if (Validator.passwordError(newPassword) != null) return Result.fail(Outcome.WEAK_PASSWORD);
        if (!newPassword.equals(confirm)) return Result.fail(Outcome.PASSWORD_MISMATCH);
        store.updatePasswordHash(userId, PasswordHasher.hash(newPassword));
        return new Result(Outcome.SUCCESS, null, false, false);
    }

    /** Finds, links or creates the MediSync account for a Google identity. Safe to call repeatedly. */
    public synchronized Result signInWithGoogle(GoogleIdentity g) {
        if (g == null || g.uid == null || g.uid.trim().isEmpty() || Validator.emailError(g.email) != null) {
            return Result.fail(Outcome.INVALID_IDENTITY);
        }
        if (!g.emailVerified) return Result.fail(Outcome.EMAIL_NOT_VERIFIED);
        String uid = g.uid.trim();
        String email = normalizeEmail(g.email);

        // 1. Returning user: the Firebase UID is the stable key.
        AccountStore.StoredAccount byUid = store.findByUid(uid);
        if (byUid != null) {
            String stored = byUid.email;
            if (!email.equals(stored)) store.updateEmail(byUid.id, email); // Google email changed; ignore a clash
            String shownEmail = email.equals(stored) || emailNowStored(byUid.id, email) ? email : stored;
            return new Result(Outcome.SUCCESS, new User(byUid.id, byUid.name, shownEmail, byUid.avatar), false, false);
        }

        // 2. An email/password account with the same Google-verified email: keep its records. Its password is removed
        //    on linking (see AccountStore.linkUid) so nobody who pre-registered that email can keep access.
        AccountStore.StoredAccount byEmail = store.findByEmail(email);
        if (byEmail != null) {
            if (!byEmail.uid.isEmpty()) return Result.fail(Outcome.EMAIL_CONFLICT); // belongs to another UID
            if (!store.linkUid(byEmail.id, uid)) return Result.fail(Outcome.STORAGE_ERROR);
            return new Result(Outcome.SUCCESS, new User(byEmail.id, byEmail.name, byEmail.email, byEmail.avatar), false, true);
        }

        // 3. First sign-in: create the account.
        String name = displayNameOrFallback(g.displayName, email);
        long id = store.insert(uid, name, email, "");
        if (id < 0) return Result.fail(Outcome.STORAGE_ERROR);
        return new Result(Outcome.SUCCESS, new User(id, name, email, ""), true, false);
    }

    private boolean emailNowStored(long id, String email) {
        AccountStore.StoredAccount a = store.findByEmail(email);
        return a != null && a.id == id;
    }

    static String displayNameOrFallback(String displayName, String email) {
        String n = displayName == null ? "" : displayName.trim();
        if (n.isEmpty()) n = email.substring(0, email.indexOf('@'));
        if (n.length() > 60) n = n.substring(0, 60).trim();
        return n.isEmpty() ? "MediSync user" : n;
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.US);
    }
}