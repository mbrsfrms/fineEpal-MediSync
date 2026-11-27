package com.medisync.app;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.manager.CalendarBuilder;
import com.medisync.app.model.Appointment;
import com.medisync.app.model.CalendarEvent;
import com.medisync.app.model.Patient;
import com.medisync.app.util.DateUtils;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/** CalendarScreen */
final class CalendarScreen {
    private CalendarScreen() { }


    static void build(final MainActivity host) {
        LinearLayout b = host.page();
        final Patient p = host.needPatient(b);
        if (p == null) return;
        host.topBar(b, "LifeSync Calendar", "Medications · Appointments · Reminders", p);

        String today = DateUtils.today();
        if (host.calYear == 0) {
            Calendar c = Calendar.getInstance();
            host.calYear = c.get(Calendar.YEAR);
            host.calMonth = c.get(Calendar.MONTH) + 1;
        }
        if (host.calSelected.isEmpty()) host.calSelected = today;

        // month header with previous / next
        LinearLayout head = host.row();
        head.setPadding(0, 0, 0, host.dp(10));
        TextView prev = host.tv("‹", 22, host.TEAL, true);
        prev.setPadding(host.dp(12), 0, host.dp(12), 0);
        prev.setOnClickListener(x -> {
            host.calMonth--;
            if (host.calMonth < 1) { host.calMonth = 12; host.calYear--; }
            host.render();
        });
        TextView next = host.tv("›", 22, host.TEAL, true);
        next.setPadding(host.dp(12), 0, host.dp(12), 0);
        next.setOnClickListener(x -> {
            host.calMonth++;
            if (host.calMonth > 12) { host.calMonth = 1; host.calYear++; }
            host.render();
        });
        TextView title = host.tv(monthName(host.calMonth) + " " + host.calYear, 17, host.INK, true);
        title.setGravity(Gravity.CENTER);
        head.addView(prev);
        head.addView(title, host.wt(1));
        head.addView(next);
        b.addView(head);

        long nowMillis = System.currentTimeMillis();
        int nowMinute = DateUtils.nowMinutes();
        Map<String, List<CalendarEvent>> events = CalendarBuilder.buildMonth(host.calYear, host.calMonth, today,
                nowMinute, nowMillis, host.db.meds(p.getId(), true), host.db.history(p.getId()),
                host.db.appointments(p.getId()));

        LinearLayout grid = host.card(b);
        LinearLayout week = host.row();
        String[] names = {"S", "M", "T", "W", "T", "F", "S"};
        for (String n : names) {
            TextView t = host.tv(n, 11, host.MUTED, true);
            t.setGravity(Gravity.CENTER);
            week.addView(t, host.wt(1));
        }
        grid.addView(week);

        Calendar first = Calendar.getInstance();
        first.set(host.calYear, host.calMonth - 1, 1);
        int leading = first.get(Calendar.DAY_OF_WEEK) - 1;
        int days = CalendarBuilder.lengthOfMonth(host.calYear, host.calMonth);
        int cell = 0;
        LinearLayout rowView = null;
        for (int i = 0; i < leading; i++) {
            if (cell % 7 == 0) { rowView = host.row(); grid.addView(rowView); }
            rowView.addView(new View(host), new LinearLayout.LayoutParams(0, 0, 1));
            cell++;
        }
        for (int d = 1; d <= days; d++) {
            if (cell % 7 == 0) { rowView = host.row(); grid.addView(rowView); }
            final String date = String.format(java.util.Locale.US, "%04d-%02d-%02d", host.calYear, host.calMonth, d);
            rowView.addView(CalendarCells.dayCell(host, d, date, today, events.get(date)), host.wt(1));
            cell++;
        }
        while (cell % 7 != 0) {
            rowView.addView(new View(host), new LinearLayout.LayoutParams(0, 0, 1));
            cell++;
        }

        // legend
        LinearLayout legend = host.row();
        legend.setPadding(0, host.dp(10), 0, 0);
        legend.addView(CalendarCells.legendItem(host, host.TEAL, "Medicine"));
        legend.addView(CalendarCells.legendItem(host, host.ORANGE, "Appointment"));
        legend.addView(CalendarCells.legendItem(host, host.RED, "Missed / overdue"));
        grid.addView(legend);

        // activities for the selected day
        LinearLayout list = host.card(b);
        list.addView(host.tv("Activities · " + DateUtils.formatDisplayDate(host.calSelected), 14, host.INK, true));
        host.sp(list, 8);
        List<CalendarEvent> dayEvents = events.get(host.calSelected);
        if (dayEvents == null) {
            // selected date may be outside the month shown; build it on demand
            Map<String, List<CalendarEvent>> one = CalendarBuilder.build(host.calSelected, host.calSelected, today,
                    nowMinute, nowMillis, host.db.meds(p.getId(), true), host.db.history(p.getId()),
                    host.db.appointments(p.getId()));
            dayEvents = one.get(host.calSelected);
        }
        if (dayEvents == null || dayEvents.isEmpty()) {
            list.addView(host.tv("Nothing scheduled for this day.", 13, host.MUTED, false));
        } else {
            for (CalendarEvent e : dayEvents) list.addView(CalendarCells.eventRow(host, e));
        }
    }

    static String monthName(int month) {
        return new java.text.DateFormatSymbols(java.util.Locale.US).getMonths()[month - 1];
    }
}