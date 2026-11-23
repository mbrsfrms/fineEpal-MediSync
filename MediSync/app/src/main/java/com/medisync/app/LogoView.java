package com.medisync.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** MediSync logo mark. */
class LogoView extends View {
    final int dark, light;
    final boolean plus;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    LogoView(Context c, int dark, int light, boolean plus) { super(c); this.dark = dark; this.light = light; this.plus = plus; }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), s = Math.min(w, h);
        if (s <= 0) return;
        float cx = w / 2f - (plus ? s * 0.03f : 0), cy = h / 2f - (plus ? s * 0.03f : 0);
        float len = s * (plus ? 0.74f : 0.78f), th = s * (plus ? 0.32f : 0.34f);
        c.save();
        c.translate(cx, cy);
        c.rotate(-45);
        RectF r = new RectF(-len / 2, -th / 2, len / 2, th / 2);
        Path clip = new Path();
        clip.addRoundRect(r, th / 2, th / 2, Path.Direction.CW);
        c.save();
        c.clipPath(clip);
        p.setStyle(Paint.Style.FILL);
        p.setColor(dark);
        c.drawRect(-len / 2, -th / 2, 0, th / 2, p);
        p.setColor(light);
        c.drawRect(0, -th / 2, len / 2, th / 2, p);
        p.setColor(0x55FFFFFF);
        c.drawRoundRect(new RectF(-len / 2 + th * 0.3f, -th / 2 + th * 0.16f, -th * 0.15f, -th / 2 + th * 0.3f), th * 0.07f, th * 0.07f, p);
        c.restore();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1f, s * 0.022f));
        p.setColor(dark);
        c.drawRoundRect(r, th / 2, th / 2, p);
        c.restore();
        if (plus) {
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(s * 0.07f);
            p.setColor(dark);
            float px = w / 2f + s * 0.27f, py = h / 2f + s * 0.27f, a = s * 0.1f;
            c.drawLine(px - a, py, px + a, py, p);
            c.drawLine(px, py - a, px, py + a, p);
        }
    }
}
