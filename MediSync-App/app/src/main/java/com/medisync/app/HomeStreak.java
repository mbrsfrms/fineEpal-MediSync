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

/** Home screen weekly streak widgets. */
abstract class HomeStreak extends HomeAlerts {
    LinearLayout weekRow(HashSet<String> days) {
        LinearLayout wk = row();
        String[] names = {"S", "M", "T", "W", "T", "F", "S"};
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String td = today();
        Calendar w = Calendar.getInstance();
        w.add(Calendar.DAY_OF_YEAR, -(w.get(Calendar.DAY_OF_WEEK) - 1));
        for (int i = 0; i < 7; i++) {
            String key = f.format(w.getTime());
            boolean did = days.contains(key), isToday = key.equals(td);
            LinearLayout d = col();
            d.setGravity(Gravity.CENTER);
            TextView nm = tv(names[i], 11, isToday ? ORANGE : MUTED, isToday);
            nm.setGravity(Gravity.CENTER);
            d.addView(nm);
            sp(d, 4);
            FrameLayout dot = new FrameLayout(this);
            GradientDrawable og = new GradientDrawable();
            og.setShape(GradientDrawable.OVAL);
            og.setColor(did ? ORANGE : Color.TRANSPARENT);
            og.setStroke(dp(isToday && !did ? 2 : 1), (did || isToday) ? ORANGE : (dark ? 0xFF5A3A22 : 0xFFF0C9A0));
            dot.setBackground(og);
            if (did) dot.addView(new IconView(this, "check", Color.WHITE), new FrameLayout.LayoutParams(dp(17), dp(17), Gravity.CENTER));
            d.addView(dot, lp(30, 30));
            wk.addView(d, new LinearLayout.LayoutParams(0, -2, 1));
            w.add(Calendar.DAY_OF_YEAR, 1);
        }
        return wk;
    }

    void homeStreak(LinearLayout b, Patient p) {
        HashSet<String> days = completeDays(p.getId());
        int streak = streakOf(days);
        int[] pr = dayProgress(db.meds(p.getId(), true), db.history(p.getId()), today());
        int hot = dark ? 0xFFFFA45C : 0xFFE8590C;

        LinearLayout c = col();
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                dark ? new int[]{0xFF3B2514, 0xFF2A1B12} : new int[]{0xFFFFF4E0, 0xFFFFE3C4});
        g.setCornerRadius(dp(22));
        g.setStroke(dp(1), dark ? 0xFF5A3A22 : 0xFFFFD3A3);
        c.setBackground(g);

        LinearLayout top = row();
        top.addView(new FlameView(this, streak > 0), lp(100, 118));
        LinearLayout t = col();
        t.setPadding(dp(14), 0, 0, 0);
        TextView num = tv("" + streak, 48, hot, true);
        num.setIncludeFontPadding(false);
        t.addView(num);
        t.addView(tv("day streak", 16, INK, true));
        boolean allDone = pr[1] > 0 && pr[0] >= pr[1];
        String msg = streak == 0 ? "Take all of today's medicines to start your streak!"
                : (allDone ? "All of today's doses are done. Great job!" : "Keep it up!");
        TextView mt = tv(msg, 12, INK, false);
        mt.setPadding(0, dp(2), 0, 0);
        t.addView(mt);
        String tp = pr[1] == 0 ? "No scheduled doses today" : "Today: " + pr[0] + " of " + pr[1] + " doses taken";
        TextView pt = tv(tp, 11, allDone ? GREEN : MUTED, true);
        pt.setPadding(0, dp(4), 0, 0);
        t.addView(pt);
        top.addView(t, wt(1));
        c.addView(top);
        sp(c, 10);
        c.addView(weekRow(days));
        c.setOnClickListener(x -> go("streak"));
        LinearLayout.LayoutParams cp = lp(-1, -2);
        cp.bottomMargin = dp(14);
        b.addView(c, cp);
    }
}
