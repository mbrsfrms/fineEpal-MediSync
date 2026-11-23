package com.medisync.app.model;

/** Outcome of one scheduled dose. Stored values are kept compatible with the earlier app ("Taken", "Missed", "Skipped"). */
public enum IntakeStatus {
    ON_TIME("Taken"),
    LATE("Late"),
    MISSED("Missed"),
    SKIPPED("Skipped");

    /** A dose taken more than this many minutes after its scheduled time counts as late. */
    public static final int LATE_AFTER_MINUTES = 60;

    private final String stored;

    IntakeStatus(String stored) { this.stored = stored; }

    public String getStoredValue() { return stored; }

    public boolean countsAsTaken() { return this == ON_TIME || this == LATE; }

    /** Maps the stored text to a status; returns null for unknown text. */
    public static IntakeStatus fromStored(String value) {
        if (value == null) return null;
        for (IntakeStatus s : values()) {
            if (s.stored.equalsIgnoreCase(value)) return s;
        }
        return null;
    }

    /** Classifies a taken dose by comparing the actual minute with the scheduled minute. */
    public static IntakeStatus classifyTaken(int scheduledMinute, int actualMinute) {
        return actualMinute - scheduledMinute > LATE_AFTER_MINUTES ? LATE : ON_TIME;
    }
}
