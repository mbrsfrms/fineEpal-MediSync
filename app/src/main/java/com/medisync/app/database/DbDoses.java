package com.medisync.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.medisync.app.model.IntakeRecord;
import com.medisync.app.model.IntakeStatus;
import com.medisync.app.model.Medication;
import java.util.ArrayList;
import java.util.List;

/** Dose intake records and history. */
abstract class DbDoses extends DbMeds {
    DbDoses(Context context) {
        super(context);
    }

    /**
     * Records one dose. The dose slot (medicine + scheduled time) is unique, so recording the same slot again
     * updates the existing row instead of adding a new one. Supply is changed only when the taken state changes,
     * so a dose is never subtracted twice.
     *
     * @param scheduledAt "yyyy-MM-dd HH:mm" of the slot, or "" for an unplanned dose
     * @param recordedAt  "yyyy-MM-dd HH:mm" when the dose was logged
     * @return the history row id
     */
    public long recordDose(long patientId, Medication med, String scheduledAt, String recordedAt, IntakeStatus status) {
        SQLiteDatabase db = getWritableDatabase();
        boolean hasSlot = scheduledAt != null && !scheduledAt.isEmpty();
        db.beginTransaction();
        try {
            long existingId = -1;
            IntakeStatus existingStatus = null;
            if (hasSlot) {
                Cursor c = db.rawQuery("SELECT id, status FROM history WHERE med=? AND scheduled=?",
                        new String[]{String.valueOf(med.getId()), scheduledAt});
                try {
                    if (c.moveToFirst()) {
                        existingId = c.getLong(0);
                        existingStatus = IntakeStatus.fromStored(c.getString(1));
                    }
                } finally {
                    c.close();
                }
            }
            ContentValues v = new ContentValues();
            v.put("patient", patientId);
            v.put("med", med.getId());
            v.put("name", med.getName());
            v.put("dosage", med.getDosage());
            v.put("at", recordedAt);
            v.put("status", status.getStoredValue());
            v.put("scheduled", hasSlot ? scheduledAt : "");

            long rowId;
            if (existingId >= 0) {
                db.update("history", v, "id=?", new String[]{String.valueOf(existingId)});
                rowId = existingId;
            } else {
                rowId = db.insertOrThrow("history", null, v);
            }
            boolean wasTaken = existingStatus != null && existingStatus.countsAsTaken();
            boolean nowTaken = status.countsAsTaken();
            if (nowTaken && !wasTaken) adjustSupply(db, med.getId(), -1);
            else if (!nowTaken && wasTaken) adjustSupply(db, med.getId(), +1);
            db.setTransactionSuccessful();
            return rowId;
        } finally {
            db.endTransaction();
        }
    }

    /** True when a history row already exists for this dose slot (taken, late, missed or skipped). */
    public boolean isSlotRecorded(long medId, String scheduledAt) {
        Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM history WHERE med=? AND scheduled=?",
                new String[]{String.valueOf(medId), scheduledAt});
        try {
            return c.moveToFirst();
        } finally {
            c.close();
        }
    }

    public List<IntakeRecord> history(long patientId) {
        List<IntakeRecord> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM history WHERE patient=? ORDER BY at DESC, id DESC", new String[]{String.valueOf(patientId)});
        try {
            while (c.moveToNext()) out.add(toRecord(c));
        } finally {
            c.close();
        }
        return out;
    }

    static IntakeRecord toRecord(Cursor c) {
        return new IntakeRecord(lng(c, "id"), lng(c, "patient"), lng(c, "med"), str(c, "name"), str(c, "dosage"),
                str(c, "scheduled"), str(c, "at"), str(c, "status"));
    }
}
