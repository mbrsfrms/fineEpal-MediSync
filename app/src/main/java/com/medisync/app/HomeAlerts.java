package com.medisync.app;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.manager.HealthAlertService;
import com.medisync.app.manager.ReminderService;
import com.medisync.app.model.*;
import com.medisync.app.util.DateUtils;
import java.util.Calendar;
import java.util.List;

/** Home screen alerts: health alerts, allergy card and opening pending reminders. */
abstract class HomeAlerts extends OnboardingScreens {
    int alertColor(HealthAlert a) {
        if (a.getSeverity() == HealthAlert.Severity.DANGER) return RED;
        if (a.getSeverity() == HealthAlert.Severity.WARNING) return ORANGE;
        return TEAL;
    }

    /** Dashboard sections: LifeSync Calendar and appointment links, upcoming appointments, and health alerts. */
    void homeExtras(LinearLayout b, final Patient p, List<Medication> ms) {
        LinearLayout ql = card(b);
        linkRow2(ql, "calendar", "LifeSync Calendar", false, () -> go("calendar"));
        linkRow2(ql, "clock", "Appointments", true, () -> go("appointments"));

        LinearLayout uc = card(b);
        uc.addView(tv("Upcoming Appointments", 14, INK, true));
        sp(uc, 6);
        long now = System.currentTimeMillis();
        int shown = 0;
        for (Appointment a : db.appointments(p.getId())) {
            if (!a.isScheduled()) continue;
            if (shown++ >= 3) break;
            boolean overdue = a.isOverdue(now);
            TextView t = tv((overdue ? "Overdue · " : "") + a.getTitle() + "  ·  "
                            + DateUtils.formatDisplayDate(a.getDate()) + " "
                            + DateUtils.formatMinutes(DateUtils.minutesOf24h(a.getTime())),
                    12, overdue ? RED : INK, overdue);
            t.setPadding(0, dp(4), 0, dp(4));
            uc.addView(t);
        }
        if (shown == 0) uc.addView(tv("No scheduled appointments yet.", 12, MUTED, false));

        // reminders: every Reminder subclass is handled through the abstract Reminder type
        List<Reminder> reminders = ReminderService.build(p.getId(), ms, db.history(p.getId()),
                db.appointments(p.getId()), DateUtils.today(), DateUtils.nowMinutes(), now);
        LinearLayout rc = card(b);
        rc.addView(tv("Reminders", 14, INK, true));
        sp(rc, 6);
        int shownReminders = 0;
        for (final Reminder r : reminders) {
            if (db.isReminderDismissed(r.getKey())) continue;
            shownReminders++;
            LinearLayout line = row();
            line.setPadding(0, dp(4), 0, dp(4));
            line.addView(tv(r.describe(), 12, INK, false), wt(1));
            TextView dismiss = tv("Dismiss", 12, TEAL, true);
            dismiss.setPadding(dp(8), dp(4), 0, dp(4));
            dismiss.setOnClickListener(x -> {
                db.dismissReminder(r.getKey());
                render();
            });
            line.addView(dismiss);
            rc.addView(line);
        }
        if (shownReminders == 0) rc.addView(tv("No reminders right now.", 12, MUTED, false));

        final List<HealthAlert> alerts = HealthAlertService.build(p, ms, db.appointments(p.getId()), now);
        LinearLayout ac = card(b);
        ac.addView(tv("Health Alerts", 14, INK, true));
        sp(ac, 6);
        if (alerts.isEmpty()) ac.addView(tv("No alerts. Allergies, supply and appointments look fine.", 12, MUTED, false));
        for (int i = 0; i < alerts.size() && i < 4; i++) {
            HealthAlert a = alerts.get(i);
            TextView t = tv("●  " + a.getMessage(), 12, alertColor(a), false);
            t.setPadding(0, dp(3), 0, dp(3));
            ac.addView(t);
        }
        if (alerts.size() > 4) {
            TextView all = tv("View all " + alerts.size() + " alerts", 12, TEAL, true);
            all.setPadding(0, dp(6), 0, 0);
            all.setOnClickListener(x -> showAllAlerts(alerts));
            ac.addView(all);
        }
    }

    void showAllAlerts(List<HealthAlert> alerts) {
        LinearLayout body = col();
        for (HealthAlert a : alerts) {
            TextView t = tv("●  " + a.getMessage(), 13, alertColor(a), false);
            t.setPadding(0, dp(6), 0, dp(6));
            body.addView(t);
        }
        dialog("Health Alerts", body, null, null, "Close");
    }

    /** Patient allergies, shown in red on the dashboard alerts when they match a medicine. */
    void allergyCard(LinearLayout b, final Patient p) {
        LinearLayout c = card(b);
        LinearLayout r = row();
        LinearLayout t = col();
        t.addView(tv("Allergies", 13, INK, true));
        t.addView(tv(p.hasAllergies() ? p.getAllergies() : "None recorded. Tap to add.", 12,
                p.hasAllergies() ? RED : MUTED, false));
        r.addView(t, wt(1));
        r.addView(new IconView(this, "pencil", TEAL), lp(20, 20));
        r.setOnClickListener(x -> {
            LinearLayout body = col();
            final EditText e = input(body, "e.g. Penicillin, peanuts", null, false);
            e.setText(p.getAllergies());
            dialog("Allergies", body, "Save", () -> {
                db.updatePatientAllergies(p.getId(), e.getText().toString());
                toast("Allergies saved.");
                render();
                return true;
            }, "Cancel");
        });
        c.addView(r);
    }

    /** Opens the screen a notification asked for, once the user is signed in. */
    void openPending() {
        if (pendingOpen == null || userId < 0) return;
        String o = pendingOpen;
        pendingOpen = null;
        if ("appointments".equals(o)) tab("appointments");
        else if ("meds".equals(o)) tab("meds");
    }
}
