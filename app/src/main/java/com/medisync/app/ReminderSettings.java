package com.medisync.app;

import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.medisync.app.model.*;

/** Reminder and notification settings screens. */
abstract class ReminderSettings extends DependentScreens {
    void remRow(LinearLayout parent, final long pid, String icon, String title, final String key, boolean def) {
        LinearLayout r = row();
        r.setPadding(dp(2), dp(10), dp(2), dp(10));
        FrameLayout circ = new FrameLayout(this);
        GradientDrawable og = new GradientDrawable();
        og.setShape(GradientDrawable.OVAL);
        og.setColor(MINT);
        circ.setBackground(og);
        circ.addView(new IconView(this, icon, TEAL), new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER));
        r.addView(circ, lp(38, 38));
        LinearLayout t = col();
        t.setPadding(dp(12), 0, 0, 0);
        t.addView(tv(title, 13, INK, true));
        boolean on = "1".equals(db.get(remKey(pid, key), def ? "1" : "0"));
        final TextView st = tv(on ? "On" : "Off", 11, MUTED, false);
        t.addView(st);
        r.addView(t, wt(1));
        Switch s = sw(on);
        s.setOnCheckedChangeListener((cb, v) -> {
            db.put(remKey(pid, key), v ? "1" : "0");
            st.setText(v ? "On" : "Off");
            scheduleAll();
            if (v) askNotif();
        });
        r.addView(s);
        parent.addView(r);
        divider(parent);
    }

    void reminders() {
        LinearLayout b = page();
        final Patient p = needPatient(b);
        if (p == null) return;
        topBar(b, p.getName(), "Reminders", p);
        LinearLayout c = card(b);
        remRow(c, p.getId(), "checkc", "Medication Reminders", "med", true);
        remRow(c, p.getId(), "box", "Low Supply Alert", "low", true);
        remRow(c, p.getId(), "pill", "Refill Reminder", "refill", true);
        remRow(c, p.getId(), "bell", "Appointment Reminders", "follow", true);
        sp(c, 8);
        c.addView(tv("Each medicine alarm rings at its own dose times, set in the medicine schedule. "
                        + "Appointment reminders ring one hour before the appointment. Alarms are restored after a restart.",
                11, MUTED, false));
    }

    void notifications() {
        LinearLayout b = page();
        topBar(b, "Notifications", null, null);
        LinearLayout c = card(b);
        LinearLayout r = row();
        LinearLayout t = col();
        t.addView(tv("Push notifications", 13, INK, true));
        t.addView(tv("Medication, supply and refill alerts", 11, MUTED, false));
        r.addView(t, wt(1));
        Switch s = sw("1".equals(db.get("notif_on", "1")));
        s.setOnCheckedChangeListener((cb, v) -> {
            db.put("notif_on", v ? "1" : "0");
            scheduleAll();
            if (v) askNotif();
        });
        r.addView(s);
        c.addView(r);
        sp(c, 6);
        btn(c, "Send test notification", () -> {
            askNotif();
            postNotification(this, 9001, "MediSync", "This is a test reminder. Notifications are working!");
            toast("Test notification sent.");
        });
    }
}
