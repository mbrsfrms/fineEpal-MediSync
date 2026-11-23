# MediSync (Android, Java)

Medication monitoring app for the BatStateU CICS **MediSync** project (see the official proposal).
This version is a revision of the earlier starter project. Work stayed on the existing codebase and design.

> **Verification status — read first.** The pure-Java domain layer (models, calculators, validation,
> Google-account mapping, calendar and reminder logic) was compiled and tested in a plain JVM:
> **74 of 74 tests pass**. The SQLite v4 to v5 upgrade statements and the per-account data separation were run against a
> real SQLite engine (13 checks pass). The Android layer (database class, screens, Firebase and Google sign-in code, alarms)
> was checked for syntax only. **It has not been compiled against the Android SDK, and Google Sign-In has not been run on a
> device or emulator**, because this environment has no Android SDK, no Gradle/Maven access and no Google account.
> Build it in Android Studio, finish `FIREBASE_SETUP.md`, and run the checklist in section 4 before relying on it.

---

## 1. Setup and run

1. Open the `MediSync` folder in **Android Studio** (Koala or newer). Use **JDK 17**.
2. Add `app/google-services.json` (see `FIREBASE_SETUP.md`), then let Gradle sync. Android Studio creates `local.properties` on first open. The Gradle wrapper is not included; if Android Studio reports it missing, run `gradle wrapper --gradle-version 8.9` in the project folder (needs a local Gradle install) or let Android Studio use its bundled Gradle.
3. Select an emulator or device running Android 6.0 (API 23) or newer, then press **Run**.
4. After signing in, allow notifications. On Android 12+, if the system asks, allow **Alarms & reminders**
   (exact alarms). Without it, dose alarms still fire but may be a few minutes late.

**Google sign-in needs your own Firebase project.** Follow `FIREBASE_SETUP.md` (enable Google provider, add SHA-1/SHA-256,
put `google-services.json` in `app/`). Medication data stays in the local database on the device.

### Running the domain tests (no Android needed)
From the project root:
```
mkdir -p build/domain
javac -d build/domain $(find app/src/main/java/com/medisync/app/model app/src/main/java/com/medisync/app/util app/src/main/java/com/medisync/app/manager -name '*.java' ! -name AlarmScheduler.java ! -name NotificationHelper.java) app/src/test/java/com/medisync/app/DomainTests.java
java -cp build/domain com.medisync.app.DomainTests
```
Expected output: `Passed: 75  Failed: 0`.

---

## 2. Package layout

| Package | Contents | Android dependency |
|---|---|---|
| `auth` | `GoogleSignIn` (Credential Manager + Firebase Authentication) | yes |
| `com.medisync.app` | `MainActivity` (navigation and screens), `CalendarScreen` (LifeSync Calendar), `AppointmentScreen` (appointments) | yes |
| `model` | `Person` (base), `User`, `Patient` (inheritance from `Person`); `Medication`, `MedicationSchedule`, `IntakeRecord`, `IntakeStatus`, `Appointment`; `Reminder` (abstract) with `MedicationReminder`, `RestockReminder`, `AppointmentReminder`; `HealthAlert`, `CalendarEvent` | no |
| `manager` | `AuthManager`, `AccountStore`, `Validator`, `StreakCalculator`, `CalendarBuilder`, `ReminderService`, `HealthAlertService`, `AlarmScheduler`, `NotificationHelper` | `AlarmScheduler`, `NotificationHelper` only |
| `database` | `MediSyncDatabase` (SQLite, schema v5, migrations) | yes |
| `receiver` | `ReminderReceiver` (dose due, "Mark taken", appointment due), `BootReceiver` (reboot, time or timezone change) | yes |
| `util` | `DateUtils`, `Keys` | no |

**OOP concepts used (proposal section II):**
- *Classes and objects*: every record type is a class; the database returns objects, not raw cursors.
- *Encapsulation*: private fields, getters, and validated setters (for example `Medication.setRemaining` rejects negatives).
- *Inheritance*: `User` and `Patient` extend `Person`; the three reminder types extend `Reminder`.
- *Polymorphism*: `ReminderService` returns `List<Reminder>`; the dashboard calls `describe()` and the notification code calls `buildMessage()` on each object, and each subclass supplies its own text.
- *Abstraction*: `Reminder` is abstract; `AccountStore` is an interface, implemented by `MediSyncDatabase` and by an in-memory fake in the tests.
- *Exception handling*: validation throws `IllegalArgumentException` with a message shown to the user; invalid database rows are skipped, not fatal; the alarm receiver catches runtime errors so a background alarm cannot crash the app.
- *Data handling*: lists and maps for collections; parameterized SQL everywhere; cursors closed in `finally`.

---

## 3. Feature status

