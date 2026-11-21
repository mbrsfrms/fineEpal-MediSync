package com.medisync.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.medisync.app.model.*;

/** Basic view factories: sizes, rounded boxes, text, rows, cards, buttons and the page frame. */
abstract class UiBasics extends FormatHelpers {
    int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + .5f); }

    GradientDrawable rr(int fill, int stroke, float r) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(r));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    GradientDrawable grad(float r) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xFF25B3AF, 0xFF10858C});
        d.setCornerRadius(dp(r));
        return d;
    }

    TextView tv(String s, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size * TS);
        t.setTextColor(color);
        t.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return t;
    }

    LinearLayout col() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }

    LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h)); }

    LinearLayout.LayoutParams wt(float w) { return new LinearLayout.LayoutParams(0, -2, w); }

    void sp(LinearLayout p, int h) { p.addView(new View(this), new LinearLayout.LayoutParams(1, dp(h))); }

    void divider(LinearLayout p) { View d = new View(this); d.setBackgroundColor(BORDER); p.addView(d, lp(-1, 1)); }

    void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    LinearLayout card(LinearLayout parent) {
        LinearLayout c = col();
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        c.setBackground(rr(CARD, BORDER, 14));
        LinearLayout.LayoutParams p = lp(-1, -2);
        p.bottomMargin = dp(12);
        parent.addView(c, p);
        return c;
    }

    Button btn(LinearLayout parent, String text, final Runnable r) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackground(grad(12));
        b.setStateListAnimator(null);
        b.setOnClickListener(x -> r.run());
        LinearLayout.LayoutParams p = lp(-1, 54);
        p.topMargin = dp(8);
        p.bottomMargin = dp(6);
        parent.addView(b, p);
        return b;
    }

    TextView chip(String text, boolean on, final Runnable r) {
        TextView t = tv(text, 12, on ? Color.WHITE : MUTED, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(on ? grad(18) : rr(CARD, BORDER, 18));
        t.setOnClickListener(x -> r.run());
        return t;
    }

    LinearLayout page() {
        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);
        sc.setVerticalScrollBarEnabled(false);
        sc.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        sc.setNestedScrollingEnabled(true);
        sc.setClipToPadding(false);
        LinearLayout b = col();
        b.setPadding(dp(18), dp(14), dp(18), dp(90));
        sc.addView(b);
        content.addView(sc, new FrameLayout.LayoutParams(-1, -1));
        return b;
    }

    LinearLayout wavePage() {
        content.addView(new WaveView(this), new FrameLayout.LayoutParams(-1, dp(170), Gravity.BOTTOM));
        return page();
    }

    void topBar(LinearLayout b, String title, String sub, Patient avatarOf) {
        LinearLayout r = row();
        r.setPadding(0, 0, 0, dp(14));
        IconView back = new IconView(this, "back", INK);
        back.setOnClickListener(x -> back());
        r.addView(back, lp(30, 30));
        if (avatarOf != null) {
            LinearLayout.LayoutParams ap = lp(46, 46);
            ap.leftMargin = dp(8);
            r.addView(new AvatarView(this, avatarOf.getAvatar(), avType(avatarOf)), ap);
        }
        LinearLayout t = col();
        t.setPadding(dp(10), 0, 0, 0);
        t.addView(tv(title, 17, INK, true));
        if (sub != null) t.addView(tv(sub, 12, TEAL, false));
        r.addView(t, wt(1));
        b.addView(r);
    }
}
