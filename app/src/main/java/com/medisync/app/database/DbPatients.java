package com.medisync.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.medisync.app.model.Patient;
import java.util.ArrayList;
import java.util.List;

/** Patients and dependents. */
abstract class DbPatients extends DbAccounts {
    DbPatients(Context context) {
        super(context);
    }

    public long addPatient(long ownerId, String name, String category, String relationship, String dob, String avatar) {
        ContentValues v = new ContentValues();
        v.put("owner", ownerId);
        v.put("name", name.trim());
        v.put("cat", category);
        v.put("rel", relationship);
        v.put("dob", dob);
        v.put("avatar", avatar == null ? "" : avatar);
        v.put("allergies", "");
        return getWritableDatabase().insertOrThrow("patients", null, v);
    }

    public List<Patient> patients(long ownerId) {
        List<Patient> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM patients WHERE owner=? ORDER BY id",
                new String[]{String.valueOf(ownerId)});
        try {
            while (c.moveToNext()) {
                try {
                    out.add(toPatient(c));
                } catch (IllegalArgumentException ignored) {
                    // skip a malformed row rather than crash the screen
                }
            }
        } finally {
            c.close();
        }
        return out;
    }

    public Patient patient(long id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM patients WHERE id=?", new String[]{String.valueOf(id)});
        try {
            return c.moveToFirst() ? toPatient(c) : null;
        } finally {
            c.close();
        }
    }

    public void updatePatient(long id, String relationship, String dob) {
        ContentValues v = new ContentValues();
        v.put("rel", relationship);
        v.put("dob", dob);
        getWritableDatabase().update("patients", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void updatePatientAllergies(long id, String allergies) {
        ContentValues v = new ContentValues();
        v.put("allergies", allergies == null ? "" : allergies.trim());
        getWritableDatabase().update("patients", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void setPatientAvatar(long id, String avatar) {
        ContentValues v = new ContentValues();
        v.put("avatar", avatar == null ? "" : avatar);
        getWritableDatabase().update("patients", v, "id=?", new String[]{String.valueOf(id)});
    }

    /** Deletes a patient and everything that belongs to them, in one transaction. */
    public void deletePatient(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] arg = {String.valueOf(id)};
        db.beginTransaction();
        try {
            db.delete("history", "patient=?", arg);
            db.delete("meds", "patient=?", arg);
            db.delete("appointments", "patient=?", arg);
            db.delete("patients", "id=?", arg);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    static Patient toPatient(Cursor c) {
        return new Patient(lng(c, "id"), lng(c, "owner"), str(c, "name"), str(c, "cat"), str(c, "rel"),
                str(c, "dob"), str(c, "avatar"), str(c, "allergies"));
    }
}
