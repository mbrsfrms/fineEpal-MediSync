package com.medisync.app.model;

import com.medisync.app.util.DateUtils;

/** A follow-up or medical appointment for one patient. */
public class Appointment {
    public enum Status {
        SCHEDULED("Scheduled"), COMPLETED("Completed"), CANCELLED("Cancelled");

        private final String label;

        Status(String label) { this.label = label; }

        public String getLabel() { return label; }

        public static Status fromStored(String value) {
            for (Status s : values()) if (s.name().equalsIgnoreCase(value)) return s;
            return SCHEDULED;
        }
    }

    private final long id;
    private final long patientId;
    private String title;
    private String doctor;
    private String date;
    private String time;
    private String notes;
    private Status status;

    public Appointment(long id, long patientId, String title, String doctor, String date, String time,
                       String notes, Status status) {
        this.id = id;
        this.patientId = patientId;
        setTitle(title);
        setDoctor(doctor);
        setSchedule(date, time);
        setNotes(notes);
        this.status = status == null ? Status.SCHEDULED : status;
    }

    public long getId() { return id; }

    public long getPatientId() { return patientId; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Appointment title is required.");
        this.title = title.trim();
    }

    public String getDoctor() { return doctor; }

    public void setDoctor(String doctor) { this.doctor = doctor == null ? "" : doctor.trim(); }

    /** Date as yyyy-MM-dd. */
    public String getDate() { return date; }

    /** Time as HH:mm (24-hour). */
    public String getTime() { return time; }

    /** Validates and sets date and time together. */
    public void setSchedule(String date, String time) {
        if (DateUtils.parseToMillis(date) == null) throw new IllegalArgumentException("Choose a valid date.");
        if (DateUtils.minutesOf24h(time) < 0) throw new IllegalArgumentException("Choose a valid time.");
        this.date = date.trim();
        this.time = time.trim();
    }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes == null ? "" : notes.trim(); }

    public Status getStatus() { return status; }

    public long getStartMillis() {
        return DateUtils.millisAt(date, DateUtils.minutesOf24h(time));
    }

    public boolean isScheduled() { return status == Status.SCHEDULED; }

    /** Overdue: still marked scheduled after its date and time have passed. Completed and cancelled ones never are. */
    public boolean isOverdue(long nowMillis) {
        return status == Status.SCHEDULED && getStartMillis() < nowMillis;
    }

    /** Marks the appointment completed. Only a scheduled appointment can be completed. */
    public void markCompleted() {
        if (status != Status.SCHEDULED) throw new IllegalStateException("Only a scheduled appointment can be completed.");
        status = Status.COMPLETED;
    }

    /** Cancels the appointment. Only a scheduled appointment can be cancelled. */
    public void cancel() {
        if (status != Status.SCHEDULED) throw new IllegalStateException("Only a scheduled appointment can be cancelled.");
        status = Status.CANCELLED;
    }

    /** Reopens a completed or cancelled appointment (used when the user corrects a mistake). */
    public void reopen() { status = Status.SCHEDULED; }
}
