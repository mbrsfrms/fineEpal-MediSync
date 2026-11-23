package com.medisync.app;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Password rules, password checklist and inline error messages. */
abstract class FormHelpers extends AvatarPicker {
    static boolean[] pwRules(String p) {
        boolean up = false, lo = false, di = false, space = false;
        for (char ch : p.toCharArray()) {
            if (ch >= 'A' && ch <= 'Z') up = true;
            else if (ch >= 'a' && ch <= 'z') lo = true;
            else if (ch >= '0' && ch <= '9') di = true;
            if (Character.isWhitespace(ch)) space = true;
        }
        return new boolean[]{p.length() >= 8, up, lo, di, !p.isEmpty() && !space};
    }

    static String pwError(String p) {
        if (p.isEmpty()) return "Enter a password.";
        boolean[] r = pwRules(p);
        if (!r[4]) return "Password must not contain spaces.";
        if (!r[0]) return "Password must be at least 8 characters.";
        if (!r[1]) return "Password needs at least one uppercase letter (A-Z).";
        if (!r[2]) return "Password needs at least one lowercase letter (a-z).";
        if (!r[3]) return "Password needs at least one number (0-9).";
        return null;
    }

    void pwChecklist(LinearLayout parent, final EditText pw) {
        final String[] names = {"At least 8 characters", "One uppercase letter (A–Z)", "One lowercase letter (a–z)",
                "One number (0–9)", "No spaces"};
        final TextView[] rows = new TextView[5];
        LinearLayout box = col();
        box.setPadding(dp(4), 0, 0, dp(10));
        for (int i = 0; i < 5; i++) {
            rows[i] = tv("○  " + names[i], 11, MUTED, false);
            box.addView(rows[i]);
        }
        parent.addView(box);
        pw.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { }
            public void afterTextChanged(Editable s) {
                boolean[] r = pwRules(s.toString());
                for (int i = 0; i < 5; i++) {
                    rows[i].setText((r[i] ? "✓  " : "○  ") + names[i]);
                    rows[i].setTextColor(r[i] ? GREEN : MUTED);
                }
            }
        });
    }

    TextView errView(LinearLayout parent) {
        TextView t = tv("", 12, RED, false);
        t.setPadding(dp(4), 0, 0, dp(8));
        t.setVisibility(View.GONE);
        parent.addView(t);
        return t;
    }

    void showMsg(TextView t, String msg, int color) {
        if (msg == null || msg.isEmpty()) { t.setVisibility(View.GONE); return; }
        t.setText(msg);
        t.setTextColor(color);
        t.setVisibility(View.VISIBLE);
    }

    void clearOnType(EditText e, final TextView t) {
        e.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { }
            public void afterTextChanged(Editable s) { t.setVisibility(View.GONE); }
        });
    }
}
