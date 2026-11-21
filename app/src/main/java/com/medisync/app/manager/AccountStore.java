package com.medisync.app.manager;

/** Storage contract for accounts. MediSyncDatabase implements it; tests use an in-memory version. */
public interface AccountStore {

    /** Account row. {@code uid} is the stable Firebase UID, or "" for an account not yet linked to Google. */
    final class StoredAccount {
        public final long id;
        public final String uid;
        public final String name;
        public final String email;
        public final String avatar;
        /** Salted password hash for email/password accounts; "" for Google-only accounts. Only AuthManager reads it. */
        public final String passwordHash;

        public StoredAccount(long id, String uid, String name, String email, String avatar) {
            this(id, uid, name, email, avatar, "");
        }

        public StoredAccount(long id, String uid, String name, String email, String avatar, String passwordHash) {
            this.id = id;
            this.uid = uid == null ? "" : uid;
            this.name = name;
            this.email = email;
            this.avatar = avatar == null ? "" : avatar;
            this.passwordHash = passwordHash == null ? "" : passwordHash;
        }
    }

    StoredAccount findByUid(String uid);

    /** Returns the account with this (lower-case) email, or null. */
    StoredAccount findByEmail(String email);

    /** Inserts a new account linked to the UID and returns its id, or -1 on failure. Email must be lower case. */
    long insert(String uid, String name, String email, String avatar);

    /** Inserts an email/password account (no Google UID) and returns its id, or -1 if the email is taken. */
    long insertWithPassword(String name, String email, String passwordHash);

    void updatePasswordHash(long id, String passwordHash);

    /** Links an existing account that has no UID yet to a Firebase UID and removes its password, so the verified Google owner takes over.
     *  Returns false if it was not updated. */
    boolean linkUid(long id, String uid);

    boolean updateEmail(long id, String email);
}
