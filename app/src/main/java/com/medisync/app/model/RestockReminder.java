package com.medisync.app.model;

/** Reminder that a medicine is running low and should be restocked. */
public class RestockReminder extends Reminder {
    private final String medicationName;
    private final int daysLeft;
    private final int remaining;
    private final String unit;

    public RestockReminder(String key, long patientId, String medicationName, int remaining, int daysLeft, String unit) {
        super(key, patientId, medicationName, daysLeft <= 1 ? "Running out" : daysLeft + " days left");
        this.medicationName = medicationName;
        this.remaining = remaining;
        this.daysLeft = daysLeft;
        this.unit = unit == null ? "doses" : unit;
    }

    @Override
    public Type getType() { return Type.RESTOCK; }

    public int getDaysLeft() { return daysLeft; }

    @Override
    public String buildMessage() {
        if (daysLeft <= 0) return medicationName + " is out of stock. Restock now.";
        return "Low supply: " + medicationName + " has " + remaining + " " + unit + " left (about "
                + daysLeft + (daysLeft == 1 ? " day" : " days") + ").";
    }
}
