package com.medisync.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Decorative wave header. */
class WaveView extends View {
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    WaveView(Context c) { super(c); }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        p.setStyle(Paint.Style.FILL);
        Path a = new Path();
        a.moveTo(0, h * 0.5f);
        a.cubicTo(w * 0.25f, h * 0.15f, w * 0.5f, h * 0.85f, w, h * 0.35f);
        a.lineTo(w, h); a.lineTo(0, h); a.close();
        p.setColor(0x261A9FA3);
        c.drawPath(a, p);
        Path b = new Path();
        b.moveTo(0, h * 0.72f);
        b.cubicTo(w * 0.3f, h * 0.4f, w * 0.6f, h * 1.0f, w, h * 0.6f);
        b.lineTo(w, h); b.lineTo(0, h); b.close();
        p.setColor(0x331A9FA3);
        c.drawPath(b, p);
    }
}
