package com.medisync.app;

import android.graphics.drawable.GradientDrawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.model.*;

/** Forward declarations so each layer can call screens defined in later layers. */
abstract class AppHooks extends AppState {
    abstract int dp(float v);
    abstract GradientDrawable rr(int fill, int stroke, float r);
    abstract TextView tv(String s, float size, int color, boolean bold);
    abstract void sp(LinearLayout p, int h);
    abstract Button btn(LinearLayout parent, String text, Runnable r);
    abstract void go(String s);
    abstract void back();
    abstract void listDialog(String title, String[] opts, IntCb cb);
    abstract void render();
    abstract void splash();
    abstract void login();
    abstract void signup();
    abstract void role();
    abstract void caretype();
    abstract void home();
    abstract /** Add/edit medication screen; ed is null when adding a new medicine. */ void medForm(Medication ed);
    abstract void meds();
    abstract void addMed();
    abstract void history();
    abstract void patientScreen();
    abstract void dependents();
    abstract void upcoming();
    abstract void supply();
    abstract void reminders();
    abstract void notifications();
    abstract void streak();
    abstract void about();
    abstract void more();
    abstract void profile();
    abstract void personal();
    abstract void password();
    abstract void help();
    abstract void textPage(String title, String text);
    abstract void openPending();
    abstract void forceSignOut(String message);
    abstract void logout();
    abstract void linkRow2(LinearLayout parent, String icon, String title, boolean last, Runnable r);
    abstract void medDialog(Medication m);
    abstract /** Time between doses: only shown for medicines taken more than once a day. */ void medFormInterval(LinearLayout f, MedFormState s);
    abstract /** Duration, quantity and notes. */ void medFormSupply(LinearLayout f, MedFormState s);
    abstract /** Validates the form, warns about allergies and saves or updates the medicine. */ void saveMedForm(MedFormState s);
    abstract void applyTheme();
}
