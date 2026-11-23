package com.medisync.app;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.medisync.app.model.*;
import com.medisync.app.util.DateUtils;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Formatting helpers, medicine/patient lookups and pill graphics. */
abstract class FormatHelpers extends AppHooks {
    static String fmtDate(String s) { return DateUtils.formatDisplayDate(s); }

    static String age(String dob) { int a = DateUtils.ageInYears(dob); return a < 0 ? "" : a + " years old"; }

    static String sub(Patient p) {
        String a = (p.getDateOfBirth() == null || p.getDateOfBirth().isEmpty()) ? "" : age(p.getDateOfBirth());
        return p.getCategory() + (a.isEmpty() ? "" : " \u00B7 " + a);
    }

    static int avType(Patient p) {
        String r = p.getRelationship() == null ? "" : p.getRelationship().toLowerCase(Locale.US);
        if ("Elderly".equals(p.getCategory())) return 2;
        if ("Adult".equals(p.getCategory()) || r.equals("self")) return 3;
        if (r.contains("son") && !r.contains("daughter")) return 1;
        return 0;
    }

    static List<Integer> parseTimes(String s) { return DateUtils.parseTimes(s); }

    static String fmtMin(int m) { return DateUtils.formatMinutes(m); }

    static String makeTimes(String frq, int t, int intervalHours) {
        return Medication.buildTimes(frq, t, intervalHours);
    }

    static String intervalLabel(int hours) { return hours == 1 ? "Every 1 hour" : "Every " + hours + " hours"; }

    /** Gap choices that still fit every dose of the frequency inside one day. */
    static String[] intervalOptions(String frq) {
        int max = Medication.maxIntervalHours(frq);
        String[] o = new String[max];
        for (int i = 0; i < max; i++) o[i] = intervalLabel(i + 1);
        return o;
    }

    static int intervalFromLabel(String label, String frq) {
        try { return Integer.parseInt(label.split(" ")[1]); } catch (RuntimeException e) { return Medication.defaultIntervalHours(frq); }
    }

    static double perDayOf(String freq, String times) { return Medication.dosesPerDay(freq, times); }

    static int daysLeft(Medication m) { return m.daysOfSupplyLeft(); }

    static String shortFreq(String f) {
        if ("Once daily".equals(f)) return "1x daily";
        if ("Twice daily".equals(f)) return "2x daily";
        if ("Three times daily".equals(f)) return "3x daily";
        if ("Four times daily".equals(f)) return "4x daily";
        return f;
    }

    static String unit(String form) {
        if ("Tablet".equals(form)) return "tablets";
        if ("Capsule".equals(form)) return "capsules";
        return "doses";
    }

    static String remKey(long pid, String name) { return "rem_" + name + "_" + pid; }

    static String today() { return DateUtils.today(); }

    String[] nextDose(List<Medication> ms) {
        Calendar n = Calendar.getInstance();
        int now = n.get(Calendar.HOUR_OF_DAY) * 60 + n.get(Calendar.MINUTE);
        int bt = -1, bm = -1; Medication mt = null, mm = null;
        for (Medication m : ms) {
            if ("As needed".equals(m.getFrequency())) continue;
            for (int t : parseTimes(m.getTimes())) {
                if (t >= now && (bt < 0 || t < bt)) { bt = t; mt = m; }
                if (bm < 0 || t < bm) { bm = t; mm = m; }
            }
        }
        if (mt != null) return new String[]{"Today, " + fmtMin(bt), mt.getName() + " " + mt.getDosage()};
        if (mm != null) return new String[]{"Tomorrow, " + fmtMin(bm), mm.getName() + " " + mm.getDosage()};
        return null;
    }

    Patient curPatient() {
        List<Patient> ps = db.patients(userId);
        for (Patient p : ps) if (p.getId() == patientId) return p;
        if (!ps.isEmpty()) { patientId = ps.get(0).getId(); db.put("patient_id", "" + patientId); return ps.get(0); }
        return null;
    }

    Patient needPatient(LinearLayout b) {
        Patient p = curPatient();
        if (p == null) {
            b.addView(tv("No patient yet", 18, INK, true));
            sp(b, 6);
            b.addView(tv("Add the person you are caring for to get started.", 13, MUTED, false));
            btn(b, "+  Add Patient", () -> { careType = "Child"; addingDependent = false; go("caretype"); });
        }
        return p;
    }

    int[] pillColors(String name) {
        int[][] c = {{0xFF1A9FA3, 0xFFA6E6DC}, {0xFF2DB57A, 0xFFB6EBCF}, {0xFFEF8A1F, 0xFFFFD6A3}, {0xFF3B82F6, 0xFFC4D9FF}};
        return c[Math.abs(name.hashCode()) % 4];
    }

    View pillBox(String name, int size) {
        FrameLayout f = new FrameLayout(this);
        f.setBackground(rr(MINT, 0, 12));
        int[] c = pillColors(name);
        f.addView(new LogoView(this, c[0], c[1], false), new FrameLayout.LayoutParams(dp(size - 10), dp(size - 10), Gravity.CENTER));
        return f;
    }
}
