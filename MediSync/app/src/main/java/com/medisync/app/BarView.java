package com.medisync.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** Simple progress bar. */
class BarView extends View {
    final float frac;
    final int color, track;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    BarView(Context c, float frac, int color, int track) { super(c); this.frac = frac; this.color = color; this.track = track; }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        p.setStyle(Paint.Style.FILL);
        p.setColor(track);
        c.drawRoundRect(new RectF(0, 0, w, h), h / 2, h / 2, p);
        p.setColor(color);
        float fw = Math.max(h, w * Math.max(0f, Math.min(1f, frac)));
        if (frac > 0) c.drawRoundRect(new RectF(0, 0, fw, h), h / 2, h / 2, p);
    }
}
