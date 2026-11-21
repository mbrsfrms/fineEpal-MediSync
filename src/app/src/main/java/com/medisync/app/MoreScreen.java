package com.medisync.app;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.auth.GoogleSignIn;
import com.medisync.app.manager.AlarmScheduler;
import com.medisync.app.model.*;
import com.medisync.app.util.Keys;

/** More menu, logout/session handling and profile. */
abstract class MoreScreen extends StreakAboutScreens {
    void settingRow(LinearLayout parent, String icon, String title, boolean last, final Runnable r) {
        LinearLayout row = row();
        row.setPadding(dp(2), dp(13), dp(2), dp(13));
        row.addView(new IconView(this, icon, MUTED), lp(24, 24));
        TextView t = tv(title, 13, INK, false);
        t.setPadding(dp(14), 0, 0, 0);
        row.addView(t, wt(1));
        row.addView(new IconView(this, "right", MUTED), lp(19, 19));
        row.setOnClickListener(x -> r.run());
        parent.addView(row);
        if (!last) divider(parent);
    }

    void more() {
        LinearLayout b = page();
        b.addView(tv("Settings", 20, INK, true));
        sp(b, 12);
        LinearLayout pc = card(b);
        LinearLayout h = row();
        h.addView(avatarEdit(userAvatar, 3, 60, v -> { deleteAvatarFile(userAvatar, v); userAvatar = v; db.setUserAvatar(userId, v); render(); }), lp(60, 60));
        LinearLayout t = col();
        t.setPadding(dp(12), 0, 0, 0);
        t.addView(tv(userName, 15, INK, true));
        t.addView(tv(userEmail, 12, MUTED, false));
        h.addView(t, wt(1));
        h.addView(new IconView(this, "right", MUTED), lp(22, 22));
        h.setOnClickListener(x -> go("profile"));
        pc.addView(h);

        LinearLayout pt = card(b);
        settingRow(pt, "person", "Patient Profile", false, () -> go("patient"));
        settingRow(pt, "person", "My Dependents", false, () -> go("dependents"));
        settingRow(pt, "box", "Medication Supply", false, () -> go("supply"));
        settingRow(pt, "flame", "Intake Streak", true, () -> go("streak"));

        LinearLayout c = card(b);
        settingRow(c, "bell", "Notifications", false, () -> go("notifications"));
        settingRow(c, "clock", "Reminders", false, () -> go("reminders"));
        settingRow(c, "sun", "Appearance", false, () -> listDialog("Appearance", new String[]{"Light", "Dark"}, i -> {
            dark = i == 1;
            db.put("dark", dark ? "1" : "0");
            applyTheme();
            render();
        }));
        settingRow(c, "info", "About", false, () -> go("about"));
        settingRow(c, "logout", "Log Out", true, () -> confirm("Log out", "Your medications and records stay saved on this device and will be here when you sign in again with the same Google account. Reminders pause until then.", "Log Out", this::logout));
    }

    /** Ends the session without deleting any account or medication data. */
    void endSession() {
        for (Patient p : db.patients(userId)) {
            for (Medication m : db.meds(p.getId(), true)) AlarmScheduler.cancelMedication(this, m);
            for (Medication m : db.meds(p.getId(), false)) AlarmScheduler.cancelMedication(this, m);
            for (Appointment a : db.appointments(p.getId())) AlarmScheduler.cancelAppointment(this, a.getId());
        }
        db.put(Keys.USER_ID, "");
        userId = -1; patientId = -1; userName = ""; userEmail = ""; userAvatar = "";
        googleSession = false;
        // state first, then Firebase, so the auth-state listener sees that nobody is signed in here
        GoogleSignIn.signOut(this);
    }

    void logout() {
        endSession();
        loginMsg = "";
        signingIn = false;
        tab("login");
    }

    /** Used when Firebase reports that the saved session is no longer valid. */
    void forceSignOut(String message) {
        if (userId < 0) return;
        endSession();
        signingIn = false;
        loginMsg = message;
        loginMsgIsError = true;
        tab("login");
    }

    void profile() {
        LinearLayout b = page();
        Patient p = curPatient();
        topBar(b, p == null ? userName : p.getName(), "Profile", p);
        LinearLayout c = card(b);
        settingRow(c, "person", "Personal Information", false, () -> go("personal"));
        if (db.hasPassword(userId)) settingRow(c, "lock", "Change Password", false, () -> go("password"));
        settingRow(c, "help", "Help & Support", false, () -> go("help"));
        settingRow(c, "shield", "Privacy Policy", false, () -> go("privacy"));
        settingRow(c, "doc", "Terms of Service", true, () -> go("terms"));
    }
}
