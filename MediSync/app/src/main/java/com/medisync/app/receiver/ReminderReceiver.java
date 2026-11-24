package com.medisync.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.medisync.app.database.MediSyncDatabase;
import com.medisync.app.manager.AlarmScheduler;
import com.medisync.app.manager.NotificationHelper;
import com.medisync.app.model.Appointment;
import com.medisync.app.model.IntakeStatus;
import com.medisync.app.model.Medication;
import com.medisync.app.model.Patient;
import com.medisync.app.util.DateUtils;
import com.medisync.app.util.Keys;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String ACTION_TAKEN = "com.medisync.app.action.TAKEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (action == null) return;
        MediSyncDatabase db = MediSyncDatabase.getInstance(context);
        try {
            switch (action) {
                case AlarmScheduler.ACTION_DOSE_DUE:
                    handleDoseDue(context, db, intent);
                    break;
                case ACTION_TAKEN:
                    handleTaken(context, db, intent);
                    break;
                case AlarmScheduler.ACTION_APPOINTMENT_DUE:
                    handleAppointmentDue(context, db, intent);
                    break;
                default:
                    break;
            }
        } catch (RuntimeException e) {
            // a bad record must never crash the app from a background alarm
        }
    }

    private static void handleDoseDue(Context c, MediSyncDatabase db, Intent in) {
        long patientId = in.getLongExtra(AlarmScheduler.EXTRA_PATIENT_ID, -1);
        long medId = in.getLongExtra(AlarmScheduler.EXTRA_MED_ID, -1);
        int minute = in.getIntExtra(AlarmScheduler.EXTRA_MINUTE, -1);
        if (patientId < 0 || medId < 0 || minute < 0) return;

        Medication m = db.med(medId);
        Patient p = db.patient(patientId);
        boolean master = "1".equals(db.get(Keys.NOTIFICATIONS_ON, "1"));
        boolean medOn = "1".equals(db.get(Keys.remKey(patientId, "med"), "1"));
        if (m == null || p == null || !m.isActive() || m.isAsNeeded() || !master || !medOn) return;

        String slot = DateUtils.today() + " " + DateUtils.formatMinutes24h(minute);
        if (!db.isSlotRecorded(medId, slot)) {
            StringBuilder text = new StringBuilder("Time to take " + m.getName() + " " + m.getDosage()
                    + " (" + DateUtils.formatMinutes(minute) + ")");
            int doseNo = m.doseNumberOf(minute), doseTotal = m.getDoseMinutes().size();
            if (doseNo > 0 && doseTotal > 1) text.append(" \u00B7 Dose ").append(doseNo).append(" of ").append(doseTotal);
            text.append(".");
            int gap = m.getIntervalHours();
            if (doseNo > 0 && doseNo < doseTotal && gap > 0) {
                text.append(" Next dose in ").append(gap).append(gap == 1 ? " hour." : " hours.");
            }
            if (m.isLowStock(Medication.LOW_STOCK_DAYS) && "1".equals(db.get(Keys.remKey(patientId, "low"), "1"))) {
                text.append(" Low supply: about ").append(m.daysOfSupplyLeft()).append(" day(s) left.");
            }
            NotificationHelper.showDose(c, AlarmScheduler.doseNotificationId(medId, minute),
                    "MediSync · " + p.getName(), text.toString(), patientId, medId, minute);
        }
        // re-arm tomorrow's alarm for this dose time
        AlarmScheduler.scheduleDose(c, patientId, medId, minute);
    }

    private static void handleTaken(Context c, MediSyncDatabase db, Intent in) {
        long patientId = in.getLongExtra(AlarmScheduler.EXTRA_PATIENT_ID, -1);
        long medId = in.getLongExtra(AlarmScheduler.EXTRA_MED_ID, -1);
        int minute = in.getIntExtra(AlarmScheduler.EXTRA_MINUTE, -1);
        int notificationId = in.getIntExtra(AlarmScheduler.EXTRA_NOTIFICATION_ID, -1);
        Medication m = db.med(medId);
        if (m != null && patientId >= 0 && minute >= 0) {
            String slot = DateUtils.today() + " " + DateUtils.formatMinutes24h(minute);
            if (!db.isSlotRecorded(medId, slot)) {
                IntakeStatus status = IntakeStatus.classifyTaken(minute, DateUtils.nowMinutes());
                db.recordDose(patientId, m, slot, DateUtils.now(), status);
            }
        }
        if (notificationId >= 0) NotificationHelper.cancel(c, notificationId);
    }

    private static void handleAppointmentDue(Context c, MediSyncDatabase db, Intent in) {
        long id = in.getLongExtra(AlarmScheduler.EXTRA_APPOINTMENT_ID, -1);
        if (id < 0) return;
        Appointment a = db.appointment(id);
        if (a == null || !a.isScheduled()) return;
        if (!"1".equals(db.get(Keys.NOTIFICATIONS_ON, "1"))) return;
        if (!"1".equals(db.get(Keys.remKey(a.getPatientId(), "follow"), "1"))) return;
        String doctor = a.getDoctor().isEmpty() ? "" : " with " + a.getDoctor();
        NotificationHelper.showAppointment(c, 3000 + (int) (a.getId() % 50000),
                "Upcoming appointment",
                a.getTitle() + doctor + " at " + DateUtils.formatMinutes(DateUtils.minutesOf24h(a.getTime())) + " today or soon.");
    }
}
