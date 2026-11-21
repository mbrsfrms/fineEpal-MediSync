package com.medisync.app.util;

/** Names of settings stored in the database's settings table. */
public final class Keys {
    public static final String NOTIFICATIONS_ON = "notif_on";
    public static final String USER_ID = "user_id";
    public static final String PATIENT_ID = "patient_id";
    public static final String DARK_MODE = "dark";

    private Keys() { }

    /** Per-patient reminder switch: med, low, refill or follow. */
    public static String remKey(long patientId, String name) {
        return "rem_" + name + "_" + patientId;
    }
}
