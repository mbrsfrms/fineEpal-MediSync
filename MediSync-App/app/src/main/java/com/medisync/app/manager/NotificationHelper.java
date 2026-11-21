package com.medisync.app.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.medisync.app.MainActivity;
import com.medisync.app.receiver.ReminderReceiver;

/** Creates notification channels and posts MediSync notifications. */
public final class NotificationHelper {
    public static final String CHANNEL_DOSES = "medisync_doses";
    public static final String CHANNEL_APPOINTMENTS = "medisync_appointments";
    public static final String CHANNEL_SUPPLY = "medisync_supply";

    /** Extra used by MainActivity to open a specific screen from a notification. */
    public static final String EXTRA_OPEN = "open_screen";

    private NotificationHelper() { }

    public static void createChannels(Context c) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        nm.createNotificationChannel(new NotificationChannel(CHANNEL_DOSES, "Medication doses",
                NotificationManager.IMPORTANCE_HIGH));
        nm.createNotificationChannel(new NotificationChannel(CHANNEL_APPOINTMENTS, "Appointments",
                NotificationManager.IMPORTANCE_DEFAULT));
        nm.createNotificationChannel(new NotificationChannel(CHANNEL_SUPPLY, "Low supply and refills",
                NotificationManager.IMPORTANCE_DEFAULT));
    }

    /** True when the app may show notifications (runtime permission on Android 13+, and not blocked in settings). */
    public static boolean canNotify(Context c) {
        if (Build.VERSION.SDK_INT >= 33 && c.checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        return nm != null && (Build.VERSION.SDK_INT < 24 || nm.areNotificationsEnabled());
    }

    /** Dose reminder with a "Taken" action and a tap-to-open action. */
    public static void showDose(Context c, int notificationId, String title, String text,
                                long patientId, long medId, int minuteOfDay) {
        Intent taken = new Intent(c, ReminderReceiver.class)
                .setAction(ReminderReceiver.ACTION_TAKEN)
                .putExtra(AlarmScheduler.EXTRA_PATIENT_ID, patientId)
                .putExtra(AlarmScheduler.EXTRA_MED_ID, medId)
                .putExtra(AlarmScheduler.EXTRA_MINUTE, minuteOfDay)
                .putExtra(AlarmScheduler.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent takenPi = PendingIntent.getBroadcast(c, notificationId, taken,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        post(c, notificationId, CHANNEL_DOSES, title, text, "meds", takenPi, "Mark taken");
    }

    public static void showAppointment(Context c, int notificationId, String title, String text) {
        post(c, notificationId, CHANNEL_APPOINTMENTS, title, text, "appointments", null, null);
    }

    public static void showSupply(Context c, int notificationId, String title, String text) {
        post(c, notificationId, CHANNEL_SUPPLY, title, text, "meds", null, null);
    }

    public static void cancel(Context c, int notificationId) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(notificationId);
    }

    private static void post(Context c, int id, String channel, String title, String text, String openScreen,
                             PendingIntent actionPi, String actionLabel) {
        if (!canNotify(c)) return;
        createChannels(c);
        Intent open = new Intent(c, MainActivity.class)
                .putExtra(EXTRA_OPEN, openScreen)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPi = PendingIntent.getActivity(c, id, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder nb = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(c, channel) : new Notification.Builder(c);
        nb.setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(contentPi)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_REMINDER);
        if (actionPi != null && actionLabel != null) nb.addAction(android.R.drawable.checkbox_on_background, actionLabel, actionPi);
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(id, nb.build());
    }
}
