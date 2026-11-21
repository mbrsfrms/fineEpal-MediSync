package com.medisync.app.model;

/** One logged dose (taken, late, missed or skipped). Immutable once written. */
public class IntakeRecord {
    private final long id;
    private final long patientId;
    private final long medicationId;
    private final String name;
    private final String dosage;
    private final String scheduledAt;
    private final String recordedAt;
    private final String status;

    /**
     * @param scheduledAt "yyyy-MM-dd HH:mm" of the dose slot, or "" for legacy rows that did not store it
     * @param recordedAt  "yyyy-MM-dd HH:mm" when the user logged the dose
     * @param status      stored status text, e.g. "Taken", "Late", "Missed", "Skipped"
     */
    public IntakeRecord(long id, long patientId, long medicationId, String name, String dosage,
                        String scheduledAt, String recordedAt, String status) {
        this.id = id;
        this.patientId = patientId;
        this.medicationId = medicationId;
        this.name = name == null ? "" : name;
        this.dosage = dosage == null ? "" : dosage;
        this.scheduledAt = scheduledAt == null ? "" : scheduledAt;
        this.recordedAt = recordedAt == null ? "" : recordedAt;
        this.status = status == null ? "" : status;
    }

    public long getId() { return id; }

    public long getPatientId() { return patientId; }

    public long getMedicationId() { return medicationId; }

    public String getName() { return name; }

    public String getDosage() { return dosage; }

    public String getScheduledAt() { return scheduledAt; }

    public String getRecordedAt() { return recordedAt; }

    public String getStatus() { return status; }

    /** The typed status, or null when the stored text is unknown. */
    public IntakeStatus getIntakeStatus() { return IntakeStatus.fromStored(status); }

    public boolean isTaken() {
        IntakeStatus s = getIntakeStatus();
        return s != null && s.countsAsTaken();
    }

    /** The calendar date (yyyy-MM-dd) this record belongs to, using the scheduled date when known. */
    public String getDate() {
        String source = scheduledAt.isEmpty() ? recordedAt : scheduledAt;
        return source.length() >= 10 ? source.substring(0, 10) : "";
    }
}
