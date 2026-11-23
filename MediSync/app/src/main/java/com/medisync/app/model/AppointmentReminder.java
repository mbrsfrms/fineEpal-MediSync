package com.medisync.app.model;

/** Reminder for an upcoming appointment. */
public class AppointmentReminder extends Reminder {
    private final String doctor;

    public AppointmentReminder(String key, long patientId, String title, String doctor, String whenLabel) {
        super(key, patientId, title, whenLabel);
        this.doctor = doctor == null ? "" : doctor;
    }

    @Override
    public Type getType() { return Type.APPOINTMENT; }

    @Override
    public String buildMessage() {
        return "Upcoming appointment: " + getTitle() + (doctor.isEmpty() ? "" : " with " + doctor)
                + (getWhen().isEmpty() ? "." : ", " + getWhen() + ".");
    }
}