| Proposal feature | Status | Notes |
|---|---|---|
| AI prescription scanning (OCR) | **Not implemented** | Proposal limitation 6 allows exclusion. No OCR library is included. |
| Prescription image upload | **Not implemented** | Profile photos use the system picker (no broad gallery permission). |
| Medication management (add, edit, delete, view, activate/deactivate, supply) | Working in code | Existing screens kept. Delete and deactivate now cancel the medicine's alarms. |
| Scheduling and reminders | Working in code | One exact alarm per dose time (previously one alarm per patient). Notification has a **Mark taken** action. |
| Intake tracking and history | Working in code | Taken on time, **Late** (more than 60 minutes after the slot), Missed, Skipped. Filter tab "Late" added. |
| Intake streak | Working in code | A day counts when every required dose was taken on time or late. Missed and skipped doses break it. An unfinished today does not. |
| Restock reminder | Working in code | Supply goes down once per dose slot. Changing a recorded dose adjusts supply only when its taken state changes. |
| Health risk flags | Working in code | Recorded allergies (new field per patient), medicine names matching an allergy, overdue appointments, low supply. |
| LifeSync Calendar | Working in code | Month grid, previous and next month, today and selected day highlighted, dots for medicine, appointment, and missed or overdue items. Tap an event for details. |
| Appointments | Working in code | Add, edit, delete, mark completed, cancel, reopen. Upcoming and Past tabs. Overdue flag. One-hour-before reminder. |
| Medication dashboard | Working in code | Streak, next dose, supply, upcoming appointments, reminders with **Dismiss**, health alerts. |
| Roles (self, parent or guardian, caretaker) and dependents | Kept from earlier version | Data is filtered per patient in every query. |
| Authentication | Google Sign-In via Firebase (needs setup) | See section 4. No MediSync passwords. |

---

## 4. Authentication (Google Sign-In with Firebase)

**How it works**
- The login screen has one button, **Continue with Google**. There is no email field, password field, sign-up screen or
  Change Password screen any more, and no password hashes are stored (the v5 database upgrade blanks old ones).
- The button opens Android's Google account chooser (Credential Manager). The Google ID token it returns is exchanged with
  **Firebase Authentication** (`signInWithCredential`). Only the `FirebaseUser` that Firebase returns is used. The app never
  reads an email from the device and treats it as logged in. The user must be Google-backed with a verified email.
- Each MediSync account is keyed by the stable **Firebase UID** (`users.uid`, unique). `AuthManager.signInWithGoogle` finds the
  account by UID, otherwise creates one (name, verified email; the Google photo is downloaded as the profile picture),
  and then sends a new user to the role screen: *Personal medication management*, *Parent / Guardian* (child 10 or below),
  or *Caretaker* (elderly patient or dependent). A returning user goes straight to the dashboard.
- The same UID always maps to the same account, so signing out and in again never duplicates accounts or touches records.
  Every table is reached through the account's patients (`patients.owner`), so one Google account cannot see another's data.
- **Session:** Firebase persists the session. On every app start the account is looked up from the live Firebase user, and
  `user.reload()` checks it. If the account was deleted or disabled, or its token was revoked or expired, the app signs out and
  shows "Please sign in again". If the phone is offline, the session is kept so reminders and records keep working.
  An auth-state listener also signs the app out if Firebase drops the session while the app is open.
- **Logout** ends the Firebase session and clears the Google choice. It never deletes the account or medication data.
  It also cancels that account's alarms (as before); they are re-armed at the next sign-in.
- **Account info:** Personal Information shows the verified Google email (read-only) and lets the user edit the display name and photo.
- **Errors handled:** cancelled picker, no Google account, no network (checked before and during sign-in), Play services unavailable,
  disabled account, too many attempts, missing Firebase configuration, unverified email, email already owned by another UID, storage errors.

**Accounts from the email/password version.** Data stays in the database. When someone signs in with Google for the first time and
their verified Google email equals an old account's email, that old account is attached to the new Firebase UID and keeps all its
patients, medicines and history. Old accounts whose email is not a Google address (for example the former demo account
`alex@email.com`) stay in the file but cannot be signed in to.

**Limits.** Records live in SQLite on the phone. They are separated per Google account, but they are **not** synced to the cloud, so
signing in on a different phone starts with an empty account. Cloud sync (for example Firestore) was not part of this change.

### Manual test checklist (run on a device after the Firebase setup)
| # | Test | Expected |
|---|---|---|
| 1 | First sign-in with Google account A | Account picker, then role screen. Name, email and photo come from Google. |
| 2 | Pick a role, add a medicine, sign out, sign in with A | Straight to dashboard with the same patient and medicine. |
| 3 | Sign out, choose Google account B | New account, role screen, no data from A. |
| 4 | Sign out, sign in with A | A's data only. |
| 5 | Open the picker and press back / tap outside | "Sign-in cancelled" message in grey, still on login. |
| 6 | Turn on airplane mode, tap the button | "No internet connection" message. |
| 7 | Sign in, force-close the app, reopen | Dashboard after the splash, no login. |
| 8 | Signed in, remove the user in Firebase Console > Authentication, reopen the app with internet | Returned to login with "session expired or revoked". |
| 9 | More > Profile > Personal Information | Email is read-only; name and photo can be changed. |
| 10 | Rotate the phone on the login screen and while "Signing in" | No crash; flow continues. |

