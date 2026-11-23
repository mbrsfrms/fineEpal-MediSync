package com.medisync.app;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Dialog helpers: generic dialog, list dialog and confirm dialog. */
abstract class UiDialogs extends UiInputs {
    AlertDialog dialog(String title, View body, String pos, final Act onPos, String neg) {
        LinearLayout box = col();
        box.setBackground(rr(CARD, 0, 18));
        box.setPadding(dp(18), dp(18), dp(18), dp(10));
        if (title != null) { box.addView(tv(title, 16, INK, true)); sp(box, 12); }
        if (body != null) {
            final int maxH = (int) (getResources().getDisplayMetrics().heightPixels * 0.62f);
            ScrollView bodyScroll = new ScrollView(this) {
                @Override protected void onMeasure(int w, int h) {
                    super.onMeasure(w, View.MeasureSpec.makeMeasureSpec(maxH, View.MeasureSpec.AT_MOST));
                }
            };
            bodyScroll.setVerticalScrollBarEnabled(false);
            bodyScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
            if (body.getParent() instanceof ViewGroup) ((ViewGroup) body.getParent()).removeView(body);
            bodyScroll.addView(body);
            box.addView(bodyScroll, new LinearLayout.LayoutParams(-1, -2));
        }
        final AlertDialog d = new AlertDialog.Builder(this).setView(box).create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearLayout bar = row();
        bar.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        if (neg != null) {
            TextView n = tv(neg, 13, MUTED, true);
            n.setPadding(dp(14), dp(10), dp(14), dp(10));
            n.setOnClickListener(x -> d.dismiss());
            bar.addView(n);
        }
        if (pos != null) {
            TextView pb = tv(pos, 13, TEAL, true);
            pb.setPadding(dp(14), dp(10), dp(8), dp(10));
            pb.setOnClickListener(x -> { if (onPos == null || onPos.run()) d.dismiss(); });
            bar.addView(pb);
        }
        sp(box, 6);
        box.addView(bar, lp(-1, -2));
        d.show();
        if (d.getWindow() != null)
            d.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9f), ViewGroup.LayoutParams.WRAP_CONTENT);
        return d;
    }

    void listDialog(String title, String[] opts, final IntCb cb) {
        final AlertDialog[] dd = new AlertDialog[1];
        LinearLayout body = col();
        for (int i = 0; i < opts.length; i++) {
            final int k = i;
            TextView t = tv(opts[i], 14, INK, false);
            t.setPadding(dp(4), dp(12), dp(4), dp(12));
            t.setOnClickListener(x -> { dd[0].dismiss(); cb.on(k); });
            body.addView(t);
            if (i < opts.length - 1) divider(body);
        }
        dd[0] = dialog(title, body, null, null, "Cancel");
    }

    void confirm(String title, String msg, String pos, final Runnable r) {
        dialog(title, tv(msg, 13, MUTED, false), pos, () -> { r.run(); return true; }, "Cancel");
    }
}
