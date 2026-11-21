package com.medisync.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.model.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

/** Streak details and About screens. */
abstract class StreakAboutScreens extends ReminderSettings {
    void streak() {
        LinearLayout b = page();
        Patient p = needPatient(b);
        if (p == null) return;
        topBar(b, p.getName().split(" ")[0] + " Streak", null, null);
        HashSet<String> days = completeDays(p.getId());
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        int streak = streakOf(days);
        int[] todayPr = dayProgress(db.meds(p.getId(), true), db.history(p.getId()), today());

        LinearLayout c = card(b);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams flp = lp(120, 140);
        flp.gravity = Gravity.CENTER_HORIZONTAL;
        c.addView(new FlameView(this, streak > 0), flp);
        TextView n = tv(streak + (streak == 1 ? " day" : " days"), 28, TEAL, true);
        n.setGravity(Gravity.CENTER);
        c.addView(n);
        TextView g = tv(streak > 0 ? "Great job! Keep it up!" : "Take all of today's medicines to start your streak.", 12, TEAL, false);
        g.setGravity(Gravity.CENTER);
        g.setPadding(0, dp(2), 0, dp(10));
        c.addView(g);
        String tp;
        if (todayPr[1] == 0) tp = "No scheduled doses to track yet.";
        else if (todayPr[0] >= todayPr[1]) tp = "Today: all " + todayPr[1] + " doses taken ✓";
        else tp = "Today: " + todayPr[0] + " of " + todayPr[1] + " doses taken";
        TextView tpv = tv(tp, 12, todayPr[1] > 0 && todayPr[0] >= todayPr[1] ? GREEN : MUTED, true);
        tpv.setGravity(Gravity.CENTER);
        tpv.setPadding(0, 0, 0, dp(14));
        c.addView(tpv);
        divider(c);
        sp(c, 12);
        LinearLayout wk = row();
        String[] names = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        Calendar w = Calendar.getInstance();
        w.add(Calendar.DAY_OF_YEAR, -(w.get(Calendar.DAY_OF_WEEK) - 1));
        for (int i = 0; i < 7; i++) {
            LinearLayout d = col();
            d.setGravity(Gravity.CENTER);
            TextView nm = tv(names[i], 10, MUTED, false);
            nm.setGravity(Gravity.CENTER);
            d.addView(nm);
            sp(d, 4);
            boolean did = days.contains(f.format(w.getTime()));
            FrameLayout dot = new FrameLayout(this);
            GradientDrawable og = new GradientDrawable();
            og.setShape(GradientDrawable.OVAL);
            og.setColor(did ? TEAL : CARD);
            og.setStroke(dp(1), did ? TEAL : BORDER);
            dot.setBackground(og);
            if (did) dot.addView(new IconView(this, "check", Color.WHITE), new FrameLayout.LayoutParams(dp(17), dp(17), Gravity.CENTER));
            d.addView(dot, lp(24, 24));
            wk.addView(d, new LinearLayout.LayoutParams(0, -2, 1));
            w.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.addView(wk);
        sp(c, 10);
        TextView rule = tv("A day counts when every scheduled dose of every medicine is marked Taken.", 10, MUTED, false);
        rule.setGravity(Gravity.CENTER);
        c.addView(rule);

        LinearLayout gl = card(b);
        gl.setBackground(rr(MINT, 0, 14));
        LinearLayout gr = row();
        gr.addView(tv("\uD83C\uDFC6", 28, ORANGE, false));
        LinearLayout gt = col();
        gt.setPadding(dp(12), 0, 0, 0);
        gt.addView(tv("Your goal", 11, MUTED, false));
        gt.addView(tv("7 days", 15, INK, true));
        gr.addView(gt, wt(1));
        gl.addView(gr);
        sp(gl, 8);
        gl.addView(new BarView(this, Math.min(1f, streak / 7f), TEAL, BORDER), lp(-1, 8));
    }

    void about() {
        LinearLayout b = page();
        topBar(b, "About MediSync", null, null);
        LinearLayout c = card(b);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.setPadding(dp(18), dp(22), dp(18), dp(22));
        LinearLayout.LayoutParams lg = lp(84, 84);
        lg.gravity = Gravity.CENTER_HORIZONTAL;
        c.addView(new LogoView(this, 0xFF1A9FA3, 0xFFA6E6DC, true), lg);
        TextView t = tv("MediSync", 24, TEAL, true);
        t.setGravity(Gravity.CENTER);
        c.addView(t);
        TextView s = tv("Medication Monitoring\nand Scheduling System", 12, MUTED, false);
        s.setGravity(Gravity.CENTER);
        s.setPadding(0, dp(2), 0, dp(16));
        c.addView(s);
        LinearLayout sdg = col();
        sdg.setGravity(Gravity.CENTER_HORIZONTAL);
        sdg.setBackground(rr(MINT, 0, 12));
        sdg.setPadding(dp(14), dp(14), dp(14), dp(14));
        sdg.addView(new IconView(this, "heart", TEAL), lp(41, 41));
        sp(sdg, 6);
        TextView sg = tv("Supports Sustainable\nDevelopment Goal 3\nGood Health and Well-Being", 13, TEAL, true);
        sg.setGravity(Gravity.CENTER);
        sdg.addView(sg);
        c.addView(sdg, lp(-1, -2));
        sp(c, 14);
        TextView tag = tv("Take your medication.\nTake care of your health.", 12, MUTED, false);
        tag.setGravity(Gravity.CENTER);
        c.addView(tag);
        sp(c, 10);
        TextView ver = tv("Version 1.0", 11, HINT, false);
        ver.setGravity(Gravity.CENTER);
        c.addView(ver);
        sp(c, 8);
        TextView dis = tv("MediSync supports care decisions. It does not replace advice from a qualified doctor or pharmacist.", 10, HINT, false);
        dis.setGravity(Gravity.CENTER);
        c.addView(dis);
    }
}