## 5. Alarms and notifications

- `AlarmScheduler` creates one exact alarm per medicine dose time. Each alarm has a stable request code, so
  scheduling the same dose again replaces it; duplicates cannot pile up.
- When an alarm fires, `ReminderReceiver` shows the notification (if the slot is not already recorded) and sets the
  same dose for the next day.
- `rescheduleAll` runs at login, after any medicine or appointment change, after a reboot (`BootReceiver`),
  after an app update, and after a clock or timezone change.
- Channels: *Medication doses* (high priority), *Appointments*, *Low supply and refills*.
- Notification permission is requested on Android 13+ at login. Exact-alarm permission is checked; if it is not
  granted, inexact alarms are used instead.
- Notifications are dismissed by swiping. Reminders on the dashboard are dismissed with **Dismiss**.

---

## 6. Database

File: `medisync_app.db`, **schema version 5**.

| Table | Purpose |
|---|---|
| `users` | Accounts, keyed by Firebase `uid` (unique). `pass` is unused and left empty |
| `patients` | Patients and dependents, with `allergies` (new) |
| `meds` | Medicines, dosage, schedule, `remaining` and `total` supply, `active` |
| `history` | Dose records. `scheduled` (new) is the dose slot, `at` is when it was recorded, `status` is Taken, Late, Missed, or Skipped. A unique index on (medicine, slot) prevents duplicate records for one slot. |
| `appointments` | Appointments (new) |
| `reminder_dismissals` | Dismissed dashboard reminders (new) |
| `settings` | Session and per-patient preferences |

**Upgrade to version 5** keeps all rows; it adds `users.uid` and blanks old password hashes. **Upgrade from version 3** also keeps all rows. It adds the new columns and tables. Old history rows have no slot, and
the calendar matches them in order.

**Sample data:** a fresh install starts empty. The earlier version seeded a demo account (`alex@email.com` / `Demo1234`),
two patients and history. Existing installs keep those rows because their data is never deleted by an upgrade.
To remove them, use the app's patient and medicine screens, or uninstall and reinstall.

---

## 7. Known limitations and next steps

1. **Not compiled or run on Android yet.** Build in Android Studio, fix any errors, then complete `FIREBASE_SETUP.md` and the section 4 checklist.
2. **XML layouts:** the screens are built in code, as in the earlier version. The proposal asks for one XML layout
   per screen. Converting ~2,900 lines of `MainActivity` without a device to test on was judged too risky for this
   revision. `MainActivity` still contains most screen code; `CalendarScreen` and `AppointmentScreen` are separate.
3. **OCR prescription scanning and prescription image upload** are not implemented.
4. **Cloud sync** of medication data is not implemented; records are per-account on the device (section 4).
5. **Device-level testing still needed:** alarm timing on a real device (Doze mode, battery optimization),
   notification actions, boot restore, calendar layout on small and large screens, and the Late classification.
6. `targetSdk` was raised from 28 to 34. Behaviour changes on newer Android versions should be tested.
7. `DbHelper.java` (an unused earlier draft that used a different database file) was deleted.
8. The Gradle wrapper is not included (see section 1, step 2).

---

## 8. Audit findings fixed in this revision

| Problem in the earlier version | Fix |
|---|---|
| Login accepted a username as well as an email | Email only |
| Passwords hashed with unsalted SHA-256 | Superseded: email/password login was replaced by Google Sign-In (section 4) |
| Demo account and fake patients and history seeded on every new install | Fresh installs start empty |
| One alarm per patient at one fixed time, ignoring dose times | One exact alarm per dose time |
| No alarms after reboot; no appointment or refill alarms | `BootReceiver`; appointment alarms; refill notifications |
| Taken doses always subtracted supply, even if the same dose was recorded again | Supply changes only when the taken state changes; slots are unique |
| No on-time or late classification | `IntakeStatus` classifies taken doses against their slot |
| Appointment feature and LifeSync Calendar missing | Implemented (sections 3, 5) |
| Allergies not recorded | Patient allergy field and health alerts |
| `targetSdk 28` (blocks modern notification and alarm behaviour) | `targetSdk 34` |
| Model classes were private static inner classes of `MainActivity` | Separate classes in `model`, with encapsulation |
| `Comparator.comparingInt` used (Android API 24+ with minSdk 23) | Lambda used instead |
