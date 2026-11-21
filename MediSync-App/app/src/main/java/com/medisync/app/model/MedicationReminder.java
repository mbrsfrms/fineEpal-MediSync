package com.medisync.app.model;

/** Reminder that a scheduled dose is due. */
public class MedicationReminder extends Reminder {
    private final String medicationName;
    private final String dosage;
    private final int doseNumber;
    private final int dosesPerDay;

    public MedicationReminder(String key, long patientId, String medicationName, String dosage, String timeLabel) {
        this(key, patientId, medicationName, dosage, timeLabel, 0, 0);
    }

    /** @param doseNumber 1-based dose of the day (0 when unknown); @param dosesPerDay total doses that day */
    public MedicationReminder(String key, long patientId, String medicationName, String dosage, String timeLabel,
                              int doseNumber, int dosesPerDay) {
        super(key, patientId, medicationName, timeLabel);
        this.medicationName = medicationName;
        this.dosage = dosage == null ? "" : dosage;
        this.doseNumber = doseNumber;
        this.dosesPerDay = dosesPerDay;
    }

    @Override
    public Type getType() { return Type.MEDICATION; }

    @Override
    public String buildMessage() {
        return "Time to take " + medicationName + (dosage.isEmpty() ? "" : " " + dosage)
                + (getWhen().isEmpty() ? "" : " (" + getWhen() + ")")
                + (doseNumber > 0 && dosesPerDay > 1 ? " \u00B7 Dose " + doseNumber + " of " + dosesPerDay : "")
                + ".";
    }
}
