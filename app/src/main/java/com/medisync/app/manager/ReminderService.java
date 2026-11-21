package com.medisync.app.manager;

import com.medisync.app.model.Appointment;
import com.medisync.app.model.AppointmentReminder;
import com.medisync.app.model.IntakeRecord;
import com.medisync.app.model.Medication;
import com.medisync.app.model.MedicationReminder;
import com.medisync.app.model.Reminder;
import com.medisync.app.model.RestockReminder;
import com.medisync.app.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/** Builds the list of reminders that should be visible now. Each reminder is returned as the abstract type. */
public final class ReminderService {
    /** A dose is shown as a reminder from this many minutes before its time until this many minutes after. */
    static final int SHOW_BEFORE_MINUTES = 30;
    static final int SHOW_AFTER_MINUTES = 60;

    private ReminderService() { }

    public static List<Reminder> build(long patientId, List<Medication> activeMeds, List<IntakeRecord> history,
                                       List<Appointment> appointments, String today, int nowMinute, long nowMillis) {
        List<Reminder> out = new ArrayList<>();
        for (Medication m : activeMeds) {
            boolean dueToday = m.isStartedBy(today) && (!m.isEveryOtherDay() || isEveryOtherDue(m, today));
            for (int minute : m.getDoseMinutes()) {
                if (m.isAsNeeded() || !dueToday) continue;
                boolean inWindow = nowMinute >= minute - SHOW_BEFORE_MINUTES && nowMinute <= minute + SHOW_AFTER_MINUTES;
                if (!inWindow || isResolved(history, m.getId(), today, minute)) continue;
                String time = DateUtils.formatMinutes(minute);
                out.add(new MedicationReminder("med|" + m.getId() + "|" + today + "|" + minute, patientId,
                        m.getName(), m.getDosage(), time, m.doseNumberOf(minute), m.getDoseMinutes().size()));
            }
            if (m.isLowStock(Medication.LOW_STOCK_DAYS)) {
                out.add(new RestockReminder("restock|" + m.getId() + "|" + today, patientId, m.getName(),
                        m.getRemaining(), m.daysOfSupplyLeft(), m.getUnitLabel()));
            }
        }
        for (Appointment a : appointments) {
            if (!a.isScheduled()) continue;
            long start = a.getStartMillis();
            if (start >= nowMillis && start - nowMillis <= 24L * 60 * 60 * 1000) {
                out.add(new AppointmentReminder("appt|" + a.getId() + "|" + a.getDate(), patientId,
                        a.getTitle(), a.getDoctor(), a.getDate() + " at " + DateUtils.formatMinutes(DateUtils.minutesOf24h(a.getTime()))));
            }
        }
        return out;
    }

    /** A dose is resolved once any record exists for that slot (taken, late, missed or skipped). */
    static boolean isResolved(List<IntakeRecord> history, long medId, String day, int minute) {
        String slot = day + " " + DateUtils.formatMinutes24h(minute);
        for (IntakeRecord r : history) {
            if (r.getMedicationId() == medId && slot.equals(r.getScheduledAt())) return true;
        }
        return false;
    }

    /** Every-other-day medicines are due on alternate days counted from the day they were added. */
    static boolean isEveryOtherDue(Medication m, String day) {
        return DateUtils.daysBetween(m.getCreated(), day) % 2 == 0;
    }
}
