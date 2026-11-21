package com.medisync.app.model;

/** One entry on the LifeSync calendar: a medication dose, an appointment, or a health reminder. */
public class CalendarEvent {
    public enum Type { MEDICATION, APPOINTMENT, REMINDER }

    private final String date;
    private final int minuteOfDay;
    private final String title;
    private final String detail;
    private final Type type;
    private final String status;
    private final long refId;

    /**
     * @param minuteOfDay minutes after midnight, or -1 for all-day items
     * @param refId       id of the medication or appointment this event came from (0 when none)
     */
    public CalendarEvent(String date, int minuteOfDay, String title, String detail, Type type, String status, long refId) {
        this.date = date;
        this.minuteOfDay = minuteOfDay;
        this.title = title;
        this.detail = detail == null ? "" : detail;
        this.type = type;
        this.status = status;
        this.refId = refId;
    }

    public String getDate() { return date; }

    public int getMinuteOfDay() { return minuteOfDay; }

    public String getTitle() { return title; }

    public String getDetail() { return detail; }

    public Type getType() { return type; }

    public String getStatus() { return status; }

    public long getRefId() { return refId; }
}
