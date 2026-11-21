package com.medisync.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Draws the person avatars. */
final class PersonArt {
    private PersonArt() { }

    // 0 girl, 1 boy, 2 elder, 3 adult
    static void drawPerson(Canvas c, Paint p, float cx, float cy, float s, int type) {
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFCFEFEA);
        c.drawCircle(cx, cy, s / 2f, p);
        Path clip = new Path();
        clip.addCircle(cx, cy, s / 2f, Path.Direction.CW);
        c.save();
        c.clipPath(clip);
        int skin = type == 2 ? 0xFFF3C9A5 : 0xFFFFD6B5;
        int hair = type == 0 ? 0xFF4A2F27 : type == 1 ? 0xFF2E2A27 : type == 2 ? 0xFFC7D0D6 : 0xFF3A2A22;
        p.setColor(type == 2 ? 0xFF7C9BB0 : 0xFF1A9FA3);
        c.drawOval(new RectF(cx - s * 0.38f, cy + s * 0.2f, cx + s * 0.38f, cy + s * 0.95f), p);
        if (type == 0 || type == 3) {
            p.setColor(hair);
            c.drawOval(new RectF(cx - s * 0.28f, cy - s * 0.3f, cx + s * 0.28f, cy + s * 0.28f), p);
        }
        p.setColor(skin);
        RectF head = new RectF(cx - s * 0.2f, cy - s * 0.26f, cx + s * 0.2f, cy + s * 0.14f);
        c.drawOval(head, p);
        p.setColor(hair);
        c.drawArc(new RectF(head.left - s * 0.02f, head.top - s * 0.02f, head.right + s * 0.02f, head.bottom - s * 0.1f), 180, 180, true, p);
        p.setColor(0xFF2B2B2B);
        c.drawCircle(cx - s * 0.075f, cy - s * 0.06f, s * 0.025f, p);
        c.drawCircle(cx + s * 0.075f, cy - s * 0.06f, s * 0.025f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1f, s * 0.025f));
        p.setStrokeCap(Paint.Cap.ROUND);
        c.drawArc(new RectF(cx - s * 0.08f, cy - s * 0.04f, cx + s * 0.08f, cy + s * 0.07f), 20, 140, false, p);
        p.setStyle(Paint.Style.FILL);
        c.restore();
    }
}
