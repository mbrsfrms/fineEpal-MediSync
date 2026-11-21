package com.medisync.app;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.manager.HealthAlertService;
import com.medisync.app.model.*;
import java.util.List;

/** Home dashboard and medication row. */
abstract class HomeScreen extends HomeStreak {
    View tile(String icon, int iconColor, String title, String val, int valColor, String subtext) {
        LinearLayout t = row();
        t.setPadding(dp(10), dp(10), dp(10), dp(10));
        t.setBackground(rr(MINT, 0, 12));
        t.addView(new IconView(this, icon, iconColor), lp(31, 31));
        LinearLayout c = col();
        c.setPadding(dp(8), 0, 0, 0);
        c.addView(tv(title, 10, MUTED, false));
        c.addView(tv(val, 12, valColor, true));
        if (subtext != null && !subtext.isEmpty()) c.addView(tv(subtext, 10, MUTED, false));
        t.addView(c, wt(1));
        return t;
    }

    int remCount(long pid) {
        int n = 0;
        if ("1".equals(db.get(remKey(pid, "med"), "1"))) n++;
        if ("1".equals(db.get(remKey(pid, "low"), "1"))) n++;
        if ("1".equals(db.get(remKey(pid, "refill"), "1"))) n++;
        if ("1".equals(db.get(remKey(pid, "follow"), "0"))) n++;
        return n;
    }

    void home() {
        LinearLayout b = page();
        final Patient p = needPatient(b);
        if (p == null) return;

        LinearLayout hd = row();
        hd.setPadding(0, dp(4), 0, dp(14));
        hd.addView(new AvatarView(this, p.getAvatar(), avType(p)), lp(60, 60));
        LinearLayout t = col();
        t.setPadding(dp(12), 0, 0, 0);
        LinearLayout cl = row();
        View dot = new View(this);
        dot.setBackground(rr(GREEN, 0, 4));
        cl.addView(dot, lp(8, 8));
        TextView c1 = tv("Caring for", 11, TEAL, true);
        c1.setPadding(dp(6), 0, 0, 0);
        cl.addView(c1);
        t.addView(cl);
        t.addView(tv(p.getName(), 20, INK, true));
        t.addView(tv(sub(p), 12, MUTED, false));
        hd.addView(t, wt(1));
        hd.addView(new IconView(this, "down", MUTED), lp(26, 26));
        hd.setOnClickListener(x -> go("dependents"));
        b.addView(hd);
        homeStreak(b, p);

        List<Medication> ms = db.meds(p.getId(), true);
        LinearLayout q = card(b);
        q.addView(tv("Quick Overview", 13, INK, true));
        sp(q, 10);
        LinearLayout tiles = row();
        String[] nd = nextDose(ms);
        View t1 = tile("checkfill", TEAL, "Next Dose", nd == null ? "No doses" : nd[0], INK, nd == null ? "" : nd[1]);
        int minDays = Integer.MAX_VALUE; Medication low = null;
        for (Medication m : ms) { int d = daysLeft(m); if (d < minDays) { minDays = d; low = m; } }
        String sv; int sc; String ss = "";
        if (low == null) { sv = "No stock"; sc = MUTED; }
        else if (minDays <= 3) { sv = "Low (" + minDays + (minDays == 1 ? " day" : " days") + " left)"; sc = ORANGE; ss = low.getName(); }
        else { sv = "Good"; sc = GREEN; ss = "Lowest: " + low.getName(); }
        View t2 = tile("box", TEAL, "Supply", sv, sc, ss);
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, -1, 1);
        l1.rightMargin = dp(8);
        tiles.addView(t1, l1);
        tiles.addView(t2, new LinearLayout.LayoutParams(0, -1, 1));
        q.addView(tiles);
        sp(q, 8);
        View t3 = tile("alertfill", TEAL, "Reminders", remCount(p.getId()) + " active", INK, null);
        t3.setOnClickListener(x -> go("reminders"));
        q.addView(t3);

        homeExtras(b, p, ms);
        LinearLayout mc = card(b);
        LinearLayout hr = row();
        hr.addView(tv("Patient's Medications", 14, INK, true), wt(1));
        TextView all = tv("View All", 12, TEAL, true);
        all.setPadding(dp(8), dp(4), 0, dp(4));
        all.setOnClickListener(x -> tab("meds"));
        hr.addView(all);
        mc.addView(hr);
        sp(mc, 8);
        int n = 0;
        for (Medication m : ms) { if (n++ >= 3) break; medRow(mc, m); }
        if (ms.isEmpty()) {
            mc.addView(tv("No medications added yet.", 13, MUTED, false));
            btn(mc, "+  Add Medication", () -> go("addmed"));
        }
    }

    void medRow(LinearLayout parent, final Medication m) {
        LinearLayout r = row();
        r.setPadding(dp(10), dp(10), dp(10), dp(10));
        r.setBackground(rr(CARD, BORDER, 12));
        r.addView(pillBox(m.getName(), 40), lp(40, 40));
        LinearLayout t = col();
        t.setPadding(dp(12), 0, dp(6), 0);
        t.addView(tv(m.getName(), 14, INK, true));
        t.addView(tv(m.getDosage() + " \u00B7 " + m.getForm(), 11, MUTED, false));
        t.addView(tv(shortFreq(m.getFrequency()), 11, MUTED, false));
        String rowHit = HealthAlertService.findAllergyMatch(curPatient(), m.getName());
        if (rowHit != null) t.addView(tv("\u26A0 Allergy: " + rowHit, 11, RED, true));
        r.addView(t, wt(1));
        r.addView(new IconView(this, "right", MUTED), lp(22, 22));
        r.setOnClickListener(x -> medDialog(m));
        LinearLayout.LayoutParams p = lp(-1, -2);
        p.bottomMargin = dp(8);
        parent.addView(r, p);
    }
}
