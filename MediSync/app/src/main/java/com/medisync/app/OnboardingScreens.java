package com.medisync.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.model.*;
import java.util.Date;

/** Role and care-type selection screens. */
abstract class OnboardingScreens extends LoginScreens {
    void roleCard(LinearLayout b, String icon, String title, String desc, final Runnable r) {
        LinearLayout c = row();
        c.setPadding(dp(14), dp(16), dp(14), dp(16));
        c.setBackground(rr(CARD, BORDER, 14));
        FrameLayout circ = new FrameLayout(this);
        GradientDrawable oval = new GradientDrawable();
        oval.setShape(GradientDrawable.OVAL);
        oval.setColor(TEAL);
        circ.setBackground(oval);
        circ.addView(new IconView(this, icon, Color.WHITE), new FrameLayout.LayoutParams(dp(31), dp(31), Gravity.CENTER));
        c.addView(circ, lp(52, 52));
        LinearLayout t = col();
        t.setPadding(dp(14), 0, dp(8), 0);
        t.addView(tv(title, 15, INK, true));
        t.addView(tv(desc, 12, MUTED, false));
        c.addView(t, wt(1));
        c.addView(new IconView(this, "right", MUTED), lp(22, 22));
        c.setOnClickListener(x -> r.run());
        LinearLayout.LayoutParams p = lp(-1, -2);
        p.bottomMargin = dp(14);
        b.addView(c, p);
    }

    void role() {
        LinearLayout b = wavePage();
        sp(b, 20);
        b.addView(tv("How will you use\nMediSync?", 24, INK, true));
        TextView s = tv("Choose how you'll be using the app. Welcome, " + firstName(userName) + "!", 13, MUTED, false);
        s.setPadding(0, dp(6), 0, dp(22));
        b.addView(s);
        roleCard(b, "person", "Personal medication management", "Manage your own medications and health.", () -> {
            patientId = db.addPatient(userId, userName, "Adult", "Self", "", userAvatar);
            db.put("patient_id", "" + patientId);
            tab("home");
            scheduleAll();
            askNotif();
        });
        roleCard(b, "person", "Parent / Guardian", "Manage a child's medications (aged 10 or below).", () -> {
            addingDependent = false; careType = "Child"; go("caretype");
        });
        roleCard(b, "person", "Caretaker", "Manage an elderly patient or dependent.", () -> {
            addingDependent = false; careType = "Elderly"; go("caretype");
        });
        sp(b, 4);
        linkRow(b, "Signed in as " + userEmail + ".", "Use a different account", this::logout);
    }

    static String firstName(String full) {
        String n = full == null ? "" : full.trim();
        int i = n.indexOf(' ');
        return i > 0 ? n.substring(0, i) : n;
    }

    void careCard(LinearLayout b, final String type, String title, String sub, int av) {
        boolean on = careType.equals(type);
        LinearLayout c = row();
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable g = rr(CARD, on ? TEAL : BORDER, 14);
        if (on) g.setStroke(dp(2), TEAL);
        c.setBackground(g);
        c.addView(new AvatarView(this, "", av), lp(50, 50));
        LinearLayout t = col();
        t.setPadding(dp(12), 0, dp(8), 0);
        t.addView(tv(title, 14, INK, true));
        if (sub != null) t.addView(tv(sub, 11, MUTED, false));
        c.addView(t, wt(1));
        c.addView(new IconView(this, on ? "check" : "right", on ? TEAL : MUTED), lp(22, 22));
        c.setOnClickListener(x -> { careType = type; render(); });
        LinearLayout.LayoutParams p = lp(-1, -2);
        p.bottomMargin = dp(10);
        b.addView(c, p);
    }

    void caretype() {
        LinearLayout b = wavePage();
        IconView back = new IconView(this, "back", INK);
        back.setOnClickListener(x -> back());
        b.addView(back, lp(30, 30));
        sp(b, 8);
        b.addView(tv("Who are you caring for?", 22, INK, true));
        TextView s = tv("Select the type of person you are managing.", 12, MUTED, false);
        s.setPadding(0, dp(4), 0, dp(16));
        b.addView(s);
        careCard(b, "Child", "Child", "(10 years old or below)", 0);
        careCard(b, "Elderly", "Elderly patient", null, 2);
        sp(b, 8);
        LinearLayout.LayoutParams nap = lp(96, 96);
        nap.gravity = Gravity.CENTER_HORIZONTAL;
        b.addView(avatarEdit(newAvatar, careType.equals("Elderly") ? 2 : 0, 96, v -> { deleteAvatarFile(newAvatar, v); newAvatar = v; }), nap);
        TextView nat = tv("Tap to choose a photo", 12, MUTED, false);
        nat.setGravity(Gravity.CENTER);
        nat.setPadding(0, dp(6), 0, dp(10));
        b.addView(nat);
        lbl(b, "Patient Name");
        final EditText nm = input(b, "e.g. Juan Dela Cruz", null, false);
        lbl(b, "Relationship");
        final AppState.Sel rel = select(b, "Select relationship", RELS, null);
        lbl(b, "Date of Birth (optional)");
        final AppState.Sel dob = dateSelect(b, "Select date of birth", null);
        btn(b, "Continue", () -> {
            String n = nm.getText().toString().trim();
            if (n.isEmpty()) { toast("Enter the patient's name."); return; }
            long id = db.addPatient(userId, n, careType, rel.v.isEmpty() ? "Dependent" : rel.v, dob.v, newAvatar);
            newAvatar = "";
            patientId = id;
            db.put("patient_id", "" + id);
            scheduleAll();
            if (addingDependent) { addingDependent = false; toast("Added " + n); back(); }
            else { tab("home"); askNotif(); }
        });
    }
}
