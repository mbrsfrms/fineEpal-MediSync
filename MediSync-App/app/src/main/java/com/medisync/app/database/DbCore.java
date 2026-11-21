package com.medisync.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Schema creation, upgrades and cursor helpers shared by the database layers. */
abstract class DbCore extends SQLiteOpenHelper {
    DbCore(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    static final String DB_NAME = "medisync_app.db";

    static final int DB_VERSION = 5;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, pass TEXT NOT NULL DEFAULT '', avatar TEXT DEFAULT '', uid TEXT)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS ux_users_uid ON users(uid) WHERE uid IS NOT NULL");
        db.execSQL("CREATE TABLE patients(id INTEGER PRIMARY KEY AUTOINCREMENT, owner INTEGER NOT NULL, name TEXT NOT NULL, cat TEXT, rel TEXT, dob TEXT, avatar TEXT DEFAULT '', allergies TEXT DEFAULT '')");
        db.execSQL("CREATE TABLE meds(id INTEGER PRIMARY KEY AUTOINCREMENT, patient INTEGER NOT NULL, name TEXT NOT NULL, dosage TEXT, form TEXT, freq TEXT, times TEXT, duration TEXT, notes TEXT, remaining INTEGER DEFAULT 0, total INTEGER DEFAULT 0, active INTEGER DEFAULT 1, created TEXT DEFAULT '2000-01-01')");
        db.execSQL("CREATE TABLE history(id INTEGER PRIMARY KEY AUTOINCREMENT, patient INTEGER, med INTEGER, name TEXT, dosage TEXT, at TEXT, status TEXT, scheduled TEXT DEFAULT '')");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS ux_history_slot ON history(med, scheduled) WHERE scheduled <> ''");
        createAppointments(db);
        db.execSQL("CREATE TABLE reminder_dismissals(rkey TEXT PRIMARY KEY, dismissed_at TEXT)");
        db.execSQL("CREATE TABLE settings(k TEXT PRIMARY KEY, v TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE meds ADD COLUMN created TEXT DEFAULT '2000-01-01'");
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE users ADD COLUMN avatar TEXT DEFAULT ''");
            db.execSQL("ALTER TABLE patients ADD COLUMN avatar TEXT DEFAULT ''");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE patients ADD COLUMN allergies TEXT DEFAULT ''");
            db.execSQL("ALTER TABLE history ADD COLUMN scheduled TEXT DEFAULT ''");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS ux_history_slot ON history(med, scheduled) WHERE scheduled <> ''");
            createAppointments(db);
            db.execSQL("CREATE TABLE IF NOT EXISTS reminder_dismissals(rkey TEXT PRIMARY KEY, dismissed_at TEXT)");
        }
        if (oldVersion < 5) {
            // Google sign-in: accounts are keyed by Firebase UID. Existing rows and all their records are kept;
            // an old account is attached to a UID the first time its verified Google email signs in.
            db.execSQL("ALTER TABLE users ADD COLUMN uid TEXT");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS ux_users_uid ON users(uid) WHERE uid IS NOT NULL");
        }
    }

    static void createAppointments(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS appointments(id INTEGER PRIMARY KEY AUTOINCREMENT, patient INTEGER NOT NULL, title TEXT NOT NULL, doctor TEXT DEFAULT '', date TEXT NOT NULL, time TEXT NOT NULL, notes TEXT DEFAULT '', status TEXT DEFAULT 'SCHEDULED')");
    }

    static String str(Cursor c, String column) {
        int i = c.getColumnIndex(column);
        return i < 0 || c.isNull(i) ? "" : c.getString(i);
    }

    static long lng(Cursor c, String column) {
        return c.getLong(c.getColumnIndexOrThrow(column));
    }
}
