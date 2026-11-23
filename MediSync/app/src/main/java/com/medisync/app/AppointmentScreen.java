package com.medisync.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.database.MediSyncDatabase;
import com.medisync.app.model.Appointment;
import com.medisync.app.model.Patient;
import com.medisync.app.util.DateUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Appointment list, add/edit form, and the detail actions (complete, cancel, reopen, delete). */
final class AppointmentScreen {
    private AppointmentScreen() { }


    /** Upcoming (including overdue) or past appointments for the selected patient. */
    static void list(final MainActivity host) {
        LinearLayout b = host.page();
        final Patient p = host.needPatient(b);
        if (p == null) return;
        host.topBar(b, "Appointments", "Upcoming and past visits", p);
        final MediSyncDatabase db = host.db;

        LinearLayout chips = host.row();
        final String[] filters = {"Upcoming", "Past"};
        for (int i = 0; i < filters.length; i++) {
            final String fv = filters[i];
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, host.dp(34), 1);
            if (i == 0) cp.rightMargin = host.dp(6);
            chips.addView(host.chip(fv, host.apptFilter.equals(fv), () -> {
                host.apptFilter = fv;
                host.render();
            }), cp);
        }
        LinearLayout.LayoutParams cpp = host.lp(-1, -2);
        cpp.bottomMargin = host.dp(12);
        b.addView(chips, cpp);

        host.btn(b, "+  Add Appointment", () -> {
            host.editingAppointmentId = -1;
            host.go("appointment_form");
        });
        host.sp(b, 8);

        long now = System.currentTimeMillis();
        List<Appointment> upcoming = new ArrayList<>();
        List<Appointment> past = new ArrayList<>();
        for (Appointment a : db.appointments(p.getId())) {
            if (a.isScheduled()) upcoming.add(a);
            else past.add(a);
        }
        Collections.reverse(past); // newest first

        List<Appointment> shown = "Past".equals(host.apptFilter) ? past : upcoming;
        if (shown.isEmpty()) {
            b.addView(host.tv("Past".equals(host.apptFilter) ? "No completed or cancelled appointments yet."
                    : "No upcoming appointments. Tap Add Appointment to schedule one.", 13, host.MUTED, false));
        }
        for (final Appointment a : shown) {
            LinearLayout c = host.row();
            c.setPadding(host.dp(12), host.dp(12), host.dp(12), host.dp(12));
            c.setBackground(host.rr(host.CARD, host.BORDER, 12));
            LinearLayout t = host.col();
            t.addView(host.tv(a.getTitle(), 14, host.INK, true));
            String doctor = a.getDoctor().isEmpty() ? "" : " · " + a.getDoctor();
            t.addView(host.tv(DateUtils.formatDisplayDate(a.getDate()) + " · "
                    + DateUtils.formatMinutes(DateUtils.minutesOf24h(a.getTime())) + doctor, 12, host.MUTED, false));
            c.addView(t, host.wt(1));
            boolean overdue = a.isOverdue(now);
            String label = overdue ? "Overdue" : a.getStatus().getLabel();
            TextView badge = host.tv(label, 11, CalendarCells.statusColor(host, label), true);
            badge.setPadding(host.dp(8), 0, 0, 0);
            c.addView(badge);
            c.setOnClickListener(x -> AppointmentDetail.showDetail(host, a));
            LinearLayout.LayoutParams lp = host.lp(-1, -2);
            lp.bottomMargin = host.dp(8);
            b.addView(c, lp);
        }
    }

    /** Add (editingAppointmentId == -1) or edit an appointment. */
    static void form(final MainActivity host) {
        final MediSyncDatabase db = host.db;
        final Appointment existing = host.editingAppointmentId > 0 ? db.appointment(host.editingAppointmentId) : null;
        LinearLayout b = host.page();
        host.topBar(b, existing == null ? "New Appointment" : "Edit Appointment", null, null);
        final Patient p = host.curPatient();
        if (p == null) {
            host.needPatient(b);
            return;
        }
        LinearLayout f = host.card(b);

        host.lbl(f, "Title or purpose");
        final EditText title = host.input(f, "e.g. Follow-up check-up", null, false);
        host.lbl(f, "Doctor or clinic (optional)");
        final EditText doctor = host.input(f, "e.g. Dr. Santos, City Clinic", null, false);

        host.lbl(f, "Date");
        final String[] date = {existing != null ? existing.getDate() : DateUtils.today()};
        final TextView dateTxt = host.tv(DateUtils.formatDisplayDate(date[0]), 13, host.INK, false);
        LinearLayout dateBox = host.row();
        dateBox.setBackground(host.rr(host.CARD, host.BORDER, 10));
        dateBox.setPadding(host.dp(12), 0, host.dp(12), 0);
        dateBox.addView(dateTxt, host.wt(1));
        dateBox.setOnClickListener(x -> {
            int[] ymd = AppointmentDetail.ymd(date[0]);
            new DatePickerDialog(host, (picker, y, m, d) -> {
                date[0] = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                dateTxt.setText(DateUtils.formatDisplayDate(date[0]));
            }, ymd[0], ymd[1] - 1, ymd[2]).show();
        });
        LinearLayout.LayoutParams dl = host.lp(-1, 52);
        dl.bottomMargin = host.dp(10);
        f.addView(dateBox, dl);

        host.lbl(f, "Time");
        final int[] minute = {existing != null ? DateUtils.minutesOf24h(existing.getTime()) : 9 * 60};
        final TextView timeTxt = host.tv(DateUtils.formatMinutes(minute[0]), 13, host.INK, false);
        LinearLayout timeBox = host.row();
        timeBox.setBackground(host.rr(host.CARD, host.BORDER, 10));
        timeBox.setPadding(host.dp(12), 0, host.dp(12), 0);
        timeBox.addView(timeTxt, host.wt(1));
        timeBox.addView(new IconView(host, "clock", host.MUTED), host.lp(22, 22));
        timeBox.setOnClickListener(x -> new TimePickerDialog(host, (picker, h, m) -> {
            minute[0] = h * 60 + m;
            timeTxt.setText(DateUtils.formatMinutes(minute[0]));
        }, minute[0] / 60, minute[0] % 60, false).show());
        LinearLayout.LayoutParams tl = host.lp(-1, 52);
        tl.bottomMargin = host.dp(10);
        f.addView(timeBox, tl);

        host.lbl(f, "Notes (optional)");
        final EditText notes = host.input(f, "e.g. Bring the lab results", null, false);

        if (existing != null) {
            title.setText(existing.getTitle());
            doctor.setText(existing.getDoctor());
            notes.setText(existing.getNotes());
        }

        host.btn(f, "Save Appointment", () -> {
            String t = title.getText().toString().trim();
            String time = DateUtils.formatMinutes24h(minute[0]);
            long id;
            try {
                // the model validates title, date, and time before anything is written
                new Appointment(0, p.getId(), t, doctor.getText().toString(), date[0], time,
                        notes.getText().toString(), null);
                if (existing == null) {
                    id = db.addAppointment(p.getId(), t, doctor.getText().toString(), date[0], time,
                            notes.getText().toString());
                } else {
                    db.updateAppointment(existing.getId(), t, doctor.getText().toString(), date[0], time,
                            notes.getText().toString());
                    id = existing.getId();
                }
            } catch (IllegalArgumentException e) {
                host.toast(e.getMessage());
                return;
            }
            host.scheduleAll(); // respects the "Appointment Reminders" switch
            host.toast("Appointment saved.");
            host.editingAppointmentId = -1;
            host.tab("appointments");
        });
    }
}
