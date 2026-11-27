package com.medisync.app.auth;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

/** E-mail ownership check for sign-up */
public final class EmailVerifier {
    private EmailVerifier() { }

    public enum Failure { NETWORK, NOT_VERIFIED, EMAIL_IN_USE, TOO_MANY, SIGN_IN_DISABLED, NOT_CONFIGURED, OTHER }

    /** failure == null means success. */
    public interface Callback { void onResult(Failure failure); }

    /** Creates the Firebase user and sends the verification e-mail. */
    public static void start(Context ctx, String email, String password, Callback cb) {
        if (FirebaseApp.getApps(ctx).isEmpty()) { cb.onResult(Failure.NOT_CONFIGURED); return; }
        final FirebaseAuth fa = FirebaseAuth.getInstance();
        fa.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) { sendLink(task.getResult().getUser(), cb); return; }
            Exception e = task.getException();
            if (e instanceof FirebaseAuthUserCollisionException) { resume(fa, email, password, cb); return; }
            cb.onResult(map(e));
        });
    }

    private static void resume(FirebaseAuth fa, String email, String password, Callback cb) {
        fa.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Exception e = task.getException();
                boolean taken = e instanceof FirebaseAuthInvalidCredentialsException || e instanceof FirebaseAuthInvalidUserException;
                cb.onResult(taken ? Failure.EMAIL_IN_USE : map(e));
                return;
            }
            FirebaseUser u = task.getResult().getUser();
            if (u != null && u.isEmailVerified()) { cb.onResult(null); return; } // already confirmed earlier
            sendLink(u, cb);
        });
    }

    private static void sendLink(FirebaseUser u, Callback cb) {
        if (u == null) { cb.onResult(Failure.OTHER); return; }
        u.sendEmailVerification().addOnCompleteListener(t -> cb.onResult(t.isSuccessful() ? null : map(t.getException())));
    }

    /** Sends the verification e-mail again. */
    public static void resend(Context ctx, Callback cb) {
        if (FirebaseApp.getApps(ctx).isEmpty()) { cb.onResult(Failure.NOT_CONFIGURED); return; }
        sendLink(FirebaseAuth.getInstance().getCurrentUser(), cb);
    }

    /** Asks Firebase whether the user has tapped the link yet. */
    public static void check(Context ctx, Callback cb) {
        if (FirebaseApp.getApps(ctx).isEmpty()) { cb.onResult(Failure.NOT_CONFIGURED); return; }
        final FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) { cb.onResult(Failure.OTHER); return; }
        u.reload().addOnCompleteListener(t -> {
            if (!t.isSuccessful()) { cb.onResult(map(t.getException())); return; }
            cb.onResult(u.isEmailVerified() ? null : Failure.NOT_VERIFIED);
        });
    }

    /** Verification finished. */
    public static void finish(Context ctx) {
        if (FirebaseApp.getApps(ctx).isEmpty()) return;
        FirebaseAuth.getInstance().signOut();
    }

    public static void cancel(Context ctx) {
        if (FirebaseApp.getApps(ctx).isEmpty()) return;
        final FirebaseAuth fa = FirebaseAuth.getInstance();
        FirebaseUser u = fa.getCurrentUser();
        if (u != null && !u.isEmailVerified()) {
            u.delete().addOnCompleteListener(t -> fa.signOut());
        } else {
            fa.signOut();
        }
    }

    private static Failure map(Exception e) {
        if (e instanceof FirebaseNetworkException) return Failure.NETWORK;
        if (e instanceof FirebaseTooManyRequestsException) return Failure.TOO_MANY;
        if (e instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) e).getErrorCode();
            String msg = String.valueOf(e.getMessage());
            if ("ERROR_OPERATION_NOT_ALLOWED".equals(code) || msg.contains("OPERATION_NOT_ALLOWED")
                    || msg.contains("CONFIGURATION_NOT_FOUND")) return Failure.SIGN_IN_DISABLED;
        }
        return Failure.OTHER;
    }

    public static String describe(Failure f) {
        switch (f) {
            case NETWORK: return "Network problem. Check your connection and try again.";
            case NOT_VERIFIED: return "Not verified yet. Open the email we sent, tap the link, then come back and try again.";
            case EMAIL_IN_USE: return "This email is already in use with a different password or sign-in method. Try logging in instead.";
            case TOO_MANY: return "Too many attempts. Please wait a few minutes and try again.";
            case SIGN_IN_DISABLED: return "Email verification isn't switched on in Firebase yet. Enable Email/Password under Authentication > Sign-in method.";
            case NOT_CONFIGURED: return "Firebase is not set up in this build, so email verification is unavailable.";
            default: return "Something went wrong. Please try again.";
        }
    }
}