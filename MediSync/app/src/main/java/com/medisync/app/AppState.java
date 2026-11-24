package com.medisync.app;

import android.app.Activity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.medisync.app.database.MediSyncDatabase;
import com.medisync.app.manager.AuthManager;
import com.medisync.app.model.*;
import java.util.ArrayList;


abstract class AppState extends Activity {
    int TEAL = 0xFF1A9FA3, MINT = 0xFFE4F6F4, BG = 0xFFF1FAFA, CARD = 0xFFFFFFFF,
            INK = 0xFF173B55, MUTED = 0xFF6D8797, BORDER = 0xFFD5E9ED, HINT = 0xFF9DB0BA;

    static final int ORANGE = 0xFFEF8A1F, RED = 0xFFE5484D, GREEN = 0xFF2DB57A;

    boolean dark = false;

    static final String[] FORMS = {"Tablet", "Capsule", "Syrup", "Drops", "Injection", "Cream"};

    static final String[] FREQS = {"Once daily", "Twice daily", "Three times daily", "Four times daily", "Every other day", "As needed"};

    static final String[] DURS = {"3 days", "5 days", "7 days", "10 days", "14 days", "30 days", "Ongoing"};

    static final String[] RELS = {"Daughter", "Son", "Mother", "Father", "Grandmother", "Grandfather", "Spouse", "Sibling", "Other"};

    MediSyncDatabase db;

    AuthManager auth;

    int calYear, calMonth;

    String calSelected = "", apptFilter = "Upcoming", pendingOpen;

    long editingAppointmentId = -1;

    LinearLayout root, nav;

    FrameLayout content;

    String cur = "splash";

    final ArrayList<String> stack = new ArrayList<>();

    long userId = -1, patientId = -1;

    String userName = "", userEmail = "";

    boolean addingDependent = false, showActive = true, notifAsked = false;

    Medication editingMed;

    String careType = "Child", histFilter = "All", medQuery = "";

    String userAvatar = "", newAvatar = "";

    // Google sign-in state
    boolean signingIn = false, loginMsgIsError = false;

    String loginMsg = "";

    long lastSessionCheck = 0;

    boolean googleSession = false; // true when the open session came from Google/Firebase, false for email/password

    FirebaseAuth.AuthStateListener authListener;

    StrCb pendingPhotoCb;

    static final int REQ_PHOTO = 41;

    static final float TS = 1.15f; // global text scale

    static class Sel { String v = ""; TextView t; }

    interface Act { boolean run(); }

    interface IntCb { void on(int i); }

    interface StrCb { void on(String s); }

    static final String PRIVACY =
            "MediSync stores your account, patient, medication and intake information on this device only. "
                    + "We do not sell or share your data.\n\n"
                    + "Notifications are generated locally by your phone. You can remove all data at any time by logging out "
                    + "and clearing the app's storage in your phone's settings.\n\n"
                    + "MediSync is a reminder and tracking tool. It does not provide medical advice.";

    static final String TERMS =
            "By using MediSync you agree to use the app as a personal medication tracking aid.\n\n"
                    + "Always follow the instructions of your doctor or pharmacist. Do not change a dose because of a reminder "
                    + "or record shown in this app.\n\n"
                    + "In an emergency, contact your local emergency number immediately.";
}
