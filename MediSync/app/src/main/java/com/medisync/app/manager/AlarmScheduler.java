package com.medisync.app.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.medisync.app.database.MediSyncDatabase;
import com.medisync.app.model.Appointment;
import com.medisync.app.model.Medication;
import com.medisync.app.model.Patient;
import com.medisync.app.receiver.ReminderReceiver;
import com.medisync.app.util.DateUtils;
import com.medisync.app.util.Keys;

/**
 * Schedules exact alarms for each medication dose time and for appointment reminders.
 * Each alarm has a stable request code, so scheduling the same dose again replaces the old alarm
 * instead of creating a duplicate. Alarms are re-armed by the receiver after each firing, and
 * rescheduleAll is run at login, after edits, and after reboot or a clock change.
 */
public final class AlarmScheduler {
    public static final String ACTION_DOSE_DUE = "com.medisync.app.action.DOSE_DUE";
    public static final String ACTION_APPOINTMENT_DUE = "com.medisync.app.action.APPOINTMENT_DUE";
    public static final String EXTRA_PATIENT_ID = "patient_id";
    public static final String EXTRA_MED_ID = "med_id";
    public static final String EXTRA_MINUTE = "minute";
    public static final String EXTRA_APPOINTMENT_ID = "appointment_id";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    /** Appointment reminders fire this long before the appointment. */
    static final long APPOINTMENT_LEAD_MILLIS = 60L * 60 * 1000;

    private AlarmScheduler() { }

    /** Re-arms all dose and appointment alarms for every patient of this account. */
    public static void rescheduleAll(Context c, long userId) {
        if (userId < 0) return;
        MediSyncDatabase db = MediSyncDatabase.getInstance(c);
        boolean master = "1".equals(db.get(Keys.NOTIFICATIONS_ON, "1"));
        for (Patient p : db.patients(userId)) {
            boolean medOn = master && "1".equals(db.get(Keys.remKey(p.getId(), "med"), "1"));
            boolean apptOn = master && "1".equals(db.get(Keys.remKey(p.getId(), "follow"), "1"));
            for (Medication m : db.meds(p.getId(), true)) {
                if (medOn) {
                    for (int minute : m.getDoseMinutes()) scheduleDose(c, p.getId(), m.getId(), minute);
                }
            }
            for (Appointment a : db.appointments(p.getId())) {
                if (apptOn && a.isScheduled()) scheduleAppointment(c, a);
                else cancelAppointment(c, a.getId());
            }
        }
    }

    /** Schedules the next occurrence of a dose (today if still ahead, otherwise tomorrow). */
    public static void scheduleDose(Context c, long patientId, long medId, int minuteOfDay) {
        long trigger = DateUtils.millisAt(DateUtils.today(), minuteOfDay);
        if (trigger <= System.currentTimeMillis()) trigger = DateUtils.millisAt(DateUtils.daysFromToday(1), minuteOfDay);
        Intent i = new Intent(c, ReminderReceiver.class)
                .setAction(ACTION_DOSE_DUE)
                .putExtra(EXTRA_PATIENT_ID, patientId)
                .putExtra(EXTRA_MED_ID, medId)
                .putExtra(EXTRA_MINUTE, minuteOfDay);
        PendingIntent pi = PendingIntent.getBroadcast(c, doseRequestCode(medId, minuteOfDay), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.cancel(pi);
        setAlarm(c, am, trigger, pi);
    }

    /** Cancels every dose alarm of a medicine (used when it is deleted or deactivated). */
    public static void cancelMedication(Context c, Medication m) {
        for (int minute : m.getDoseMinutes()) {
            Intent i = new Intent(c, ReminderReceiver.class).setAction(ACTION_DOSE_DUE);
            PendingIntent pi = PendingIntent.getBroadcast(c, doseRequestCode(m.getId(), minute), i,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            if (pi != null && am != null) {
                am.cancel(pi);
                pi.cancel();
            }
        }
    }

    /** Schedules a reminder one hour before a scheduled appointment; cancels it if the time has passed. */
    public static void scheduleAppointment(Context c, Appointment a) {
        long now = System.currentTimeMillis();
        long start = a.getStartMillis();
        if (!a.isScheduled() || start <= now) {
            cancelAppointment(c, a.getId());
            return;
        }
        long trigger = Math.max(now + 5000, start - APPOINTMENT_LEAD_MILLIS);
        Intent i = new Intent(c, ReminderReceiver.class)
                .setAction(ACTION_APPOINTMENT_DUE)
                .putExtra(EXTRA_APPOINTMENT_ID, a.getId());
        PendingIntent pi = PendingIntent.getBroadcast(c, appointmentRequestCode(a.getId()), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.cancel(pi);
        setAlarm(c, am, trigger, pi);
    }

    public static void cancelAppointment(Context c, long appointmentId) {
        Intent i = new Intent(c, ReminderReceiver.class).setAction(ACTION_APPOINTMENT_DUE);
        PendingIntent pi = PendingIntent.getBroadcast(c, appointmentRequestCode(appointmentId), i,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (pi != null && am != null) {
            am.cancel(pi);
            pi.cancel();
        }
    }

    /** Stable notification id for a dose reminder. */
    public static int doseNotificationId(long medId, int minuteOfDay) {
        return 2000 + (Math.abs(("dose|" + medId + "|" + minuteOfDay).hashCode()) % 50000);
    }

    static int doseRequestCode(long medId, int minuteOfDay) {
        return Math.abs(("dose|" + medId + "|" + minuteOfDay).hashCode());
    }

    static int appointmentRequestCode(long appointmentId) {
        return Math.abs(("appt|" + appointmentId).hashCode()) + 1;
    }

    /** Exact alarm when allowed (Android 12+ needs the user's permission); otherwise an inexact alarm. */
    private static void setAlarm(Context c, AlarmManager am, long triggerAt, PendingIntent pi) {
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (SecurityException e) {
            // exact alarms were revoked; fall back to an inexact alarm rather than crashing
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    /** Re-arms alarms for the signed-in account. Used after reboot and after a clock or timezone change. */
    public static void rescheduleFromSettings(Context c) {
        MediSyncDatabase db = MediSyncDatabase.getInstance(c);
        long uid;
        try {
            uid = Long.parseLong(db.get(Keys.USER_ID, "-1"));
        } catch (NumberFormatException e) {
            return;
        }
        rescheduleAll(c, uid);
    }
}
