package com.medisync.app.model;

import com.medisync.app.util.DateUtils;

import java.util.List;

/** A medicine managed for one patient, including its supply count. */
public class Medication {
    public static final String FREQ_ONCE = "Once daily";
    public static final String FREQ_TWICE = "Twice daily";
    public static final String FREQ_THRICE = "Three times daily";
    public static final String FREQ_FOUR = "Four times daily";
    public static final String FREQ_EVERY_OTHER = "Every other day";
    public static final String FREQ_AS_NEEDED = "As needed";
    public static final int LOW_STOCK_DAYS = 3;
    public static final int REFILL_SOON_DAYS = 7;

    private final long id;
    private final long patientId;
    private String name;
    private String dosage;
    private String form;
    private String frequency;
    private String times;
    private String duration;
    private String notes;
    private final String created;
    private int remaining;
    private int total;
    private boolean active;

    public Medication(long id, long patientId, String name, String dosage, String form, String frequency,
                      String times, String duration, String notes, String created,
                      int remaining, int total, boolean active) {
        this.id = id;
        this.patientId = patientId;
        this.name = requireText(name, "Medication name");
        this.dosage = requireText(dosage, "Dosage");
        this.form = form == null ? "" : form;
        this.frequency = frequency == null || frequency.isEmpty() ? FREQ_ONCE : frequency;
        this.times = times == null ? "" : times;
        this.duration = duration == null ? "" : duration;
        this.notes = notes == null ? "" : notes;
        this.created = created == null || created.isEmpty() ? DateUtils.today() : created;
        this.remaining = Math.max(0, remaining);
        this.total = Math.max(this.remaining, total);
        this.active = active;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    public long getId() { return id; }

    public long getPatientId() { return patientId; }

    public String getName() { return name; }

    public String getDosage() { return dosage; }

    public String getForm() { return form; }

    public String getFrequency() { return frequency; }

    public String getTimes() { return times; }

    public String getDuration() { return duration; }

    public String getNotes() { return notes; }

    public String getCreated() { return created; }

    public int getRemaining() { return remaining; }

    public int getTotal() { return total; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    /** Sets the remaining quantity. Negative values are rejected rather than silently clamped. */
    public void setRemaining(int remaining) {
        if (remaining < 0) throw new IllegalArgumentException("Remaining quantity cannot be negative.");
        this.remaining = remaining;
        if (this.total < remaining) this.total = remaining;
    }

    /** Minutes after midnight for each scheduled dose, sorted. */
    public List<Integer> getDoseMinutes() { return DateUtils.parseTimes(times); }

    public boolean isAsNeeded() { return FREQ_AS_NEEDED.equals(frequency); }

    public boolean isEveryOtherDay() { return FREQ_EVERY_OTHER.equals(frequency); }

    /** Doses per day used for supply estimates. */
    public double dosesPerDay() { return dosesPerDay(frequency, times); }

    /** Doses per day for a frequency and a list of times such as "8:00 AM, 8:00 PM". */
    public static double dosesPerDay(String frequency, String times) {
        if (FREQ_EVERY_OTHER.equals(frequency)) return 0.5;
        if (FREQ_AS_NEEDED.equals(frequency)) return 1;
        if (FREQ_TWICE.equals(frequency)) return 2;
        if (FREQ_THRICE.equals(frequency)) return 3;
        if (FREQ_FOUR.equals(frequency)) return 4;
        return Math.max(1, DateUtils.parseTimes(times).size());
    }

    /** Number of dose times in a day for a frequency (0 when it has no fixed daily doses). */
    public static int doseCountFor(String frequency) {
        if (FREQ_TWICE.equals(frequency)) return 2;
        if (FREQ_THRICE.equals(frequency)) return 3;
        if (FREQ_FOUR.equals(frequency)) return 4;
        if (FREQ_ONCE.equals(frequency) || FREQ_EVERY_OTHER.equals(frequency)) return 1;
        return 0;
    }

    /** Largest whole-hour gap that still fits every dose of the frequency inside one day. */
    public static int maxIntervalHours(String frequency) {
        int n = doseCountFor(frequency);
        return n <= 1 ? 0 : 24 / n;
    }

    /** Sensible default gap in hours between doses for a frequency. */
    public static int defaultIntervalHours(String frequency) {
        int n = doseCountFor(frequency);
        if (n <= 1) return 0;
        return n == 2 ? 12 : n == 3 ? 6 : 4;
    }

    /**
     * Builds the dose times for a frequency from the first dose and the gap in hours between doses,
     * e.g. Three times daily, 8:00 AM, 4 hours gives "8:00 AM, 12:00 PM, 4:00 PM".
     * Doses that pass midnight wrap to the next morning, so the daily pattern still repeats.
     */
    public static String buildTimes(String frequency, int firstMinute, int intervalHours) {
        if (FREQ_AS_NEEDED.equals(frequency)) return "As needed";
        int n = Math.max(1, doseCountFor(frequency));
        int gap = n == 1 ? 0 : (intervalHours > 0 ? intervalHours : defaultIntervalHours(frequency));
        List<Integer> minutes = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) minutes.add((firstMinute + i * gap * 60) % 1440);
        java.util.Collections.sort(minutes);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < minutes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(DateUtils.formatMinutes(minutes.get(i)));
        }
        return sb.toString();
    }

    /** Gap in hours when the doses are evenly spaced (e.g. every 6 hours); 0 when not evenly spaced or single-dose. */
    public int getIntervalHours() {
        List<Integer> t = getDoseMinutes();
        if (t.size() < 2) return 0;
        int diff = t.get(1) - t.get(0);
        for (int i = 1; i < t.size() - 1; i++) {
            if (t.get(i + 1) - t.get(i) != diff) return 0;
        }
        return diff % 60 == 0 ? diff / 60 : 0;
    }

    /** 1-based position of a dose time among the day's doses, or 0 when unknown. */
    public int doseNumberOf(int minuteOfDay) {
        List<Integer> t = getDoseMinutes();
        int idx = t.indexOf(minuteOfDay);
        return idx < 0 ? 0 : idx + 1;
    }

    /** Doses that must be taken on a day for the streak. As-needed and every-other-day medicines are not required daily. */
    public int dosesRequiredPerDay() {
        if (isAsNeeded() || isEveryOtherDay()) return 0;
        return (int) Math.max(1, Math.round(dosesPerDay()));
    }

    /** Estimated whole days of supply left at the current dosing rate. */
    public int daysOfSupplyLeft() {
        return (int) Math.floor(remaining / dosesPerDay());
    }

    public boolean isLowStock(int thresholdDays) { return daysOfSupplyLeft() <= thresholdDays; }

    /** True when the medicine had already been added on or before the given yyyy-MM-dd date. */
    public boolean isStartedBy(String date) { return created.compareTo(date) <= 0; }

    /** Unit word matching the form, e.g. "tablets". */
    public String getUnitLabel() {
        if ("Tablet".equals(form)) return "tablets";
        if ("Capsule".equals(form)) return "capsules";
        return "doses";
    }
}
