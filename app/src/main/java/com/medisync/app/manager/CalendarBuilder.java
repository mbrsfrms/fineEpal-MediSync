package com.medisync.app.manager;

import com.medisync.app.model.Appointment;
import com.medisync.app.model.CalendarEvent;
import com.medisync.app.model.IntakeRecord;
import com.medisync.app.model.Medication;
import com.medisync.app.util.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gathers medication doses, appointments and health reminders into one calendar.
 * Statuses: Taken, Late, Missed, Skipped (from history); Due, Upcoming, Scheduled (not yet recorded);
 * Scheduled, Completed, Cancelled, Overdue (appointments); Low supply / Refill soon (reminders).
 */
public final class CalendarBuilder {
    private CalendarBuilder() { }

    /** Events for every day from fromDate to toDate (inclusive), keyed by yyyy-MM-dd and sorted by time. */
    public static Map<String, List<CalendarEvent>> build(String fromDate, String toDate, String today, int nowMinute,
                                                         long nowMillis, List<Medication> activeMeds,
                                                         List<IntakeRecord> history, List<Appointment> appointments) {
        Map<String, List<CalendarEvent>> byDate = new LinkedHashMap<>();
        int span = DateUtils.daysBetween(fromDate, toDate);
        for (int i = 0; i <= span; i++) {
            String day = DateUtils.shiftDate(fromDate, i);
            List<CalendarEvent> events = new ArrayList<>();
            for (Medication m : activeMeds) addMedicationEvents(events, m, day, today, nowMinute, history);
            for (Appointment a : appointments) {
                if (a.getDate().equals(day)) events.add(appointmentEvent(a, nowMillis));
            }
            if (day.equals(today)) {
                for (Medication m : activeMeds) {
                    if (m.isLowStock(Medication.LOW_STOCK_DAYS)) {
                        events.add(new CalendarEvent(day, -1, m.getName() + " is running low",
                                m.getRemaining() + " " + m.getUnitLabel() + " left", CalendarEvent.Type.REMINDER,
                                "Low supply", m.getId()));
                    }
                }
            }
            Collections.sort(events, (a, b) -> a.getMinuteOfDay() - b.getMinuteOfDay());
            byDate.put(day, events);
        }
        return byDate;
    }

    /** Events for the days of one calendar month. Month is 1-based. */
    public static Map<String, List<CalendarEvent>> buildMonth(int year, int month, String today, int nowMinute,
                                                              long nowMillis, List<Medication> activeMeds,
                                                              List<IntakeRecord> history, List<Appointment> appointments) {
        String first = String.format(java.util.Locale.US, "%04d-%02d-01", year, month);
        int days = lengthOfMonth(year, month);
        String last = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month, days);
        return build(first, last, today, nowMinute, nowMillis, activeMeds, history, appointments);
    }

    public static int lengthOfMonth(int year, int month) {
        int[] d = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        boolean leap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
        return month == 2 && leap ? 29 : d[month - 1];
    }

    private static void addMedicationEvents(List<CalendarEvent> out, Medication m, String day, String today,
                                            int nowMinute, List<IntakeRecord> history) {
        if (m.isAsNeeded() || !m.isStartedBy(day)) return;
        if (m.isEveryOtherDay() && DateUtils.daysBetween(m.getCreated(), day) % 2 != 0) return;
        List<Integer> slots = m.getDoseMinutes();
        if (slots.isEmpty()) return;

        // records logged for this medicine on this day
        List<IntakeRecord> recs = new ArrayList<>();
        for (IntakeRecord r : history) {
            if (r.getMedicationId() == m.getId() && day.equals(r.getDate())) recs.add(r);
        }
        // first pass: match records to slots by their scheduled time
        IntakeRecord[] matched = new IntakeRecord[slots.size()];
        Set<IntakeRecord> used = new HashSet<>();
        for (int i = 0; i < slots.size(); i++) {
            for (IntakeRecord r : recs) {
                if (!used.contains(r) && slotMinute(r) == slots.get(i)) {
                    matched[i] = r;
                    used.add(r);
                    break;
                }
            }
        }
        // second pass: records without a usable scheduled time (older app versions) fill the free slots in order
        for (IntakeRecord r : recs) {
            if (used.contains(r)) continue;
            for (int i = 0; i < slots.size(); i++) {
                if (matched[i] == null) {
                    matched[i] = r;
                    used.add(r);
                    break;
                }
            }
        }
        for (int i = 0; i < slots.size(); i++) {
            int minute = slots.get(i);
            String title = m.getName() + " " + m.getDosage();
            String time = DateUtils.formatMinutes(minute);
            if (matched[i] != null) {
                IntakeRecord r = matched[i];
                String detail = time + (r.getRecordedAt().length() >= 16 ? " · recorded at " + r.getRecordedAt().substring(11, 16) : "");
                out.add(new CalendarEvent(day, minute, title, detail, CalendarEvent.Type.MEDICATION, r.getStatus(), m.getId()));
            } else if (day.compareTo(today) < 0) {
                out.add(new CalendarEvent(day, minute, title, time, CalendarEvent.Type.MEDICATION, "Missed", m.getId()));
            } else if (day.equals(today)) {
                out.add(new CalendarEvent(day, minute, title, time, CalendarEvent.Type.MEDICATION,
                        minute <= nowMinute ? "Due" : "Upcoming", m.getId()));
            } else {
                out.add(new CalendarEvent(day, minute, title, time, CalendarEvent.Type.MEDICATION, "Scheduled", m.getId()));
            }
        }
    }

    private static int slotMinute(IntakeRecord r) {
        String s = r.getScheduledAt();
        return s.length() >= 16 ? DateUtils.minutesOf24h(s.substring(11, 16)) : -2;
    }

    private static CalendarEvent appointmentEvent(Appointment a, long nowMillis) {
        String status = a.isOverdue(nowMillis) ? "Overdue" : a.getStatus().getLabel();
        String detail = DateUtils.formatMinutes(DateUtils.minutesOf24h(a.getTime()))
                + (a.getDoctor().isEmpty() ? "" : " · " + a.getDoctor());
        return new CalendarEvent(a.getDate(), DateUtils.minutesOf24h(a.getTime()), a.getTitle(), detail,
                CalendarEvent.Type.APPOINTMENT, status, a.getId());
    }
}
