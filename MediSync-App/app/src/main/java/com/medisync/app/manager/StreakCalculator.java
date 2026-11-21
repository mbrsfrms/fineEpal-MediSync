package com.medisync.app.manager;

import com.medisync.app.model.IntakeRecord;
import com.medisync.app.model.Medication;
import com.medisync.app.util.DateUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Intake streak rules. A day is complete only when every required dose of every active medicine
 * that had started by that day was taken (taken on time or late). Missed and skipped doses break it.
 * Today is still in progress, so an unfinished today never breaks the streak.
 */
public final class StreakCalculator {
    public static final int MAX_STREAK_DAYS = 3650;

    private StreakCalculator() { }

    /** {taken, required} doses for one day. */
    public static int[] dayProgress(List<Medication> meds, List<IntakeRecord> history, String day) {
        int taken = 0, required = 0;
        for (Medication m : meds) {
            int need = m.dosesRequiredPerDay();
            if (need == 0 || !m.isStartedBy(day)) continue;
            int got = 0;
            for (IntakeRecord r : history) {
                if (r.getMedicationId() == m.getId() && r.isTaken() && day.equals(r.getDate())) got++;
            }
            required += need;
            taken += Math.min(need, got);
        }
        return new int[]{taken, required};
    }

    /** All dates on which the streak rule was satisfied. */
    public static Set<String> completeDays(List<Medication> activeMeds, List<IntakeRecord> history) {
        Set<String> candidates = new HashSet<>();
        for (IntakeRecord r : history) {
            if (r.isTaken() && r.getDate().length() == 10) candidates.add(r.getDate());
        }
        Set<String> done = new HashSet<>();
        for (String day : candidates) {
            int[] pr = dayProgress(activeMeds, history, day);
            if (pr[1] > 0 && pr[0] >= pr[1]) done.add(day);
        }
        return done;
    }

    /** Consecutive complete days ending today (or yesterday, if today is not finished yet). */
    public static int currentStreak(Set<String> completeDays, String today) {
        String day = today;
        if (!completeDays.contains(day)) day = DateUtils.shiftDate(today, -1);
        int streak = 0;
        while (completeDays.contains(day) && streak < MAX_STREAK_DAYS) {
            streak++;
            day = DateUtils.shiftDate(day, -1);
        }
        return streak;
    }
}
