package com.medisync.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import com.medisync.app.manager.AccountStore;
import com.medisync.app.model.User;

/** Settings, user accounts and sign-in identities. */
abstract class DbAccounts extends DbCore implements AccountStore {
    DbAccounts(Context context) {
        super(context);
    }

    public String get(String key, String fallback) {
        Cursor c = getReadableDatabase().rawQuery("SELECT v FROM settings WHERE k=?", new String[]{key});
        try {
            return c.moveToFirst() && !c.isNull(0) ? c.getString(0) : fallback;
        } finally {
            c.close();
        }
    }

    public void put(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("k", key);
        cv.put("v", value);
        getWritableDatabase().insertWithOnConflict("settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    static AccountStore.StoredAccount toStored(Cursor c) {
        return new AccountStore.StoredAccount(c.getLong(0), c.isNull(1) ? "" : c.getString(1), c.getString(2),
                c.getString(3), c.isNull(4) ? "" : c.getString(4), c.isNull(5) ? "" : c.getString(5));
    }

    @Override
    public AccountStore.StoredAccount findByUid(String uid) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id, uid, name, email, avatar, pass FROM users WHERE uid=?",
                new String[]{uid});
        try {
            return c.moveToFirst() ? toStored(c) : null;
        } finally {
            c.close();
        }
    }

    @Override
    public AccountStore.StoredAccount findByEmail(String email) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id, uid, name, email, avatar, pass FROM users WHERE email=?",
                new String[]{email});
        try {
            return c.moveToFirst() ? toStored(c) : null;
        } finally {
            c.close();
        }
    }

    @Override
    public long insert(String uid, String name, String email, String avatar) {
        ContentValues v = new ContentValues();
        v.put("uid", uid);
        v.put("name", name);
        v.put("email", email);
        v.put("pass", "");
        v.put("avatar", avatar == null ? "" : avatar);
        try {
            return getWritableDatabase().insertOrThrow("users", null, v);
        } catch (SQLiteConstraintException e) {
            return -1; // UID or email already present
        }
    }

    @Override
    public long insertWithPassword(String name, String email, String passwordHash) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email);
        v.put("pass", passwordHash);
        v.put("avatar", "");
        try {
            return getWritableDatabase().insertOrThrow("users", null, v);
        } catch (SQLiteConstraintException e) {
            return -1; // email already registered
        }
    }

    @Override
    public void updatePasswordHash(long id, String passwordHash) {
        ContentValues v = new ContentValues();
        v.put("pass", passwordHash);
        getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)});
    }

    /** True for email/password accounts. Google-only accounts have no password. */
    public boolean hasPassword(long id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT pass FROM users WHERE id=?", new String[]{String.valueOf(id)});
        try {
            return c.moveToFirst() && !c.isNull(0) && !c.getString(0).isEmpty();
        } finally {
            c.close();
        }
    }

    @Override
    public boolean linkUid(long id, String uid) {
        ContentValues v = new ContentValues();
        v.put("uid", uid);
        v.put("pass", ""); // the verified Google owner takes over; any earlier password stops working
        try {
            return getWritableDatabase().update("users", v, "id=? AND (uid IS NULL OR uid='')",
                    new String[]{String.valueOf(id)}) > 0;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    @Override
    public boolean updateEmail(long id, String email) {
        ContentValues v = new ContentValues();
        v.put("email", email);
        try {
            return getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)}) > 0;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public User user(long id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id, name, email, avatar FROM users WHERE id=?",
                new String[]{String.valueOf(id)});
        try {
            return c.moveToFirst() ? toUser(c) : null;
        } finally {
            c.close();
        }
    }

    /** Updates the display name only. The email always comes from the verified Google account. */
    public boolean updateUserName(long id, String name) {
        ContentValues v = new ContentValues();
        v.put("name", name.trim());
        return getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public void setUserAvatar(long id, String avatar) {
        ContentValues v = new ContentValues();
        v.put("avatar", avatar == null ? "" : avatar);
        getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)});
    }

    static User toUser(Cursor c) {
        return new User(lng(c, "id"), str(c, "name"), str(c, "email"), str(c, "avatar"));
    }
}
