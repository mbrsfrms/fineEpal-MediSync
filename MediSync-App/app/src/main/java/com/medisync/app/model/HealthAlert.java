package com.medisync.app.model;

/** A health risk flag shown on the dashboard (allergy match, overdue appointment, low supply). */
public class HealthAlert {
    public enum Kind { ALLERGY, OVERDUE_APPOINTMENT, LOW_STOCK }

    public enum Severity { INFO, WARNING, DANGER }

    private final Kind kind;
    private final Severity severity;
    private final String message;

    public HealthAlert(Kind kind, Severity severity, String message) {
        this.kind = kind;
        this.severity = severity;
        this.message = message == null ? "" : message;
    }

    public Kind getKind() { return kind; }

    public Severity getSeverity() { return severity; }

    public String getMessage() { return message; }
}
