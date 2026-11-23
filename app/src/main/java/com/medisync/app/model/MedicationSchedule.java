package com.medisync.app.model;

import com.medisync.app.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/** One dose slot of a medication on a specific date. Status moves from PENDING to a final outcome. */
public class MedicationSchedule {
    public enum Status { PENDING, TAKEN, LATE, SKIPPED, MISSED }

    private final long medicationId;
    private final String date;
    private final int minuteOfDay;
    private Status status = Status.PENDING;

    public MedicationSchedule(long medicationId, String date, int minuteOfDay) {
        if (minuteOfDay < 0 || minuteOfDay > 1439) throw new IllegalArgumentException("Invalid time of day.");
        this.medicationId = medicationId;
        this.date = date;
        this.minuteOfDay = minuteOfDay;
    }

    /** Creates one schedule per dose time of the medicine for the given date. Empty for as-needed medicines. */
    public static List<MedicationSchedule> createFor(Medication m, String date) {
        List<MedicationSchedule> out = new ArrayList<>();
        if (m.isAsNeeded()) return out;
        for (int minute : m.getDoseMinutes()) out.add(new MedicationSchedule(m.getId(), date, minute));
        return out;
    }

    public long getMedicationId() { return medicationId; }

    public String getDate() { return date; }

    public int getMinuteOfDay() { return minuteOfDay; }

    public Status getStatus() { return status; }

    /** Key that uniquely identifies this dose slot (used to prevent double logging). */
    public String getSlotKey() {
        return medicationId + "|" + date + "|" + DateUtils.formatMinutes24h(minuteOfDay);
    }

    public void markAsTaken(int actualMinute) {
        requirePending();
        status = IntakeStatus.classifyTaken(minuteOfDay, actualMinute) == IntakeStatus.LATE ? Status.LATE : Status.TAKEN;
    }

    public void markAsSkipped() { requirePending(); status = Status.SKIPPED; }

    public void markAsMissed() { requirePending(); status = Status.MISSED; }

    /** A pending dose becomes overdue once the grace period after its time has passed on a past date or today. */
    public boolean isOverdue(String today, int nowMinute) {
        if (status != Status.PENDING) return false;
        if (date.compareTo(today) < 0) return true;
        return date.equals(today) && nowMinute > minuteOfDay + IntakeStatus.LATE_AFTER_MINUTES;
    }

    private void requirePending() {
        if (status != Status.PENDING) throw new IllegalStateException("This dose was already recorded as " + status + ".");
    }
}
