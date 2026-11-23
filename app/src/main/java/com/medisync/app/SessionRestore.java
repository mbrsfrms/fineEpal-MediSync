package com.medisync.app;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medisync.app.auth.GoogleSignIn;
import com.medisync.app.manager.AuthManager;
import com.medisync.app.model.*;
import com.medisync.app.util.Keys;
import java.util.List;

abstract class SessionRestore extends SplashScreen {
    void startSession(User u) {
        userId = u.getId(); userName = u.getName(); userEmail = u.getEmail();
        userAvatar = u.getAvatar() == null ? "" : u.getAvatar();
        db.put(Keys.USER_ID, "" + u.getId());
        List<Patient> ps = db.patients(u.getId());
        if (ps.isEmpty()) { patientId = -1; tab("role"); return; }
        patientId = savedPatientOrFirst(ps);
        db.put(Keys.PATIENT_ID, "" + patientId);
        tab("home");
        scheduleAll();
        askNotif();
    }

    /** The last selected patient if it still belongs to this account, otherwise the first one. Never another account's. */
    long savedPatientOrFirst(List<Patient> ps) {
        long saved = -1;
        try { saved = Long.parseLong(db.get(Keys.PATIENT_ID, "-1")); } catch (Exception ignored) { }
        for (Patient p : ps) if (p.getId() == saved) return saved;
        return ps.get(0).getId();
    }

    boolean dead() { return isFinishing() || isDestroyed(); }

    /**
     * App start: the only thing that counts as "signed in" is a live Firebase session. A MediSync account is looked
     * up by its Firebase UID. Nothing is granted from a stored user id.
     */
    void restoreFirebaseSession() {
        try {
            FirebaseUser fu = GoogleSignIn.currentUser(this);
            if (fu != null) {
                AuthManager.Result r = auth.signInWithGoogle(GoogleSignIn.identityOf(fu));
                if (r.isSuccess()) {
                    userId = r.user.getId(); userName = r.user.getName(); userEmail = r.user.getEmail();
                    userAvatar = r.user.getAvatar();
                    googleSession = true;
                    db.put(Keys.USER_ID, "" + userId);
                    List<Patient> ps = db.patients(userId);
                    patientId = ps.isEmpty() ? -1 : savedPatientOrFirst(ps);
                    if (patientId >= 0) db.put(Keys.PATIENT_ID, "" + patientId);
                } else {
                    GoogleSignIn.signOut(this);
                }
            }
        } catch (RuntimeException e) {
            userId = -1; patientId = -1; googleSession = false;
        }
        if (userId < 0) restorePasswordSession();
        if (GoogleSignIn.isFirebaseReady(this)) {
            authListener = fa -> {
                // Firebase dropped the session (for example a revoked token) while the app was open
                if (googleSession && fa.getCurrentUser() == null && userId >= 0 && !dead()) {
                    forceSignOut("You were signed out. Please sign in again to continue.");
                }
            };
            FirebaseAuth.getInstance().addAuthStateListener(authListener);
        }
        verifySavedSession();
    }

    /** Email/password session saved on this device. Only accounts that really have a password are restored this way. */
    void restorePasswordSession() {
        try {
            String u = db.get(Keys.USER_ID, "");
            if (u.isEmpty()) return;
            User us = db.user(Long.parseLong(u));
            if (us == null || !db.hasPassword(us.getId())) return;
            userId = us.getId(); userName = us.getName(); userEmail = us.getEmail();
            userAvatar = us.getAvatar() == null ? "" : us.getAvatar();
            googleSession = false;
            List<Patient> ps = db.patients(userId);
            patientId = ps.isEmpty() ? -1 : savedPatientOrFirst(ps);
        } catch (Exception ignored) {
            userId = -1; patientId = -1;
        }
    }

    /** Asks Firebase whether the saved session is still good. Offline is fine: the session is kept. */
    void verifySavedSession() {
        FirebaseUser fu = GoogleSignIn.currentUser(this);
        if (!googleSession || fu == null || userId < 0) return;
        lastSessionCheck = System.currentTimeMillis();
        GoogleSignIn.checkSession(this, fu, state -> {
            if (dead()) return;
            if (state == GoogleSignIn.SessionState.REVOKED) {
                forceSignOut("Your Google session has expired or was revoked. Please sign in again.");
            }
        });
    }

    void loginNotice(String msg, boolean error) {
        signingIn = false;
        loginMsg = msg;
        loginMsgIsError = error;
        if (cur.equals("login")) render();
    }
}
