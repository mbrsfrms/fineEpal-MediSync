package com.medisync.app;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.medisync.app.model.*;
import com.medisync.app.util.DateUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Dependents, upcoming doses and supply screens. */
abstract class DependentScreens extends PatientScreen {
    void dependents() {
        LinearLayout b = page();
        topBar(b, "My Dependent", null, null);
        final List<Patient> ps = db.patients(userId);
        final Patient cp = curPatient();
        if (cp != null) {
            LinearLayout c = card(b);
            LinearLayout h = row();
            h.addView(new AvatarView(this, cp.getAvatar(), avType(cp)), lp(54, 54));
            LinearLayout t = col();
            t.setPadding(dp(12), 0, 0, 0);
            t.addView(tv(cp.getName(), 15, INK, true));
            t.addView(tv(sub(cp), 12, MUTED, false));
            h.addView(t, wt(1));
            h.addView(new IconView(this, "right", MUTED), lp(22, 22));
            h.setOnClickListener(x -> go("patient"));
            c.addView(h);
        }
        LinearLayout sw = card(b);
        sw.addView(tv("Switch Patient", 13, INK, true));
        sp(sw, 8);
        for (int i = 0; i < ps.size(); i++) {
            final Patient p = ps.get(i);
            LinearLayout r = row();
            r.setPadding(dp(4), dp(10), dp(4), dp(10));
            r.addView(new AvatarView(this, p.getAvatar(), avType(p)), lp(46, 46));
            LinearLayout t = col();
            t.setPadding(dp(12), 0, 0, 0);
            t.addView(tv(p.getName(), 14, INK, true));
            t.addView(tv(sub(p), 11, MUTED, false));
            r.addView(t, wt(1));
            if (p.getId() == patientId) r.addView(new IconView(this, "check", TEAL), lp(24, 24));
            r.setOnClickListener(x -> {
                patientId = p.getId();
                db.put("patient_id", "" + p.getId());
                toast("Now caring for " + p.getName());
                render();
            });
            sw.addView(r);
            if (i < ps.size() - 1) divider(sw);
        }
        btn(b, "+  Add Patient", () -> { addingDependent = true; careType = "Child"; go("caretype"); });
    }

    void upcoming() {
        LinearLayout b = page();
        Patient p = needPatient(b);
        if (p == null) return;
        topBar(b, p.getName(), "Upcoming Doses", p);
        List<Medication> ms = db.meds(p.getId(), true);
        final class Slot { int t; Medication m; boolean done; }
        List<Slot> slots = new ArrayList<>();
        String td = DateUtils.today();
        for (Medication m : ms) {
            if (m.isAsNeeded() || !m.isStartedBy(td)) continue;
            if (m.isEveryOtherDay() && DateUtils.daysBetween(m.getCreated(), td) % 2 != 0) continue;
            for (int t : m.getDoseMinutes()) {
                Slot s = new Slot();
                s.t = t;
                s.m = m;
                s.done = db.isSlotRecorded(m.getId(), td + " " + DateUtils.formatMinutes24h(t));
                slots.add(s);
            }
        }
        Collections.sort(slots, (x, y) -> x.t - y.t);
        b.addView(tv("Today · " + DateUtils.formatDisplayDate(td), 12, MUTED, true));
        sp(b, 8);
        if (slots.isEmpty()) b.addView(tv("No scheduled doses today.", 13, MUTED, false));
        for (final Slot s : slots) {
            LinearLayout c = row();
            c.setPadding(dp(12), dp(12), dp(12), dp(12));
            c.setBackground(rr(CARD, BORDER, 12));
            c.addView(pillBox(s.m.getName(), 38), lp(38, 38));
            LinearLayout t = col();
            t.setPadding(dp(12), 0, dp(6), 0);
            t.addView(tv(s.m.getName() + " " + s.m.getDosage(), 13, INK, true));
            t.addView(tv(DateUtils.formatMinutes(s.t) + (s.done ? " · Done" : " · Pending"), 11, s.done ? GREEN : ORANGE, false));
            c.addView(t, wt(1));
            if (!s.done) {
                TextView tk = tv("Take", 12, Color.WHITE, true);
                tk.setGravity(Gravity.CENTER);
                tk.setBackground(grad(10));
                tk.setOnClickListener(x -> recordDoseAt(s.m, "Taken", s.t));
                c.addView(tk, lp(64, 34));
            } else c.addView(new IconView(this, "check", GREEN), lp(26, 26));
            LinearLayout.LayoutParams p2 = lp(-1, -2);
            p2.bottomMargin = dp(8);
            b.addView(c, p2);
        }
    }

    void supply() {
        LinearLayout b = page();
        Patient p = needPatient(b);
        if (p == null) return;
        topBar(b, p.getName(), "Medication Supply", p);
        List<Medication> ms = db.meds(p.getId(), true);
        if (ms.isEmpty()) b.addView(tv("No active medications.", 13, MUTED, false));
        for (final Medication m : ms) {
            LinearLayout c = card(b);
            LinearLayout h = row();
            h.addView(pillBox(m.getName(), 38), lp(38, 38));
            LinearLayout t = col();
            t.setPadding(dp(12), 0, 0, 0);
            t.addView(tv(m.getName() + " " + m.getDosage(), 13, INK, true));
            int d = daysLeft(m);
            int col = d <= 2 ? RED : d <= 7 ? ORANGE : TEAL;
            float frac = m.getTotal() <= 0 ? 0 : Math.min(1f, m.getRemaining() / (float) m.getTotal());
            t.addView(new BarView(this, frac, col, BORDER), lp(-1, 7));
            h.addView(t, wt(1));
            c.addView(h);
            sp(c, 6);
            c.addView(tv(m.getRemaining() + " " + unit(m.getForm()) + " left (about " + d + (d == 1 ? " day)" : " days)"), 11, MUTED, false));
            if (d <= 7) {
                LinearLayout w = row();
                w.addView(new IconView(this, "warn", d <= 2 ? RED : ORANGE), lp(17, 17));
                TextView wt1 = tv(d <= 2 ? "Low supply" : "Running low", 11, d <= 2 ? RED : ORANGE, true);
                wt1.setPadding(dp(5), 0, 0, 0);
                w.addView(wt1);
                c.addView(w);
            }
            c.setOnClickListener(x -> editSupply(m));
        }
    }
}
