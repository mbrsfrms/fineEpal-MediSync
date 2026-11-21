package com.medisync.app.model;

/**
 * Abstract reminder. Medication, restock and appointment reminders share this contract, so the
 * notification and dashboard code can handle any reminder through a Reminder reference
 * (polymorphism) while each subclass supplies its own message.
 */
public abstract class Reminder {
    public enum Type {
        MEDICATION("Medication"), RESTOCK("Restock"), APPOINTMENT("Appointment");

        private final String label;

        Type(String label) { this.label = label; }

        public String getLabel() { return label; }
    }

    private final String key;
    private final long patientId;
    private final String title;
    private final String when;

    /**
     * @param key       stable identifier including the day, so dismissing today's reminder does not hide tomorrow's
     * @param when      human readable time or date shown with the reminder
     */
    protected Reminder(String key, long patientId, String title, String when) {
        if (key == null || key.isEmpty()) throw new IllegalArgumentException("Reminder key is required.");
        this.key = key;
        this.patientId = patientId;
        this.title = title == null ? "" : title;
        this.when = when == null ? "" : when;
    }

    public String getKey() { return key; }

    public long getPatientId() { return patientId; }

    public String getTitle() { return title; }

    public String getWhen() { return when; }

    /** Stable notification id derived from the key. */
    public int getNotificationId() { return 1000 + (Math.abs(key.hashCode()) % 90000); }

    public abstract Type getType();

    /** The text shown in the notification or on the dashboard. Each subclass decides the wording. */
    public abstract String buildMessage();

    /** Short line for lists: "Medication · Take Paracetamol at 8:00 AM". */
    public String describe() {
        return getType().getLabel() + " · " + buildMessage();
    }
}
