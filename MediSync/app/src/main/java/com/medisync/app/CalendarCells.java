package com.medisync.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.model.Appointment;
import com.medisync.app.model.CalendarEvent;
import com.medisync.app.model.Medication;
import com.medisync.app.util.DateUtils;
import java.util.List;

/** Calendar day cells, legend and event rows. */
final class CalendarCells {
    private CalendarCells() { }

    static View dayCell(final MainActivity host, int dayNumber, final String date, String today,
                                List<CalendarEvent> events) {
        LinearLayout cell = new LinearLayout(host);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);
        cell.setPadding(0, host.dp(4), 0, host.dp(4));
        boolean isToday = date.equals(today);
        boolean selected = date.equals(host.calSelected);
        if (selected) cell.setBackground(host.rr(host.MINT, 0, 10));

        TextView num = host.tv(String.valueOf(dayNumber), 13, isToday ? Color.WHITE : host.INK, isToday);
        num.setGravity(Gravity.CENTER);
        if (isToday) {
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(host.TEAL);
            num.setBackground(circle);
        }
        cell.addView(num, host.lp(30, 30));

        LinearLayout dots = host.row();
        dots.setGravity(Gravity.CENTER);
        boolean med = false, appt = false, alert = false;
        if (events != null) {
            for (CalendarEvent e : events) {
                if (e.getType() == CalendarEvent.Type.MEDICATION) {
                    if ("Missed".equals(e.getStatus())) alert = true; else med = true;
                } else if (e.getType() == CalendarEvent.Type.APPOINTMENT) {
                    if ("Overdue".equals(e.getStatus())) alert = true; else appt = true;
                } else {
                    alert = true;
                }
            }
        }
        if (med) dots.addView(dot(host, host.TEAL));
        if (appt) dots.addView(dot(host, host.ORANGE));
        if (alert) dots.addView(dot(host, host.RED));
        cell.addView(dots, host.lp(-1, 8));

        cell.setOnClickListener(x -> {
            host.calSelected = date;
            host.render();
        });
        return cell;
    }

    static View dot(MainActivity host, int color) {
        View v = new View(host);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        v.setBackground(g);
        LinearLayout.LayoutParams lp = host.lp(5, 5);
        lp.leftMargin = host.dp(1);
        lp.rightMargin = host.dp(1);
        v.setLayoutParams(lp);
        return v;
    }

    static LinearLayout legendItem(MainActivity host, int color, String label) {
        LinearLayout l = host.row();
        l.addView(dot(host, color));
        TextView t = host.tv(label, 10, host.MUTED, false);
        t.setPadding(host.dp(4), 0, host.dp(10), 0);
        l.addView(t);
        return l;
    }

    static LinearLayout eventRow(final MainActivity host, final CalendarEvent e) {
        LinearLayout r = host.row();
        r.setPadding(host.dp(10), host.dp(10), host.dp(10), host.dp(10));
        r.setBackground(host.rr(host.CARD, host.BORDER, 12));
        LinearLayout t = host.col();
        String time = e.getMinuteOfDay() >= 0 ? DateUtils.formatMinutes(e.getMinuteOfDay()) : "All day";
        t.addView(host.tv(time, 11, host.MUTED, false));
        t.addView(host.tv(e.getTitle(), 14, host.INK, true));
        if (!e.getDetail().isEmpty()) t.addView(host.tv(e.getDetail(), 11, host.MUTED, false));
        r.addView(t, host.wt(1));
        TextView badge = host.tv(e.getStatus(), 11, statusColor(host, e.getStatus()), true);
        badge.setPadding(host.dp(6), 0, 0, 0);
        r.addView(badge);
        r.setOnClickListener(x -> {
            if (e.getType() == CalendarEvent.Type.APPOINTMENT) {
                Appointment a = host.db.appointment(e.getRefId());
                if (a != null) AppointmentDetail.showDetail(host, a);
            } else {
                Medication m = host.db.med(e.getRefId());
                if (m != null) host.medDialog(m);
            }
        });
        LinearLayout.LayoutParams lp = host.lp(-1, -2);
        lp.bottomMargin = host.dp(8);
        r.setLayoutParams(lp);
        return r;
    }

    static int statusColor(MainActivity host, String status) {
        switch (status) {
            case "Taken":
            case "Completed":
                return host.GREEN;
            case "Late":
            case "Due":
            case "Low supply":
            case "Refill soon":
                return host.ORANGE;
            case "Missed":
            case "Overdue":
                return host.RED;
            default:
                return host.MUTED;
        }
    }
}
