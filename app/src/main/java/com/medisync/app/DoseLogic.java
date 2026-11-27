package com.medisync.app;

import android.content.Context;
import android.os.Build;
import com.medisync.app.database.MediSyncDatabase;
import com.medisync.app.manager.AlarmScheduler;
import com.medisync.app.manager.NotificationHelper;
import com.medisync.app.manager.StreakCalculator;
import com.medisync.app.model.*;
import com.medisync.app.util.DateUtils;
import java.util.HashSet;
import java.util.List;

/** Dose recording, notifications, streak helpers and the notification permission prompt. */
abstract class DoseLogic extends UiDialogs {
    // "As needed" and "Every other day" medicines are not required. A medicine only counts from the day it was added.
    static int dosesNeeded(Medication m) { return m.dosesRequiredPerDay(); }

    /** {taken, required} doses for one day. */
    int[] dayProgress(List<Medication> ms, List<IntakeRecord> hist, String day) {
        return StreakCalculator.dayProgress(ms, hist, day);
    }

    HashSet<String> completeDays(long pid) {
        return new HashSet<>(StreakCalculator.completeDays(db.meds(pid, true), db.history(pid)));
    }

    static int streakOf(HashSet<String> days) {
        return StreakCalculator.currentStreak(days, DateUtils.today());
    }

    // Scheduling lives in manager/AlarmScheduler, notifications in manager/NotificationHelper,
    // and the alarm receivers in receiver/. The methods below connect those to the screens.
    static void postNotification(Context c, int id, String title, String text) {
        NotificationHelper.showSupply(c, id, title, text);
    }

    void scheduleAll() { AlarmScheduler.rescheduleAll(this, userId); }

    void askNotif() {
        if (notifAsked) return;
        notifAsked = true;
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 7);
        }
    }

    /** The dose slot today closest to now that has no record yet, or -1 when every slot is recorded. */
    int nearestPendingSlot(Medication m) {
        int now = DateUtils.nowMinutes();
        String td = DateUtils.today();
        int best = -1;
        for (int t : m.getDoseMinutes()) {
            if (db.isSlotRecorded(m.getId(), td + " " + DateUtils.formatMinutes24h(t))) continue;
            if (best < 0 || Math.abs(t - now) < Math.abs(best - now)) best = t;
        }
        return best;
    }

    /** Records a dose from the medicine screen against the nearest unrecorded slot today. */
    void recordDose(Medication m, String status) {
        recordDoseAt(m, status, nearestPendingSlot(m));
    }

    /**
     * Records one dose. A taken dose is classified on time or late against its slot. slotMinute is -1 for an
     * unplanned dose. Supply is adjusted inside MediSyncDatabase.recordDose, so repeating a slot never subtracts twice.
     */
    void recordDoseAt(Medication m, String status, int slotMinute) {
        IntakeStatus st = IntakeStatus.fromStored(status);
        if (st == null) return;
        if (st == IntakeStatus.ON_TIME && slotMinute >= 0) {
            st = IntakeStatus.classifyTaken(slotMinute, DateUtils.nowMinutes());
        }
        String today = DateUtils.today();
        String scheduled = slotMinute >= 0 ? today + " " + DateUtils.formatMinutes24h(slotMinute) : "";
        long pid = m.getPatientId();
        boolean wasDone = completeDays(pid).contains(today);
        try {
            db.recordDose(pid, m, scheduled, DateUtils.now(), st);
        } catch (RuntimeException e) {
            toast("Could not save that dose. Please try again.");
            return;
        }
        if (st.countsAsTaken()) {
            Medication u = db.med(m.getId());
            if (u != null && u.isLowStock(Medication.LOW_STOCK_DAYS) && "1".equals(db.get(remKey(pid, "low"), "1"))
                    && "1".equals(db.get("notif_on", "1"))) {
                postNotification(this, (int) (5000 + m.getId()), "Low supply: " + u.getName(),
                        u.getRemaining() + " " + u.getUnitLabel() + " left (about " + u.daysOfSupplyLeft() + " day(s)).");
            }
        }
        HashSet<String> doneDays = completeDays(pid);
        if (st.countsAsTaken() && !wasDone && doneDays.contains(today)) {
            int s = streakOf(doneDays);
            toast("All doses taken today! 🔥 Streak: " + s + (s == 1 ? " day" : " days"));
        } else {
            toast("Dose recorded: " + st.getStoredValue());
        }
        render();
    }

    static boolean statusMatches(IntakeRecord r, String filter) {
        if ("Taken".equals(filter)) return r.isTaken() && !"Late".equals(r.getStatus());
        return filter.equals(r.getStatus());
    }
}
