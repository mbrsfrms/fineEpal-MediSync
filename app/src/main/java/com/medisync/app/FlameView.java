package com.medisync.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

/** Streak flame graphic. */
class FlameView extends View {
    final boolean lit;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Path flame = new Path();

    FlameView(Context c, boolean lit) {
        super(c);
        this.lit = lit;
        flame.moveTo(50, 2);
        flame.cubicTo(58, 26, 94, 44, 94, 82);
        flame.cubicTo(94, 104, 76, 118, 50, 118);
        flame.cubicTo(24, 118, 6, 104, 6, 82);
        flame.cubicTo(6, 62, 20, 52, 25, 32);
        flame.cubicTo(34, 44, 40, 48, 43, 52);
        flame.cubicTo(40, 30, 44, 14, 50, 2);
        flame.close();
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        float k = Math.min(w / 100f, h / 120f);
        if (k <= 0) return;
        c.save();
        c.translate((w - 100 * k) / 2f, (h - 120 * k) / 2f);
        c.scale(k, k);
        p.setStyle(Paint.Style.FILL);
        if (lit) {
            p.setShader(null);
            p.setColor(0x33FF9A3C);
            c.drawCircle(50, 78, 40, p);
        }
        layer(c, 1f, lit ? 0xFFFFA32B : 0xFFC9D3D9, lit ? 0xFFFF4D1C : 0xFFAEBBC3);
        layer(c, 0.68f, lit ? 0xFFFFD43B : 0xFFDCE3E8, lit ? 0xFFFF9F1C : 0xFFC9D3D9);
        layer(c, 0.38f, lit ? 0xFFFFF6B8 : 0xFFEEF2F4, lit ? 0xFFFFDA5C : 0xFFDCE3E8);
        p.setShader(null);
        c.restore();
    }

    void layer(Canvas c, float sc, int top, int bottom) {
        c.save();
        c.translate(50, 118);
        c.scale(sc, sc);
        c.translate(-50, -118);
        p.setShader(new LinearGradient(0, 0, 0, 120, top, bottom, Shader.TileMode.CLAMP));
        c.drawPath(flame, p);
        c.restore();
    }
}
