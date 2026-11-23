package com.medisync.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.medisync.app.model.Appointment;
import com.medisync.app.util.DateUtils;
import java.util.ArrayList;
import java.util.List;

/** Appointments and dismissed reminders. */
abstract class DbAppointments extends DbDoses {
    DbAppointments(Context context) {
        super(context);
    }

    public long addAppointment(long patientId, String title, String doctor, String date, String time, String notes) {
        ContentValues v = new ContentValues();
        v.put("patient", patientId);
        v.put("title", title.trim());
        v.put("doctor", doctor == null ? "" : doctor.trim());
        v.put("date", date);
        v.put("time", time);
        v.put("notes", notes == null ? "" : notes.trim());
        v.put("status", Appointment.Status.SCHEDULED.name());
        return getWritableDatabase().insertOrThrow("appointments", null, v);
    }

    public void updateAppointment(long id, String title, String doctor, String date, String time, String notes) {
        ContentValues v = new ContentValues();
        v.put("title", title.trim());
        v.put("doctor", doctor == null ? "" : doctor.trim());
        v.put("date", date);
        v.put("time", time);
        v.put("notes", notes == null ? "" : notes.trim());
        getWritableDatabase().update("appointments", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void setAppointmentStatus(long id, Appointment.Status status) {
        ContentValues v = new ContentValues();
        v.put("status", status.name());
        getWritableDatabase().update("appointments", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteAppointment(long id) {
        getWritableDatabase().delete("appointments", "id=?", new String[]{String.valueOf(id)});
    }

    public List<Appointment> appointments(long patientId) {
        List<Appointment> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM appointments WHERE patient=? ORDER BY date, time", new String[]{String.valueOf(patientId)});
        try {
            while (c.moveToNext()) {
                try {
                    out.add(toAppointment(c));
                } catch (IllegalArgumentException ignored) {
                    // skip a malformed row
                }
            }
        } finally {
            c.close();
        }
        return out;
    }

    public Appointment appointment(long id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM appointments WHERE id=?", new String[]{String.valueOf(id)});
        try {
            return c.moveToFirst() ? toAppointment(c) : null;
        } finally {
            c.close();
        }
    }

    public void dismissReminder(String key) {
        ContentValues v = new ContentValues();
        v.put("rkey", key);
        v.put("dismissed_at", DateUtils.now());
        getWritableDatabase().insertWithOnConflict("reminder_dismissals", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean isReminderDismissed(String key) {
        Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM reminder_dismissals WHERE rkey=?", new String[]{key});
        try {
            return c.moveToFirst();
        } finally {
            c.close();
        }
    }

    static Appointment toAppointment(Cursor c) {
        return new Appointment(lng(c, "id"), lng(c, "patient"), str(c, "title"), str(c, "doctor"),
                str(c, "date"), str(c, "time"), str(c, "notes"), Appointment.Status.fromStored(str(c, "status")));
    }
}
