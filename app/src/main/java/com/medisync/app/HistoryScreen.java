package com.medisync.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.model.*;
import java.util.List;

/** Dose history screen. */
abstract class HistoryScreen extends MedListScreen {
    void history() {
        LinearLayout b = page();
        Patient p = needPatient(b);
        if (p == null) return;
        topBar(b, p.getName(), "Intake History", p);

        LinearLayout chips = row();
        String[] f = {"All", "Taken", "Late", "Missed", "Skipped"};
        for (int i = 0; i < 5; i++) {
            final String fv = f[i];
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, dp(34), 1);
            if (i < 4) cp.rightMargin = dp(6);
            chips.addView(chip(fv, histFilter.equals(fv), () -> { histFilter = fv; render(); }), cp);
        }
        LinearLayout.LayoutParams cpp = lp(-1, -2);
        cpp.bottomMargin = dp(14);
        b.addView(chips, cpp);

        List<IntakeRecord> recs = db.history(p.getId());
        int taken = 0;
        for (IntakeRecord r : recs) if (r.isTaken()) taken++;
        if (!recs.isEmpty()) {
            TextView adh = tv("Adherence: " + (taken * 100 / recs.size()) + "%  \u00B7  " + recs.size() + " records", 12, MUTED, false);
            adh.setPadding(dp(2), 0, 0, dp(10));
            b.addView(adh);
        }
        String lastDate = "";
        int shown = 0;
        for (IntakeRecord r : recs) {
            if (!histFilter.equals("All") && !statusMatches(r, histFilter)) continue;
            String d = r.getRecordedAt().length() >= 10 ? r.getRecordedAt().substring(0, 10) : r.getRecordedAt();
            if (!d.equals(lastDate)) {
                TextView h = tv(fmtDate(d), 12, MUTED, true);
                h.setPadding(dp(2), dp(6), 0, dp(6));
                b.addView(h);
                lastDate = d;
            }
            String time = "";
            try {
                time = fmtMin(Integer.parseInt(r.getRecordedAt().substring(11, 13)) * 60 + Integer.parseInt(r.getRecordedAt().substring(14, 16)));
            } catch (Exception ignored) { }
            LinearLayout c = row();
            c.setPadding(dp(10), dp(10), dp(10), dp(10));
            c.setBackground(rr(CARD, BORDER, 12));
            c.addView(pillBox(r.getName(), 38), lp(38, 38));
            LinearLayout t = col();
            t.setPadding(dp(12), 0, dp(6), 0);
            t.addView(tv(r.getName() + " " + r.getDosage(), 13, INK, true));
            int sc = "Late".equals(r.getStatus()) ? ORANGE : r.isTaken() ? GREEN : r.getStatus().equals("Missed") ? RED : MUTED;
            t.addView(tv(r.getStatus() + " \u00B7 " + time, 11, sc, false));
            c.addView(t, wt(1));
            FrameLayout badge = new FrameLayout(this);
            GradientDrawable og = new GradientDrawable();
            og.setShape(GradientDrawable.OVAL);
            og.setColor(sc);
            badge.setBackground(og);
            badge.addView(new IconView(this, r.isTaken() ? "check" : r.getStatus().equals("Missed") ? "x" : "skip", Color.WHITE),
                    new FrameLayout.LayoutParams(dp(17), dp(17), Gravity.CENTER));
            c.addView(badge, lp(22, 22));
            LinearLayout.LayoutParams p2 = lp(-1, -2);
            p2.bottomMargin = dp(8);
            b.addView(c, p2);
            shown++;
        }
        if (shown == 0) b.addView(tv("No intake records yet. Open a medication and record a dose.", 13, MUTED, false));
    }
}
