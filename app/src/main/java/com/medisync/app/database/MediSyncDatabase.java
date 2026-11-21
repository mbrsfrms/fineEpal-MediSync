package com.medisync.app.database;

import android.content.Context;

/** Local SQLite database; the layers it extends hold the actual queries. */
public final class MediSyncDatabase extends DbAppointments {
    private MediSyncDatabase(Context context) {
        super(context);
    }

    private static MediSyncDatabase instance;

    public static synchronized MediSyncDatabase getInstance(Context context) {
        if (instance == null) instance = new MediSyncDatabase(context.getApplicationContext());
        return instance;
    }
}
