package com.medisync.app;

import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Google sign-in button, divider and message widgets. */
abstract class GoogleAuthUi extends GoogleAuthFlow {
    /** Google's "Continue with Google" button, following Google's branding rules for light and dark themes. */
    void googleButton(LinearLayout parent, final Runnable r) {
        int fill = dark ? 0xFF131314 : 0xFFFFFFFF;
        int stroke = dark ? 0xFF8E918F : 0xFF747775;
        int text = dark ? 0xFFE3E3E3 : 0xFF1F1F1F;
        LinearLayout g = row();
        g.setGravity(Gravity.CENTER);
        g.setBackground(rr(fill, stroke, 28));
        g.setElevation(dp(2));
        g.setPadding(dp(16), 0, dp(16), 0);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_google_logo);
        g.addView(logo, lp(22, 22));
        TextView t = tv(signingIn ? "Signing in…" : "Continue with Google", 16, text, true);
        t.setPadding(dp(12), 0, 0, 0);
        g.addView(t);
        g.setAlpha(signingIn ? 0.6f : 1f);
        g.setClickable(true);
        g.setContentDescription("Continue with Google");
        g.setOnClickListener(x -> { if (!signingIn) r.run(); });
        LinearLayout.LayoutParams p = lp(-1, 56);
        p.topMargin = dp(10);
        p.bottomMargin = dp(8);
        parent.addView(g, p);
    }

    /** Thin "or" separator between the password form and the Google button. */
    void orDivider(LinearLayout parent) {
        LinearLayout r = row();
        View l1 = new View(this); l1.setBackgroundColor(BORDER);
        View l2 = new View(this); l2.setBackgroundColor(BORDER);
        r.addView(l1, new LinearLayout.LayoutParams(0, dp(1), 1));
        TextView t = tv("or", 12, MUTED, false);
        t.setPadding(dp(12), 0, dp(12), 0);
        r.addView(t);
        r.addView(l2, new LinearLayout.LayoutParams(0, dp(1), 1));
        LinearLayout.LayoutParams p = lp(-1, -2);
        p.topMargin = dp(10);
        p.bottomMargin = dp(2);
        parent.addView(r, p);
    }

    /** Message under the Google button (cancelled, no network, setup problems...). */
    void googleMessage(LinearLayout parent) {
        if (signingIn) {
            TextView s = tv("Waiting for Google\u2026", 12, MUTED, false);
            s.setGravity(Gravity.CENTER);
            parent.addView(s, lp(-1, -2));
        }
        if (!loginMsg.isEmpty()) {
            TextView m = tv(loginMsg, 12, loginMsgIsError ? RED : MUTED, false);
            m.setGravity(Gravity.CENTER);
            m.setPadding(dp(4), dp(6), dp(4), dp(4));
            parent.addView(m);
        }
    }
}
