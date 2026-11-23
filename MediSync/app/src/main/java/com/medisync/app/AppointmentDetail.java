package com.medisync.app;

import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.database.MediSyncDatabase;
import com.medisync.app.manager.AlarmScheduler;
import com.medisync.app.model.Appointment;
import com.medisync.app.util.DateUtils;

/** Appointment detail dialog and its actions (complete, cancel, reopen, delete). */
final class AppointmentDetail {
    private AppointmentDetail() { }

    /** Detail dialog with the actions allowed for the appointment's current status. */
    static void showDetail(final MainActivity host, final Appointment a) {
        final MediSyncDatabase db = host.db;
        final android.app.AlertDialog[] dd = new android.app.AlertDialog[1];
        LinearLayout body = host.col();
        body.addView(host.tv(DateUtils.formatDisplayDate(a.getDate()) + " · "
                + DateUtils.formatMinutes(DateUtils.minutesOf24h(a.getTime())), 13, host.INK, true));
        if (!a.getDoctor().isEmpty()) body.addView(host.tv(a.getDoctor(), 12, host.MUTED, false));
        String status = a.isOverdue(System.currentTimeMillis()) ? "Overdue (not marked completed or cancelled)"
                : a.getStatus().getLabel();
        body.addView(host.tv("Status: " + status, 12, host.MUTED, false));
        if (!a.getNotes().isEmpty()) body.addView(host.tv("Notes: " + a.getNotes(), 12, host.MUTED, false));
        host.sp(body, 12);

        if (a.isScheduled()) {
            action(host, body, "Mark as completed", host.GREEN, () -> {
                db.setAppointmentStatus(a.getId(), Appointment.Status.COMPLETED);
                AlarmScheduler.cancelAppointment(host, a.getId());
                host.toast("Appointment marked completed.");
                dd[0].dismiss();
                host.render();
            }, dd);
            action(host, body, "Cancel appointment", host.ORANGE, () -> {
                db.setAppointmentStatus(a.getId(), Appointment.Status.CANCELLED);
                AlarmScheduler.cancelAppointment(host, a.getId());
                host.toast("Appointment cancelled.");
                dd[0].dismiss();
                host.render();
            }, dd);
        } else {
            action(host, body, "Reopen as scheduled", host.TEAL, () -> {
                db.setAppointmentStatus(a.getId(), Appointment.Status.SCHEDULED);
                host.scheduleAll();
                host.toast("Appointment reopened.");
                dd[0].dismiss();
                host.render();
            }, dd);
        }
        action(host, body, "Edit", host.TEAL, () -> {
            host.editingAppointmentId = a.getId();
            dd[0].dismiss();
            host.go("appointment_form");
        }, dd);
        action(host, body, "Delete", host.RED, () -> {
            dd[0].dismiss();
            host.confirm("Delete appointment", "Delete \"" + a.getTitle() + "\"? This cannot be undone.", "Delete", () -> {
                AlarmScheduler.cancelAppointment(host, a.getId());
                db.deleteAppointment(a.getId());
                host.toast("Appointment deleted.");
                host.render();
            });
        }, dd);
        dd[0] = host.dialog(a.getTitle(), body, null, null, "Close");
    }

    static void action(MainActivity host, LinearLayout body, String label, int color, Runnable r,
                               final android.app.AlertDialog[] dd) {
        TextView t = host.tv(label, 13, color, false);
        t.setPadding(host.dp(2), host.dp(10), 0, host.dp(10));
        t.setGravity(Gravity.START);
        t.setOnClickListener(x -> r.run());
        body.addView(t);
        host.divider(body);
    }

    static int[] ymd(String date) {
        Long millis = DateUtils.parseToMillis(date);
        java.util.Calendar c = java.util.Calendar.getInstance();
        if (millis != null) c.setTimeInMillis(millis);
        return new int[]{c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH)};
    }
}
