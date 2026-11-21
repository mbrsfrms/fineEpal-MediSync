package com.medisync.app;

import com.medisync.app.manager.AccountStore;
import com.medisync.app.manager.AuthManager;
import com.medisync.app.manager.CalendarBuilder;
import com.medisync.app.manager.HealthAlertService;
import com.medisync.app.manager.ReminderService;
import com.medisync.app.manager.StreakCalculator;
import com.medisync.app.manager.Validator;
import com.medisync.app.model.Appointment;
import com.medisync.app.model.CalendarEvent;
import com.medisync.app.model.HealthAlert;
import com.medisync.app.model.IntakeRecord;
import com.medisync.app.model.IntakeStatus;
import com.medisync.app.model.Medication;
import com.medisync.app.model.MedicationSchedule;
import com.medisync.app.model.Patient;
import com.medisync.app.model.Reminder;
import com.medisync.app.util.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Plain-Java tests for the domain layer (no Android, no JUnit needed).
 * Run from the project root:  see README "Running the domain tests".
 */
public final class DomainTests {
    private static int passed = 0;
    private static final List<String> failures = new ArrayList<>();

    interface Check { void run() throws Exception; }

    static void test(String name, Check c) {
        try {
            c.run();
            passed++;
        } catch (Throwable t) {
            failures.add(name + " -> " + t);
        }
    }

