package com.medisync.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.medisync.app.model.Medication;
import com.medisync.app.util.DateUtils;
import java.util.ArrayList;
import java.util.List;

/** Medications and supply tracking. */
abstract class DbMeds extends DbPatients {
    DbMeds(Context context) {
        super(context);
    }

    public long addMed(long patientId, String name, String dosage, String form, String frequency, String times,
                       String duration, String notes, int quantity) {
        ContentValues v = new ContentValues();
        v.put("patient", patientId);
        v.put("name", name.trim());
        v.put("dosage", dosage.trim());
        v.put("form", form);
        v.put("freq", frequency);
        v.put("times", times);
        v.put("duration", duration);
        v.put("notes", notes == null ? "" : notes.trim());
        v.put("remaining", quantity);
        v.put("total", quantity);
        v.put("active", 1);
        v.put("created", DateUtils.today());
        return getWritableDatabase().insertOrThrow("meds", null, v);
    }

    public List<Medication> meds(long patientId, boolean activeOnly) {
        List<Medication> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM meds WHERE patient=? AND active=? ORDER BY name COLLATE NOCASE",
                new String[]{String.valueOf(patientId), activeOnly ? "1" : "0"});
        try {
            while (c.moveToNext()) {
                try {
                    out.add(toMedication(c));
                } catch (IllegalArgumentException ignored) {
                    // skip a malformed row
                }
            }
        } finally {
            c.close();
        }
        return out;
    }

    public Medication med(long id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM meds WHERE id=?", new String[]{String.valueOf(id)});
        try {
            return c.moveToFirst() ? toMedication(c) : null;
        } finally {
            c.close();
        }
    }

    public void setActive(long id, boolean active) {
        ContentValues v = new ContentValues();
        v.put("active", active ? 1 : 0);
        getWritableDatabase().update("meds", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteMed(long id) {
        getWritableDatabase().delete("meds", "id=?", new String[]{String.valueOf(id)});
    }

    /** Updates every editable field of a medicine. The added date and intake history are left untouched. */
    public void updateMed(long id, String name, String dosage, String form, String frequency, String times,
                          String duration, String notes, int remaining) {
        if (remaining < 0) throw new IllegalArgumentException("Remaining quantity cannot be negative.");
        getWritableDatabase().execSQL(
                "UPDATE meds SET name=?, dosage=?, form=?, freq=?, times=?, duration=?, notes=?, remaining=?, "
                        + "total=CASE WHEN total<? THEN ? ELSE total END WHERE id=?",
                new Object[]{name.trim(), dosage.trim(), form, frequency, times, duration,
                        notes == null ? "" : notes.trim(), remaining, remaining, remaining, id});
    }

    /** Sets the remaining quantity manually. */
    public void updateSupply(long id, int remaining) {
        if (remaining < 0) throw new IllegalArgumentException("Remaining quantity cannot be negative.");
        getWritableDatabase().execSQL(
                "UPDATE meds SET remaining=?, total=CASE WHEN total<? THEN ? ELSE total END WHERE id=?",
                new Object[]{remaining, remaining, remaining, id});
    }

    /** Adds delta (may be negative) to the remaining quantity, never below zero. */
    static void adjustSupply(SQLiteDatabase db, long medId, int delta) {
        Cursor c = db.rawQuery("SELECT remaining FROM meds WHERE id=?", new String[]{String.valueOf(medId)});
        int current;
        try {
            if (!c.moveToFirst()) return;
            current = c.getInt(0);
        } finally {
            c.close();
        }
        int next = Math.max(0, current + delta);
        db.execSQL("UPDATE meds SET remaining=?, total=CASE WHEN total<? THEN ? ELSE total END WHERE id=?",
                new Object[]{next, next, next, medId});
    }

    static Medication toMedication(Cursor c) {
        String created = str(c, "created");
        return new Medication(lng(c, "id"), lng(c, "patient"), str(c, "name"), str(c, "dosage"), str(c, "form"),
                str(c, "freq"), str(c, "times"), str(c, "duration"), str(c, "notes"),
                created.isEmpty() ? "2000-01-01" : created,
                c.getInt(c.getColumnIndexOrThrow("remaining")), c.getInt(c.getColumnIndexOrThrow("total")),
                c.getInt(c.getColumnIndexOrThrow("active")) == 1);
    }
}
