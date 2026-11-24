package com.medisync.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;


final class AnimalArt {
    private AnimalArt() { }

    static void tri(Canvas c, Paint p, float x1, float y1, float x2, float y2, float x3, float y3) {
        Path t = new Path();
        t.moveTo(x1, y1); t.lineTo(x2, y2); t.lineTo(x3, y3); t.close();
        c.drawPath(t, p);
    }

    static void oval(Canvas c, Paint p, float cx, float cy, float rx, float ry, float rot) {
        c.save();
        c.rotate(rot, cx, cy);
        c.drawOval(new RectF(cx - rx, cy - ry, cx + rx, cy + ry), p);
        c.restore();
    }

    // 0 cat, 1 bunny, 2 bear, 3 panda, 4 puppy, 5 fox, 6 chick, 7 frog
    static void drawAnimal(Canvas c, Paint p, float cx, float cy, float s, int k) {
        final int[] BG = {0xFFFFE4EC, 0xFFE3EEFF, 0xFFFFEBD6, 0xFFE4F5EA, 0xFFFFF1D0, 0xFFFFE0CC, 0xFFFFF7C2, 0xFFDDF5E3};
        final int[] FACE = {0xFFFFB86B, 0xFFFFFFFF, 0xFFB9794B, 0xFFFFFFFF, 0xFFEBCFA6, 0xFFFF8A3D, 0xFFFFD84D, 0xFF7ED67E};
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(BG[k]);
        c.drawCircle(cx, cy, s / 2f, p);
        Path clip = new Path();
        clip.addCircle(cx, cy, s / 2f, Path.Direction.CW);
        c.save();
        c.clipPath(clip);
        float fy = cy + 0.07f * s;
        int face = FACE[k];

        // ears (behind the face)
        switch (k) {
            case 0:
            case 5:
                p.setColor(face);
                tri(c, p, cx - 0.33f * s, fy - 0.05f * s, cx - 0.30f * s, fy - 0.42f * s, cx - 0.05f * s, fy - 0.27f * s);
                tri(c, p, cx + 0.33f * s, fy - 0.05f * s, cx + 0.30f * s, fy - 0.42f * s, cx + 0.05f * s, fy - 0.27f * s);
                p.setColor(k == 0 ? 0xFFFF9DB5 : 0xFF6B3F2A);
                tri(c, p, cx - 0.28f * s, fy - 0.14f * s, cx - 0.27f * s, fy - 0.33f * s, cx - 0.12f * s, fy - 0.24f * s);
                tri(c, p, cx + 0.28f * s, fy - 0.14f * s, cx + 0.27f * s, fy - 0.33f * s, cx + 0.12f * s, fy - 0.24f * s);
                break;
            case 1:
                p.setColor(face);
                oval(c, p, cx - 0.15f * s, fy - 0.38f * s, 0.075f * s, 0.24f * s, -8);
                oval(c, p, cx + 0.15f * s, fy - 0.38f * s, 0.075f * s, 0.24f * s, 8);
                p.setColor(0xFFFFB6C8);
                oval(c, p, cx - 0.15f * s, fy - 0.38f * s, 0.04f * s, 0.18f * s, -8);
                oval(c, p, cx + 0.15f * s, fy - 0.38f * s, 0.04f * s, 0.18f * s, 8);
                break;
            case 2:
                p.setColor(face);
                c.drawCircle(cx - 0.27f * s, fy - 0.24f * s, 0.11f * s, p);
                c.drawCircle(cx + 0.27f * s, fy - 0.24f * s, 0.11f * s, p);
                p.setColor(0xFFEBC9A0);
                c.drawCircle(cx - 0.27f * s, fy - 0.24f * s, 0.06f * s, p);
                c.drawCircle(cx + 0.27f * s, fy - 0.24f * s, 0.06f * s, p);
                break;
            case 3:
                p.setColor(0xFF2B2B2B);
                c.drawCircle(cx - 0.27f * s, fy - 0.25f * s, 0.11f * s, p);
                c.drawCircle(cx + 0.27f * s, fy - 0.25f * s, 0.11f * s, p);
                break;
            case 4:
                p.setColor(0xFFA86F45);
                oval(c, p, cx - 0.34f * s, fy - 0.02f * s, 0.095f * s, 0.2f * s, 14);
                oval(c, p, cx + 0.34f * s, fy - 0.02f * s, 0.095f * s, 0.2f * s, -14);
                break;
            case 6:
                p.setColor(face);
                oval(c, p, cx - 0.05f * s, fy - 0.33f * s, 0.035f * s, 0.07f * s, -20);
                oval(c, p, cx + 0.03f * s, fy - 0.35f * s, 0.035f * s, 0.075f * s, 8);
                oval(c, p, cx + 0.11f * s, fy - 0.32f * s, 0.032f * s, 0.065f * s, 28);
                break;
            case 7:
                p.setColor(face);
                c.drawCircle(cx - 0.19f * s, fy - 0.25f * s, 0.12f * s, p);
                c.drawCircle(cx + 0.19f * s, fy - 0.25f * s, 0.12f * s, p);
                break;
            default:
                break;
        }

        // face
        p.setColor(face);
        c.drawOval(new RectF(cx - 0.36f * s, fy - 0.31f * s, cx + 0.36f * s, fy + 0.31f * s), p);

        // face details
        if (k == 2) {
            p.setColor(0xFFEBC9A0);
            c.drawOval(new RectF(cx - 0.14f * s, fy + 0.0f * s, cx + 0.14f * s, fy + 0.2f * s), p);
        } else if (k == 5) {
            p.setColor(0xFFFFFFFF);
            oval(c, p, cx - 0.17f * s, fy + 0.15f * s, 0.17f * s, 0.12f * s, 20);
            oval(c, p, cx + 0.17f * s, fy + 0.15f * s, 0.17f * s, 0.12f * s, -20);
        } else if (k == 4) {
            p.setColor(0xFFA86F45);
            c.drawCircle(cx + 0.14f * s, fy - 0.05f * s, 0.085f * s, p);
        } else if (k == 3) {
            p.setColor(0xFF2B2B2B);
            oval(c, p, cx - 0.14f * s, fy - 0.03f * s, 0.075f * s, 0.1f * s, 22);
            oval(c, p, cx + 0.14f * s, fy - 0.03f * s, 0.075f * s, 0.1f * s, -22);
        }

        // eyes
        if (k == 7) {
            for (int side = -1; side <= 1; side += 2) {
                float ex = cx + side * 0.19f * s, ey = fy - 0.25f * s;
                p.setColor(0xFFFFFFFF);
                c.drawCircle(ex, ey, 0.075f * s, p);
                p.setColor(0xFF2B2B2B);
                c.drawCircle(ex, ey, 0.045f * s, p);
                p.setColor(0xFFFFFFFF);
                c.drawCircle(ex + 0.014f * s, ey - 0.014f * s, 0.016f * s, p);
            }
        } else {
            for (int side = -1; side <= 1; side += 2) {
                float ex = cx + side * 0.14f * s, ey = fy - 0.03f * s;
                p.setColor(0xFF2B2B2B);
                c.drawCircle(ex, ey, 0.046f * s, p);
                p.setColor(0xFFFFFFFF);
                c.drawCircle(ex + 0.013f * s, ey - 0.014f * s, 0.016f * s, p);
            }
        }

        // blush
        p.setColor(0x88FF8FA3);
        c.drawCircle(cx - 0.24f * s, fy + 0.1f * s, 0.05f * s, p);
        c.drawCircle(cx + 0.24f * s, fy + 0.1f * s, 0.05f * s, p);

        // nose + mouth
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1.2f, s * 0.022f));
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(0xFF3B2A2A);
        if (k == 6) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFFF9F1C);
            tri(c, p, cx - 0.055f * s, fy + 0.04f * s, cx + 0.055f * s, fy + 0.04f * s, cx, fy + 0.12f * s);
        } else if (k == 7) {
            c.drawArc(new RectF(cx - 0.16f * s, fy - 0.03f * s, cx + 0.16f * s, fy + 0.17f * s), 15, 150, false, p);
        } else {
            p.setStyle(Paint.Style.FILL);
            p.setColor(k == 0 || k == 1 ? 0xFFFF7A9A : 0xFF3B2A2A);
            c.drawOval(new RectF(cx - 0.035f * s, fy + 0.035f * s, cx + 0.035f * s, fy + 0.08f * s), p);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(0xFF3B2A2A);
            c.drawArc(new RectF(cx - 0.075f * s, fy + 0.07f * s, cx, fy + 0.14f * s), 0, 180, false, p);
            c.drawArc(new RectF(cx, fy + 0.07f * s, cx + 0.075f * s, fy + 0.14f * s), 0, 180, false, p);
        }
        p.setStyle(Paint.Style.FILL);
        c.restore();
    }
}