    static void eq(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    static void isTrue(boolean b) { if (!b) throw new AssertionError("expected true"); }

    static void throwsIae(Check c) {
        try { c.run(); } catch (IllegalArgumentException e) { return; } catch (Exception e) { throw new AssertionError("wrong exception " + e); }
        throw new AssertionError("expected IllegalArgumentException");
    }

    static void throwsIse(Check c) {
        try { c.run(); } catch (IllegalStateException e) { return; } catch (Exception e) { throw new AssertionError("wrong exception " + e); }
        throw new AssertionError("expected IllegalStateException");
    }

    static Medication med(long id, String name, String freq, String times, int remaining, String created) {
        return new Medication(id, 1, name, "500mg", "Tablet", freq, times, "Ongoing", "", created, remaining, remaining, true);
    }

    static String day(int d) { return String.format("2026-11-%02d", d); }

    static IntakeRecord rec(long id, long medId, String scheduled, String recorded, String status) {
        return new IntakeRecord(id, 1, medId, "Med", "500mg", scheduled, recorded, status);
    }

    // in-memory account store for AuthManager tests
    static final class MemStore implements AccountStore {
        final Map<Long, StoredAccount> rows = new java.util.LinkedHashMap<>();
        long next = 1;
        public StoredAccount findByUid(String uid) {
            for (StoredAccount a : rows.values()) if (a.uid.equals(uid)) return a;
            return null;
        }
        public StoredAccount findByEmail(String email) {
            for (StoredAccount a : rows.values()) if (a.email.equals(email)) return a;
            return null;
        }
        public long insert(String uid, String name, String email, String avatar) {
            if (findByEmail(email) != null || findByUid(uid) != null) return -1; // UNIQUE constraints
            long id = next++;
            rows.put(id, new StoredAccount(id, uid, name, email, avatar));
            return id;
        }
        public boolean linkUid(long id, String uid) {
            StoredAccount a = rows.get(id);
            if (a == null || !a.uid.isEmpty() || findByUid(uid) != null) return false;
            rows.put(id, new StoredAccount(a.id, uid, a.name, a.email, a.avatar));
            return true;
        }
        public boolean updateEmail(long id, String email) {
            StoredAccount a = rows.get(id);
            StoredAccount other = findByEmail(email);
            if (a == null || (other != null && other.id != id)) return false;
            rows.put(id, new StoredAccount(a.id, a.uid, a.name, email, a.avatar));
            return true;
        }
        void legacy(long id, String name, String email) { rows.put(id, new StoredAccount(id, "", name, email, "")); next = Math.max(next, id + 1); }
        void rename(long id, String name) { StoredAccount a = rows.get(id); rows.put(id, new StoredAccount(a.id, a.uid, name, a.email, a.avatar)); }
    }

    static AuthManager.GoogleIdentity gid(String uid, String email, boolean verified, String name) {
        return new AuthManager.GoogleIdentity(uid, email, verified, name, null);
    }

    public static void main(String[] args) {
        // ---------------------------------------------------------------- DateUtils
        test("parse 12-hour time", () -> eq(480, DateUtils.minutesOf("8:00 AM")));
        test("parse 12-hour PM time", () -> eq(14 * 60, DateUtils.minutesOf("2:00 PM")));
        test("12 PM is noon", () -> eq(720, DateUtils.minutesOf("12:00 PM")));
        test("12 AM is midnight", () -> eq(0, DateUtils.minutesOf("12:00 AM")));
        test("invalid time gives -1", () -> eq(-1, DateUtils.minutesOf("25:99")));
        test("parse time list sorted", () -> eq("[480, 840, 1200]", DateUtils.parseTimes("8:00 PM, 2:00 PM, 8:00 AM").toString()));
        test("format minutes", () -> { eq("8:00 AM", DateUtils.formatMinutes(480)); eq("12:00 AM", DateUtils.formatMinutes(0)); eq("12:30 PM", DateUtils.formatMinutes(750)); });
        test("format 24h", () -> eq("08:05", DateUtils.formatMinutes24h(485)));
        test("shift date across month end", () -> eq("2026-03-01", DateUtils.shiftDate("2026-02-28", 1)));
        test("shift date backwards across leap day", () -> eq("2028-02-29", DateUtils.shiftDate("2028-03-01", -1)));
        test("days between", () -> eq(7, DateUtils.daysBetween("2026-11-01", "2026-11-08")));
        test("age from date of birth", () -> {
            String dob = DateUtils.daysFromToday(-(365 * 10 + 3));
            int age = DateUtils.ageInYears(dob);
            isTrue(age == 10 || age == 9);
        });
        test("millis at minute of day", () -> isTrue(DateUtils.millisAt("2026-11-11", 480) > 0));

        // ---------------------------------------------------------------- Medication
        test("dosesPerDay twice daily", () -> eq(2.0, med(1, "A", "Twice daily", "8:00 AM, 8:00 PM", 10, "2026-01-01").dosesPerDay()));
        test("dosesPerDay uses listed times", () -> eq(3.0, med(1, "A", "Three times daily", "8:00 AM, 2:00 PM, 8:00 PM", 10, "2026-01-01").dosesPerDay()));
        test("days of supply", () -> eq(5, med(1, "A", "Twice daily", "8:00 AM, 8:00 PM", 10, "2026-01-01").daysOfSupplyLeft()));
        test("every other day supply", () -> eq(6, med(1, "A", "Every other day", "8:00 AM", 3, "2026-01-01").daysOfSupplyLeft()));
        test("as needed is not required for streak", () -> eq(0, med(1, "A", "As needed", "As needed", 5, "2026-01-01").dosesRequiredPerDay()));
        test("required doses for daily medicine", () -> eq(2, med(1, "A", "Twice daily", "8:00 AM, 8:00 PM", 10, "2026-01-01").dosesRequiredPerDay()));
        test("low stock threshold", () -> isTrue(med(1, "A", "Once daily", "8:00 AM", 3, "2026-01-01").isLowStock(3)));
        test("not low stock", () -> isTrue(!med(1, "A", "Once daily", "8:00 AM", 4, "2026-01-01").isLowStock(3)));
        test("negative remaining rejected", () -> throwsIae(() -> med(1, "A", "Once daily", "8:00 AM", 3, "2026-01-01").setRemaining(-1)));
        test("blank medication name rejected", () -> throwsIae(() -> new Medication(1, 1, " ", "500mg", "Tablet", "Once daily", "", "", "", "", 1, 1, true)));
        test("started by date", () -> { Medication m = med(1, "A", "Once daily", "8:00 AM", 3, "2026-05-10"); isTrue(m.isStartedBy("2026-05-10")); isTrue(!m.isStartedBy("2026-05-09")); });

        // ---------------------------------------------------------------- Intake classification
        test("taken near scheduled time is on time", () -> eq(IntakeStatus.ON_TIME, IntakeStatus.classifyTaken(480, 500)));
        test("taken 61 minutes late is late", () -> eq(IntakeStatus.LATE, IntakeStatus.classifyTaken(480, 541)));
        test("taken exactly 60 minutes late is on time", () -> eq(IntakeStatus.ON_TIME, IntakeStatus.classifyTaken(480, 540)));
        test("stored values map back", () -> { eq(IntakeStatus.ON_TIME, IntakeStatus.fromStored("Taken")); eq(IntakeStatus.LATE, IntakeStatus.fromStored("Late")); eq(null, IntakeStatus.fromStored("???")); });
        test("late counts as taken", () -> isTrue(IntakeStatus.LATE.countsAsTaken()));
        test("missed does not count", () -> isTrue(!IntakeStatus.MISSED.countsAsTaken()));

        // ---------------------------------------------------------------- MedicationSchedule
        test("schedule slots per dose time", () -> eq(2, MedicationSchedule.createFor(med(1, "A", "Twice daily", "8:00 AM, 8:00 PM", 10, "2026-01-01"), "2026-11-11").size()));
        test("as needed has no schedule slots", () -> eq(0, MedicationSchedule.createFor(med(1, "A", "As needed", "As needed", 10, "2026-01-01"), "2026-11-11").size()));
        test("slot can be marked once only", () -> {
            MedicationSchedule s = new MedicationSchedule(1, "2026-11-11", 480);
            s.markAsTaken(490);
            eq(MedicationSchedule.Status.TAKEN, s.getStatus());
            throwsIse(() -> s.markAsSkipped());
        });
        test("late schedule status", () -> { MedicationSchedule s = new MedicationSchedule(1, "2026-11-11", 480); s.markAsTaken(700); eq(MedicationSchedule.Status.LATE, s.getStatus()); });
        test("pending past dose is overdue", () -> isTrue(new MedicationSchedule(1, "2026-11-10", 480).isOverdue("2026-11-11", 100)));
        test("pending dose later today not overdue", () -> isTrue(!new MedicationSchedule(1, "2026-11-11", 900).isOverdue("2026-11-11", 600)));

        // ---------------------------------------------------------------- Appointment
        test("appointment overdue after time passes", () -> {
            Appointment a = new Appointment(1, 1, "Checkup", "Dr. Cruz", "2026-11-10", "09:00", "", Appointment.Status.SCHEDULED);
            isTrue(a.isOverdue(DateUtils.millisAt("2026-11-11", 600)));
        });
        test("completed appointment never overdue", () -> {
            Appointment a = new Appointment(1, 1, "Checkup", "", "2026-11-10", "09:00", "", Appointment.Status.COMPLETED);
            isTrue(!a.isOverdue(DateUtils.millisAt("2026-11-11", 600)));
        });
        test("cancelled appointment never overdue", () -> {
            Appointment a = new Appointment(1, 1, "Checkup", "", "2026-11-10", "09:00", "", Appointment.Status.SCHEDULED);
            a.cancel();
            isTrue(!a.isOverdue(DateUtils.millisAt("2026-11-11", 600)));
            throwsIse(() -> a.markCompleted());
        });
        test("appointment rejects blank title", () -> throwsIae(() -> new Appointment(1, 1, "  ", "", "2026-11-10", "09:00", "", null)));
        test("appointment rejects bad date", () -> throwsIae(() -> new Appointment(1, 1, "X", "", "2026-13-40", "09:00", "", null)));
        test("appointment rejects bad time", () -> throwsIae(() -> new Appointment(1, 1, "X", "", "2026-11-10", "25:00", "", null)));
        test("appointment complete then reopen", () -> {
            Appointment a = new Appointment(1, 1, "X", "", "2026-11-10", "09:00", "", null);
            a.markCompleted(); eq(Appointment.Status.COMPLETED, a.getStatus()); a.reopen(); isTrue(a.isScheduled());
        });

        // ---------------------------------------------------------------- Polymorphic reminders
        test("reminders are polymorphic", () -> {
            Medication low = med(2, "Vitamin D3", "Once daily", "8:00 AM", 2, "2026-01-01");
            Medication ok = med(3, "Paracetamol", "Twice daily", "8:00 AM, 8:00 PM", 50, "2026-01-01");
            List<Medication> meds = new ArrayList<>();
            meds.add(low); meds.add(ok);
            List<Appointment> appts = new ArrayList<>();
            appts.add(new Appointment(9, 1, "Follow-up", "Dr. Cruz", "2026-11-11", "18:00", "", null));
            long now = DateUtils.millisAt("2026-11-11", 9 * 60);
            List<Reminder> rs = ReminderService.build(1, meds, new ArrayList<>(), appts, "2026-11-11", 9 * 60, now);
            boolean sawMed = false, sawRestock = false, sawAppt = false;
            for (Reminder r : rs) {
                String msg = r.buildMessage(); // dynamic dispatch on the actual subclass
                isTrue(msg != null && !msg.isEmpty());
                switch (r.getType()) {
                    case MEDICATION: sawMed = true; break;
                    case RESTOCK: sawRestock = true; break;
                    case APPOINTMENT: sawAppt = true; break;
                }
            }
            isTrue(sawMed && sawRestock && sawAppt);
        });
        test("reminder keys differ per day", () -> {
            Medication m = med(2, "Vitamin D3", "Once daily", "8:00 AM", 2, "2026-01-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            long now = DateUtils.millisAt("2026-11-11", 8 * 60);
            Reminder a = ReminderService.build(1, meds, new ArrayList<>(), new ArrayList<>(), "2026-11-11", 8 * 60, now).get(0);
            Reminder b = ReminderService.build(1, meds, new ArrayList<>(), new ArrayList<>(), "2026-11-12", 8 * 60, now).get(0);
            isTrue(!a.getKey().equals(b.getKey()));
        });
        test("taken dose removes its reminder", () -> {
            Medication m = med(2, "Vitamin D3", "Once daily", "8:00 AM", 20, "2026-01-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            List<IntakeRecord> h = new ArrayList<>();
            h.add(rec(1, 2, "2026-11-11 08:00", "2026-11-11 08:05", "Taken"));
            long now = DateUtils.millisAt("2026-11-11", 8 * 60);
            isTrue(ReminderService.build(1, meds, h, new ArrayList<>(), "2026-11-11", 8 * 60, now).isEmpty());
        });

        // ---------------------------------------------------------------- Streak
        test("streak counts consecutive complete days", () -> {
            Medication m = med(1, "A", "Once daily", "8:00 AM", 10, "2026-11-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            List<IntakeRecord> h = new ArrayList<>();
            for (int d = 8; d <= 10; d++) h.add(rec(d, 1, day(d) + " 08:00", day(d) + " 08:10", "Taken"));
            Set<String> done = StreakCalculator.completeDays(meds, h);
            eq(3, StreakCalculator.currentStreak(done, "2026-11-11"));
        });
        test("unfinished today does not break streak", () -> {
            Medication m = med(1, "A", "Once daily", "8:00 AM", 10, "2026-11-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            List<IntakeRecord> h = new ArrayList<>();
            for (int d = 9; d <= 10; d++) h.add(rec(d, 1, day(d) + " 08:00", day(d) + " 08:10", "Taken"));
            eq(2, StreakCalculator.currentStreak(StreakCalculator.completeDays(meds, h), "2026-11-11"));
        });
        test("missed dose breaks streak", () -> {
            Medication m = med(1, "A", "Once daily", "8:00 AM", 10, "2026-11-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            List<IntakeRecord> h = new ArrayList<>();
            h.add(rec(1, 1, "2026-11-09 08:00", "2026-11-09 08:00", "Taken"));
            h.add(rec(2, 1, "2026-11-10 08:00", "2026-11-10 08:00", "Missed"));
            h.add(rec(3, 1, "2026-11-11 08:00", "2026-11-11 08:00", "Taken"));
            eq(1, StreakCalculator.currentStreak(StreakCalculator.completeDays(meds, h), "2026-11-11"));
        });
        test("late dose still keeps streak", () -> {
            Medication m = med(1, "A", "Once daily", "8:00 AM", 10, "2026-11-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            List<IntakeRecord> h = new ArrayList<>();
            h.add(rec(1, 1, "2026-11-10 08:00", "2026-11-10 10:30", "Late"));
            eq(1, StreakCalculator.currentStreak(StreakCalculator.completeDays(meds, h), "2026-11-10"));
        });
        test("partial day is not complete", () -> {
            Medication m = med(1, "A", "Twice daily", "8:00 AM, 8:00 PM", 10, "2026-11-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            List<IntakeRecord> h = new ArrayList<>();
            h.add(rec(1, 1, "2026-11-10 08:00", "2026-11-10 08:00", "Taken"));
            isTrue(StreakCalculator.completeDays(meds, h).isEmpty());
        });
        test("medicine added today does not count for earlier days", () -> {
            Medication m = med(1, "A", "Once daily", "8:00 AM", 10, "2026-11-11");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            int[] pr = StreakCalculator.dayProgress(meds, new ArrayList<>(), "2026-11-10");
            eq(0, pr[1]);
        });

        // ---------------------------------------------------------------- Calendar
        test("calendar shows taken, due and upcoming doses", () -> {
            Medication m = med(1, "Paracetamol", "Twice daily", "8:00 AM, 8:00 PM", 10, "2026-11-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            List<IntakeRecord> h = new ArrayList<>();
            h.add(rec(1, 1, "2026-11-11 08:00", "2026-11-11 08:05", "Taken"));
            Map<String, List<CalendarEvent>> cal = CalendarBuilder.buildMonth(2026, 11, "2026-11-11", 9 * 60,
                    DateUtils.millisAt("2026-11-11", 9 * 60), meds, h, new ArrayList<>());
            List<CalendarEvent> today = cal.get("2026-11-11");
            eq(2, today.size());
            eq("Taken", today.get(0).getStatus());
            eq("Upcoming", today.get(1).getStatus());
        });
        test("calendar marks past undone dose as missed", () -> {
            Medication m = med(1, "Paracetamol", "Once daily", "8:00 AM", 10, "2026-11-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            Map<String, List<CalendarEvent>> cal = CalendarBuilder.buildMonth(2026, 11, "2026-11-11", 9 * 60,
                    DateUtils.millisAt("2026-11-11", 9 * 60), meds, new ArrayList<>(), new ArrayList<>());
            eq("Missed", cal.get("2026-11-10").get(0).getStatus());
        });
        test("calendar matches legacy record without scheduled time", () -> {
            Medication m = med(1, "Paracetamol", "Once daily", "8:00 AM", 10, "2026-11-01");
            List<Medication> meds = new ArrayList<>(); meds.add(m);
            List<IntakeRecord> h = new ArrayList<>();
            h.add(rec(1, 1, "", "2026-11-10 07:55", "Taken"));
            Map<String, List<CalendarEvent>> cal = CalendarBuilder.buildMonth(2026, 11, "2026-11-11", 9 * 60,
                    DateUtils.millisAt("2026-11-11", 9 * 60), meds, h, new ArrayList<>());
            eq("Taken", cal.get("2026-11-10").get(0).getStatus());
        });
        test("calendar includes overdue appointment", () -> {
            List<Appointment> appts = new ArrayList<>();
            appts.add(new Appointment(5, 1, "Lab follow-up", "", "2026-11-09", "10:00", "", null));
            Map<String, List<CalendarEvent>> cal = CalendarBuilder.buildMonth(2026, 11, "2026-11-11", 9 * 60,
                    DateUtils.millisAt("2026-11-11", 9 * 60), new ArrayList<>(), new ArrayList<>(), appts);
            eq("Overdue", cal.get("2026-11-09").get(0).getStatus());
            eq(CalendarEvent.Type.APPOINTMENT, cal.get("2026-11-09").get(0).getType());
        });
        test("leap year February length", () -> { eq(29, CalendarBuilder.lengthOfMonth(2028, 2)); eq(28, CalendarBuilder.lengthOfMonth(2026, 2)); });

        // ---------------------------------------------------------------- Health alerts
        test("allergy and overdue and low stock alerts", () -> {
            Patient p = new Patient(1, 1, "Maria", "Child", "Daughter", "2017-03-12", "", "Paracetamol, peanuts");
            List<Medication> meds = new ArrayList<>();
            meds.add(med(1, "Paracetamol", "Once daily", "8:00 AM", 2, "2026-01-01"));
            List<Appointment> appts = new ArrayList<>();
            appts.add(new Appointment(1, 1, "Checkup", "", "2026-11-01", "09:00", "", null));
            List<HealthAlert> alerts = HealthAlertService.build(p, meds, appts, DateUtils.millisAt("2026-11-11", 600));
            int allergy = 0, overdue = 0, low = 0;
            for (HealthAlert a : alerts) {
                switch (a.getKind()) {
                    case ALLERGY: allergy++; break;
                    case OVERDUE_APPOINTMENT: overdue++; break;
                    case LOW_STOCK: low++; break;
                }
            }
            eq(2, allergy); eq(1, overdue); eq(1, low);
        });
        test("no allergies no allergy alert", () -> {
            Patient p = new Patient(1, 1, "Alex", "Adult", "Self", "", "", "");
            eq(0, HealthAlertService.build(p, new ArrayList<>(), new ArrayList<>(), 0L).size());
        });

        // ---------------------------------------------------------------- Validation
        test("valid emails accepted", () -> {
            for (String e : new String[]{"alex@email.com", "first.last+tag@sub.example.ph", "a_b-c@x.io"})
                eq(null, Validator.emailError(e));
        });
        test("invalid emails rejected", () -> {
            for (String e : new String[]{"", "alex", "alex@", "@email.com", "alex@email", "alex@@email.com", "a b@c.com"})
                isTrue(Validator.emailError(e) != null);
        });

        // ---------------------------------------------------------------- Google account mapping
        test("first Google sign-in creates one account from the verified identity", () -> {
            MemStore st = new MemStore();
            AuthManager.Result r = new AuthManager(st).signInWithGoogle(gid("uidA", "Sam@Gmail.com", true, "  Sam Cruz "));
            eq(AuthManager.Outcome.SUCCESS, r.outcome);
            isTrue(r.newAccount);
            eq("sam@gmail.com", r.user.getEmail());
            eq("Sam Cruz", r.user.getName());
            eq(1, st.rows.size());
            eq("uidA", st.rows.get(r.user.getId()).uid);
            eq("", st.rows.get(r.user.getId()).avatar);
        });
        test("returning user: same UID gives same account, no duplicate", () -> {
            MemStore st = new MemStore();
            AuthManager am = new AuthManager(st);
            long first = am.signInWithGoogle(gid("uidA", "sam@gmail.com", true, "Sam")).user.getId();
            AuthManager.Result again = am.signInWithGoogle(gid("uidA", "sam@gmail.com", true, "Sam"));
            eq(first, again.user.getId());
            isTrue(!again.newAccount);
            eq(1, st.rows.size());
        });
        test("returning user keeps a name they edited in MediSync", () -> {
            MemStore st = new MemStore();
            AuthManager am = new AuthManager(st);
            long id = am.signInWithGoogle(gid("uidA", "sam@gmail.com", true, "Sam")).user.getId();
            st.rename(id, "Mama Sam");
            eq("Mama Sam", am.signInWithGoogle(gid("uidA", "sam@gmail.com", true, "Sam Cruz")).user.getName());
        });
        test("different Google accounts get different MediSync accounts", () -> {
            MemStore st = new MemStore();
            AuthManager am = new AuthManager(st);
            long a = am.signInWithGoogle(gid("uidA", "a@gmail.com", true, "A")).user.getId();
            long b = am.signInWithGoogle(gid("uidB", "b@gmail.com", true, "B")).user.getId();
            isTrue(a != b);
            eq(2, st.rows.size());
            eq(a, am.signInWithGoogle(gid("uidA", "a@gmail.com", true, "A")).user.getId());
            eq(b, am.signInWithGoogle(gid("uidB", "b@gmail.com", true, "B")).user.getId());
        });
        test("sign out and in again does not create a second account", () -> {
            MemStore st = new MemStore();
            AuthManager am = new AuthManager(st);
            long id = am.signInWithGoogle(gid("uidA", "a@gmail.com", true, "A")).user.getId();
            for (int i = 0; i < 5; i++) eq(id, am.signInWithGoogle(gid("uidA", "a@gmail.com", true, "A")).user.getId());
            eq(1, st.rows.size());
        });
        test("account from the email/password version keeps its records when its Google email signs in", () -> {
            MemStore st = new MemStore();
            st.legacy(7, "Alex", "alex@gmail.com");
            AuthManager.Result r = new AuthManager(st).signInWithGoogle(gid("uidA", "Alex@gmail.com", true, "Alex G"));
            eq(AuthManager.Outcome.SUCCESS, r.outcome);
            eq(7L, r.user.getId());
            isTrue(r.linkedExisting);
            isTrue(!r.newAccount);
            eq("Alex", r.user.getName());
            eq("uidA", st.rows.get(7L).uid);
            eq(1, st.rows.size());
        });
        test("an account already linked to one UID is not taken over by another UID with the same email", () -> {
            MemStore st = new MemStore();
            AuthManager am = new AuthManager(st);
            am.signInWithGoogle(gid("uidA", "a@gmail.com", true, "A"));
            eq(AuthManager.Outcome.EMAIL_CONFLICT, am.signInWithGoogle(gid("uidOther", "a@gmail.com", true, "X")).outcome);
            eq("uidA", st.rows.get(1L).uid);
        });
        test("unverified email is refused and nothing is stored", () -> {
            MemStore st = new MemStore();
            eq(AuthManager.Outcome.EMAIL_NOT_VERIFIED, new AuthManager(st).signInWithGoogle(gid("uidA", "a@gmail.com", false, "A")).outcome);
            eq(0, st.rows.size());
        });
        test("missing or malformed identity is refused", () -> {
            MemStore st = new MemStore();
            AuthManager am = new AuthManager(st);
            eq(AuthManager.Outcome.INVALID_IDENTITY, am.signInWithGoogle(null).outcome);
            eq(AuthManager.Outcome.INVALID_IDENTITY, am.signInWithGoogle(gid("", "a@gmail.com", true, "A")).outcome);
            eq(AuthManager.Outcome.INVALID_IDENTITY, am.signInWithGoogle(gid(null, "a@gmail.com", true, "A")).outcome);
            eq(AuthManager.Outcome.INVALID_IDENTITY, am.signInWithGoogle(gid("u", null, true, "A")).outcome);
            eq(AuthManager.Outcome.INVALID_IDENTITY, am.signInWithGoogle(gid("u", "not-an-email", true, "A")).outcome);
            eq(0, st.rows.size());
        });
        test("missing Google display name falls back to the email name", () -> {
            AuthManager.Result r = new AuthManager(new MemStore()).signInWithGoogle(gid("u", "jane.doe@gmail.com", true, null));
            eq("jane.doe", r.user.getName());
            eq("jane.doe", new AuthManager(new MemStore()).signInWithGoogle(gid("u", "jane.doe@gmail.com", true, "   ")).user.getName());
        });
        test("very long display name is shortened to the 60-character limit", () -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100; i++) sb.append('x');
            eq(null, Validator.nameError(new AuthManager(new MemStore()).signInWithGoogle(gid("u", "a@b.com", true, sb.toString())).user.getName()));
        });
        test("changed Google email for the same UID updates the stored email", () -> {
            MemStore st = new MemStore();
            AuthManager am = new AuthManager(st);
            long id = am.signInWithGoogle(gid("uidA", "old@gmail.com", true, "A")).user.getId();
            AuthManager.Result r = am.signInWithGoogle(gid("uidA", "new@gmail.com", true, "A"));
            eq(id, r.user.getId());
            eq("new@gmail.com", r.user.getEmail());
            eq(1, st.rows.size());
        });
        test("email change that clashes with another account keeps the old email and still signs in", () -> {
            MemStore st = new MemStore();
            AuthManager am = new AuthManager(st);
            long a = am.signInWithGoogle(gid("uidA", "a@gmail.com", true, "A")).user.getId();
            am.signInWithGoogle(gid("uidB", "b@gmail.com", true, "B"));
            AuthManager.Result r = am.signInWithGoogle(gid("uidA", "b@gmail.com", true, "A"));
            eq(AuthManager.Outcome.SUCCESS, r.outcome);
            eq(a, r.user.getId());
            eq("a@gmail.com", r.user.getEmail());
        });

        // ---------------------------------------------------------------- Results
        System.out.println("Passed: " + passed + "  Failed: " + failures.size());
        for (String f : failures) System.out.println("  FAIL " + f);
        if (!failures.isEmpty()) System.exit(1);
    }
}
