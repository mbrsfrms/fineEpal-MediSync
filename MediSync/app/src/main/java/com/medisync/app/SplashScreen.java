package com.medisync.app;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

abstract class SplashScreen extends Navigation {
    void splash() {
        FrameLayout f = new FrameLayout(this);
        LinearLayout b = col();
        b.setGravity(Gravity.CENTER);
        b.addView(new LogoView(this, 0xFF5CD3C7, 0xFFFFFFFF, true), lp(140, 140));
        sp(b, 8);
        TextView t = tv("MediSync", 36, Color.WHITE, true);
        t.setGravity(Gravity.CENTER);
        b.addView(t);
        sp(b, 8);
        TextView s = tv("Your Medications,\nOn Track.", 16, 0xFFE6F8F6, false);
        s.setGravity(Gravity.CENTER);
        b.addView(s);
        f.addView(b, new FrameLayout.LayoutParams(-1, -1));
        // fade + gentle zoom-in of the logo and name
        b.setAlpha(0f);
        b.setScaleX(0.85f);
        b.setScaleY(0.85f);
        b.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(900).start();
        LinearLayout dots = row();
        for (int i = 0; i < 3; i++) {
            View d = new View(this);
            d.setBackground(rr(i == 0 ? Color.WHITE : 0x66FFFFFF, 0, 3));
            LinearLayout.LayoutParams p = lp(i == 0 ? 24 : 12, 5);
            p.leftMargin = dp(3); p.rightMargin = dp(3);
            dots.addView(d, p);
        }
        FrameLayout.LayoutParams dp0 = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        dp0.bottomMargin = dp(48);
        f.addView(dots, dp0);
        content.addView(f, new FrameLayout.LayoutParams(-1, -1));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || !cur.equals("splash")) return;
            if (userId >= 0) {
                if (db.patients(userId).isEmpty()) tab("role"); else { tab("home"); scheduleAll(); openPending(); }
            } else tab("login");
        }, 2400);
    }

    void authHeader(LinearLayout b, String big, String small) {
        b.setGravity(Gravity.CENTER_HORIZONTAL);
        sp(b, 20);
        LinearLayout.LayoutParams p = lp(70, 70);
        p.gravity = Gravity.CENTER_HORIZONTAL;
        b.addView(new LogoView(this, 0xFF1A9FA3, 0xFFA6E6DC, true), p);
        sp(b, 4);
        TextView t = tv(big, 24, TEAL, true);
        t.setGravity(Gravity.CENTER);
        b.addView(t);
        TextView s = tv(small, 13, MUTED, false);
        s.setGravity(Gravity.CENTER);
        s.setPadding(0, dp(4), 0, dp(20));
        b.addView(s);
    }

    void linkRow(LinearLayout b, String q, String link, final Runnable r) {
        LinearLayout l = row();
        l.setGravity(Gravity.CENTER);
        l.addView(tv(q + " ", 12, MUTED, false));
        TextView t = tv(link, 12, TEAL, true);
        t.setOnClickListener(x -> r.run());
        t.setPadding(dp(4), dp(8), dp(4), dp(8));
        l.addView(t);
        b.addView(l, lp(-1, -2));
    }
}
