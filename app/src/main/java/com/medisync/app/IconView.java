package com.medisync.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Draws the small line icons used across the app. */
class IconView extends View {
    String type;
    int color;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Path path = new Path();

    IconView(Context c, String t, int col) { super(c); type = t; color = col; }

    void set(String t, int col) { type = t; color = col; invalidate(); }

    void stroke(Canvas c) { c.drawPath(path, p); path.reset(); }

    @Override
    protected void onDraw(Canvas c) {
        float s = Math.min(getWidth(), getHeight());
        if (s <= 0) return;
        c.save();
        c.translate((getWidth() - s) / 2f, (getHeight() - s) / 2f);
        c.scale(s / 24f, s / 24f);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.8f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        path.reset();
        switch (type) {
            case "home":
                path.moveTo(3, 11); path.lineTo(12, 3.5f); path.lineTo(21, 11); stroke(c);
                path.moveTo(5.5f, 9.5f); path.lineTo(5.5f, 20); path.lineTo(18.5f, 20); path.lineTo(18.5f, 9.5f); stroke(c);
                path.moveTo(10, 20); path.lineTo(10, 14); path.lineTo(14, 14); path.lineTo(14, 20); stroke(c);
                break;
            case "pill":
                c.save(); c.rotate(-45, 12, 12);
                c.drawRoundRect(new RectF(3, 8, 21, 16), 4, 4, p);
                c.drawLine(12, 8, 12, 16, p);
                c.restore();
                break;
            case "clock":
                c.drawCircle(12, 12, 9, p);
                path.moveTo(12, 7); path.lineTo(12, 12); path.lineTo(15.5f, 14); stroke(c);
                break;
            case "grid":
                c.drawRoundRect(new RectF(4, 4, 10, 10), 1.5f, 1.5f, p);
                c.drawRoundRect(new RectF(14, 4, 20, 10), 1.5f, 1.5f, p);
                c.drawRoundRect(new RectF(4, 14, 10, 20), 1.5f, 1.5f, p);
                c.drawRoundRect(new RectF(14, 14, 20, 20), 1.5f, 1.5f, p);
                break;
            case "back": path.moveTo(15, 5); path.lineTo(8, 12); path.lineTo(15, 19); stroke(c); break;
            case "right": path.moveTo(9, 5); path.lineTo(16, 12); path.lineTo(9, 19); stroke(c); break;
            case "down": path.moveTo(5, 9); path.lineTo(12, 16); path.lineTo(19, 9); stroke(c); break;
            case "search": c.drawCircle(11, 11, 6.5f, p); c.drawLine(16, 16, 20.5f, 20.5f, p); break;
            case "lock":
                c.drawRoundRect(new RectF(5, 11, 19, 20.5f), 2.5f, 2.5f, p);
                c.drawArc(new RectF(8.5f, 4, 15.5f, 11), 180, 180, false, p);
                c.drawLine(8.5f, 7.5f, 8.5f, 11, p);
                c.drawLine(15.5f, 7.5f, 15.5f, 11, p);
                break;
            case "mail":
                c.drawRoundRect(new RectF(3, 5, 21, 19), 2.5f, 2.5f, p);
                path.moveTo(3.5f, 6.5f); path.lineTo(12, 13); path.lineTo(20.5f, 6.5f); stroke(c);
                break;
            case "person":
                c.drawCircle(12, 8, 3.8f, p);
                c.drawArc(new RectF(5, 14, 19, 28), 180, 180, false, p);
                break;
            case "eye": case "eyeoff":
                c.drawOval(new RectF(2.5f, 6.5f, 21.5f, 17.5f), p);
                c.drawCircle(12, 12, 2.6f, p);
                if (type.equals("eyeoff")) c.drawLine(4, 4, 20, 20, p);
                break;
            case "bell":
                path.moveTo(5, 18); path.lineTo(7, 16); path.lineTo(7, 11);
                path.arcTo(new RectF(7, 5, 17, 15), 180, 180, false);
                path.lineTo(17, 16); path.lineTo(19, 18); path.close(); stroke(c);
                c.drawArc(new RectF(10, 19, 14, 22.5f), 0, 180, false, p);
                break;
            case "check": path.moveTo(5, 12.5f); path.lineTo(10, 17.5f); path.lineTo(19, 7); stroke(c); break;
            case "checkc":
                c.drawCircle(12, 12, 9, p);
                path.moveTo(8, 12.5f); path.lineTo(11, 15.5f); path.lineTo(16.5f, 9); stroke(c);
                break;
            case "checkfill":
                p.setStyle(Paint.Style.FILL); c.drawCircle(12, 12, 10, p);
                p.setStyle(Paint.Style.STROKE); p.setColor(Color.WHITE); p.setStrokeWidth(2.2f);
                path.moveTo(7.5f, 12.5f); path.lineTo(10.5f, 15.5f); path.lineTo(16.5f, 9); stroke(c);
                break;
            case "alertfill":
                p.setStyle(Paint.Style.FILL); c.drawCircle(12, 12, 10, p);
                p.setColor(Color.WHITE); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2.4f);
                c.drawLine(12, 7, 12, 13, p);
                p.setStyle(Paint.Style.FILL); c.drawCircle(12, 16.5f, 1.4f, p);
                break;
            case "sun":
                c.drawCircle(12, 12, 4, p);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI / 4 * i;
                    c.drawLine((float) (12 + 7 * Math.cos(a)), (float) (12 + 7 * Math.sin(a)),
                            (float) (12 + 9.5 * Math.cos(a)), (float) (12 + 9.5 * Math.sin(a)), p);
                }
                break;
            case "info":
                c.drawCircle(12, 12, 9, p);
                c.drawLine(12, 11, 12, 17, p);
                p.setStyle(Paint.Style.FILL); c.drawCircle(12, 7.6f, 1.2f, p);
                break;
            case "logout":
                path.moveTo(9, 4); path.lineTo(5, 4); path.lineTo(5, 20); path.lineTo(9, 20); stroke(c);
                c.drawLine(10, 12, 20, 12, p);
                path.moveTo(16, 8); path.lineTo(20, 12); path.lineTo(16, 16); stroke(c);
                break;
            case "help":
                c.drawCircle(12, 12, 9, p);
                p.setStyle(Paint.Style.FILL); p.setTextSize(15); p.setTextAlign(Paint.Align.CENTER);
                c.drawText("?", 12, 16.5f, p);
                break;
            case "shield":
                path.moveTo(12, 3); path.lineTo(19, 6); path.lineTo(19, 12);
                path.cubicTo(19, 16.5f, 16, 19.5f, 12, 21);
                path.cubicTo(8, 19.5f, 5, 16.5f, 5, 12);
                path.lineTo(5, 6); path.close(); stroke(c);
                break;
            case "doc":
                path.moveTo(7, 3); path.lineTo(14, 3); path.lineTo(18, 7); path.lineTo(18, 21); path.lineTo(7, 21); path.close(); stroke(c);
                c.drawLine(10, 12, 15, 12, p); c.drawLine(10, 16, 15, 16, p);
                break;
            case "pencil":
                path.moveTo(4, 20); path.lineTo(5, 15); path.lineTo(16, 4); path.lineTo(20, 8); path.lineTo(9, 19); path.close(); stroke(c);
                c.drawLine(14, 6, 18, 10, p);
                break;
            case "plus": c.drawLine(12, 5, 12, 19, p); c.drawLine(5, 12, 19, 12, p); break;
            case "x": c.drawLine(6, 6, 18, 18, p); c.drawLine(18, 6, 6, 18, p); break;
            case "skip": c.drawLine(6, 12, 18, 12, p); break;
            case "warn":
                path.moveTo(12, 4); path.lineTo(21, 20); path.lineTo(3, 20); path.close(); stroke(c);
                c.drawLine(12, 10, 12, 14.5f, p);
                p.setStyle(Paint.Style.FILL); c.drawCircle(12, 17.2f, 1f, p);
                break;
            case "box":
                path.moveTo(4, 8); path.lineTo(12, 4); path.lineTo(20, 8); path.lineTo(20, 16); path.lineTo(12, 20); path.lineTo(4, 16); path.close(); stroke(c);
                path.moveTo(4, 8); path.lineTo(12, 12); path.lineTo(20, 8); stroke(c);
                c.drawLine(12, 12, 12, 20, p);
                break;
            case "calendar":
                c.drawRoundRect(new RectF(4, 5, 20, 20), 2.5f, 2.5f, p);
                c.drawLine(4, 10, 20, 10, p); c.drawLine(8.5f, 3, 8.5f, 7, p); c.drawLine(15.5f, 3, 15.5f, 7, p);
                break;
            case "heart":
                path.moveTo(12, 20.5f);
                path.cubicTo(4, 14.5f, 3, 9.5f, 6.5f, 6.5f);
                path.cubicTo(9, 4.5f, 11.5f, 6, 12, 8);
                path.cubicTo(12.5f, 6, 15, 4.5f, 17.5f, 6.5f);
                path.cubicTo(21, 9.5f, 20, 14.5f, 12, 20.5f);
                path.close(); stroke(c);
                break;
            case "flame":
                path.moveTo(12, 3); path.cubicTo(13, 7, 18, 9, 18, 14.5f);
                path.cubicTo(18, 18, 15.5f, 21, 12, 21);
                path.cubicTo(8.5f, 21, 6, 18, 6, 14.5f);
                path.cubicTo(6, 12, 7.5f, 10.5f, 8.5f, 9);
                path.cubicTo(9, 11, 10, 11.5f, 10.5f, 11.5f);
                path.cubicTo(10.5f, 8, 11, 5, 12, 3); path.close(); stroke(c);
                break;
            case "image":
                c.drawRoundRect(new RectF(3.5f, 4.5f, 20.5f, 19.5f), 3f, 3f, p);
                path.moveTo(4.5f, 17f); path.lineTo(9.5f, 11.5f); path.lineTo(13f, 15f);
                path.lineTo(15.5f, 12.5f); path.lineTo(19.5f, 17f); stroke(c);
                p.setStyle(Paint.Style.FILL); c.drawCircle(8.5f, 8.8f, 1.6f, p);
                break;
            default: break;
        }
        c.restore();
    }
}
