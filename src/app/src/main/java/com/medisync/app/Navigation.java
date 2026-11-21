package com.medisync.app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Screen stack, tab handling, the screen router and the bottom navigation bar. */
abstract class Navigation extends FormHelpers {
    boolean isAuth(String s) {
        return s.equals("splash") || s.equals("login") || s.equals("signup") || s.equals("role") || s.equals("caretype");
    }

    void go(String s) { if (s.equals("caretype")) newAvatar = ""; stack.add(cur); cur = s; render(); }

    void tab(String s) { stack.clear(); cur = s; render(); }

    void back() {
        if (!stack.isEmpty()) { cur = stack.remove(stack.size() - 1); render(); return; }
        if (userId >= 0 && !cur.equals("home") && !isAuth(cur)) { cur = "home"; render(); return; }
        finish();
    }

    String tabOf(String s) {
        switch (s) {
            case "meds": case "addmed": case "editmed": case "supply": return "Medications";
            case "history": return "History";
            case "more": case "profile": case "personal": case "password": case "help": case "privacy":
            case "terms": case "notifications": case "about": case "streak": case "reminders": return "More";
            default: return "Home";
        }
    }

    Drawable splashBg() {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xFF2BB9B0, 0xFF0F7B86});
    }

    void render() {
        boolean splash = cur.equals("splash");
        boolean auth = isAuth(cur);
        if (!auth && userId < 0) { cur = "login"; stack.clear(); auth = true; }
        getWindow().setBackgroundDrawable(splash ? splashBg() : new ColorDrawable(BG));
        root.setBackground(splash ? splashBg() : new ColorDrawable(BG));
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(auth ? (splash ? 0xFF0F7B86 : BG) : CARD);
        int f = 0;
        if (!dark && !splash) f |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (!dark && Build.VERSION.SDK_INT >= 26) f |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(f);

        nav.setVisibility(auth ? View.GONE : View.VISIBLE);
        nav.setBackgroundColor(CARD);
        content.removeAllViews();

        switch (cur) {
            case "splash": splash(); break;
            case "login": login(); break;
            case "signup": signup(); break;
            case "role": role(); break;
            case "caretype": caretype(); break;
            case "home": home(); break;
            case "meds": meds(); break;
            case "addmed": addMed(); break;
            case "editmed": if (editingMed == null) addMed(); else medForm(editingMed); break;
            case "history": history(); break;
            case "more": more(); break;
            case "patient": patientScreen(); break;
            case "dependents": dependents(); break;
            case "upcoming": upcoming(); break;
            case "supply": supply(); break;
            case "reminders": reminders(); break;
            case "streak": streak(); break;
            case "about": about(); break;
            case "profile": profile(); break;
            case "personal": personal(); break;
            case "password": password(); break;
            case "help": help(); break;
            case "privacy": textPage("Privacy Policy", PRIVACY); break;
            case "terms": textPage("Terms of Service", TERMS); break;
            case "notifications": notifications(); break;
            case "calendar": CalendarScreen.build((MainActivity) this); break;
            case "appointments": AppointmentScreen.list((MainActivity) this); break;
            case "appointment_form": AppointmentScreen.form((MainActivity) this); break;
            default: home();
        }
        if (!auth) buildNav();
    }

    void buildNav() {
        nav.removeAllViews();
        final String[] lb = {"Home", "Medications", "History", "More"};
        final String[] ic = {"home", "pill", "clock", "grid"};
        final String[] tg = {"home", "meds", "history", "more"};
        String sel = tabOf(cur);
        for (int i = 0; i < 4; i++) {
            final int k = i;
            boolean on = lb[i].equals(sel);
            LinearLayout it = col();
            it.setGravity(Gravity.CENTER);
            it.addView(new IconView(this, ic[i], on ? TEAL : MUTED), lp(26, 26));
            TextView t = tv(lb[i], 10, on ? TEAL : MUTED, on);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, dp(3), 0, 0);
            it.addView(t);
            it.setOnClickListener(x -> { if (!(cur.equals(tg[k]) && stack.isEmpty())) tab(tg[k]); });
            nav.addView(it, new LinearLayout.LayoutParams(0, -1, 1));
        }
    }
}
