package com.medisync.app;

import android.app.DatePickerDialog;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.model.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Patient overview screen. */
abstract class PatientScreen extends HistoryScreen {
    void patientScreen() {
        LinearLayout b = page();
        final Patient p = needPatient(b);
        if (p == null) return;
        topBar(b, "Patient Profile", null, null);

        LinearLayout c = card(b);
        LinearLayout h = row();
        h.addView(avatarEdit(p.getAvatar(), avType(p), 68, v -> { deleteAvatarFile(p.getAvatar(), v); db.setPatientAvatar(p.getId(), v); render(); }), lp(68, 68));
        LinearLayout t = col();
        t.setPadding(dp(12), 0, 0, 0);
        t.addView(tv(p.getName(), 16, INK, true));
        t.addView(tv(sub(p), 12, MUTED, false));
        h.addView(t, wt(1));
        c.addView(h);
        sp(c, 12);
        divider(c);
        LinearLayout rr1 = row();
        rr1.setPadding(0, dp(10), 0, dp(10));
        LinearLayout rl = col();
        rl.addView(tv("Relationship", 11, MUTED, false));
        rl.addView(tv(p.getRelationship(), 14, INK, true));
        rr1.addView(rl, wt(1));
        IconView pen = new IconView(this, "pencil", TEAL);
        rr1.addView(pen, lp(20, 20));
        rr1.setOnClickListener(x -> listDialog("Relationship", RELS, i -> { db.updatePatient(p.getId(), RELS[i], p.getDateOfBirth()); render(); }));
        c.addView(rr1);
        divider(c);
        LinearLayout dr = row();
        dr.setPadding(0, dp(10), 0, dp(4));
        LinearLayout dl = col();
        dl.addView(tv("Date of Birth", 11, MUTED, false));
        dl.addView(tv(p.getDateOfBirth() == null || p.getDateOfBirth().isEmpty() ? "Tap to set" : fmtDate(p.getDateOfBirth()), 14, INK, true));
        dr.addView(dl, wt(1));
        dr.addView(new IconView(this, "pencil", TEAL), lp(24, 24));
        dr.setOnClickListener(x -> {
            Calendar cc = Calendar.getInstance();
            DatePickerDialog d = new DatePickerDialog(this, (pk, y, m, dd) -> {
                db.updatePatient(p.getId(), p.getRelationship(), String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, dd));
                render();
            }, cc.get(Calendar.YEAR) - 8, cc.get(Calendar.MONTH), cc.get(Calendar.DAY_OF_MONTH));
            d.getDatePicker().setMaxDate(System.currentTimeMillis());
            d.show();
        });
        c.addView(dr);

        List<Medication> act = db.meds(p.getId(), true), ina = db.meds(p.getId(), false);
        LinearLayout ov = card(b);
        LinearLayout oh = row();
        oh.addView(new IconView(this, "pill", TEAL), lp(24, 24));
        TextView ot = tv("Medication Overview", 14, INK, true);
        ot.setPadding(dp(8), 0, 0, 0);
        oh.addView(ot);
        ov.addView(oh);
        sp(ov, 8);
        ov.addView(tv(act.size() + " active medication" + (act.size() == 1 ? "" : "s") + "\n" + ina.size() + " completed/inactive", 12, MUTED, false));
        sp(ov, 10);
        LinearLayout tiles = row();
        String[] nd = nextDose(act);
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, -1, 1);
        l1.rightMargin = dp(8);
        tiles.addView(tile("checkfill", TEAL, "Next Dose", nd == null ? "None" : nd[0], INK, nd == null ? "" : nd[1]), l1);
        int minDays = Integer.MAX_VALUE;
        for (Medication m : act) minDays = Math.min(minDays, daysLeft(m));
        String sv = act.isEmpty() ? "No stock" : (minDays <= 3 ? "Low (" + minDays + " days)" : "Good");
        tiles.addView(tile("box", TEAL, "Supply", sv, minDays <= 3 && !act.isEmpty() ? ORANGE : GREEN, null), new LinearLayout.LayoutParams(0, -1, 1));
        ov.addView(tiles);

        LinearLayout lk = card(b);
        linkRow2(lk, "calendar", "Upcoming Doses", false, () -> go("upcoming"));
        linkRow2(lk, "doc", "Intake History", false, () -> tab("history"));
        linkRow2(lk, "box", "Supply / Low Stock", false, () -> go("supply"));
        linkRow2(lk, "bell", "Reminders", true, () -> go("reminders"));

        allergyCard(b, p);
        if (db.patients(userId).size() > 1) {
            TextView del = tv("Remove this patient", 12, RED, true);
            del.setGravity(Gravity.CENTER);
            del.setPadding(0, dp(12), 0, dp(12));
            del.setOnClickListener(x -> confirm("Remove patient", "Remove " + p.getName() + " and all their medications and history?", "Remove", () -> {
                deleteAvatarFile(p.getAvatar(), "");
                db.deletePatient(p.getId());
                patientId = -1;
                curPatient();
                toast("Patient removed.");
                scheduleAll();
                tab("home");
            }));
            b.addView(del);
        }
    }

    void linkRow2(LinearLayout parent, String icon, String title, boolean last, final Runnable r) {
        LinearLayout row = row();
        row.setPadding(dp(2), dp(13), dp(2), dp(13));
        row.addView(new IconView(this, icon, TEAL), lp(24, 24));
        TextView t = tv(title, 13, INK, false);
        t.setPadding(dp(14), 0, 0, 0);
        row.addView(t, wt(1));
        row.addView(new IconView(this, "right", MUTED), lp(19, 19));
        row.setOnClickListener(x -> r.run());
        parent.addView(row);
        if (!last) divider(parent);
    }
}
