package com.medisync.app;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Locale;

/** Form widgets: labels, text inputs, selects, date selects and switches. */
abstract class UiInputs extends UiBasics {
    TextView lbl(LinearLayout p, String s) {
        TextView t = tv(s, 11, MUTED, false);
        t.setPadding(dp(2), dp(4), 0, dp(4));
        p.addView(t);
        return t;
    }

    EditText input(LinearLayout parent, String hint, String icon, boolean pw) {
        LinearLayout box = row();
        box.setBackground(rr(CARD, BORDER, 10));
        box.setPadding(dp(12), 0, dp(12), 0);
        if (icon != null) box.addView(new IconView(this, icon, MUTED), lp(22, 22));
        final EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(HINT);
        e.setTextColor(INK);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setBackground(null);
        e.setPadding(dp(icon != null ? 10 : 2), 0, dp(4), 0);
        box.addView(e, new LinearLayout.LayoutParams(0, -1, 1));
        if (pw) {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            final IconView eye = new IconView(this, "eye", MUTED);
            final boolean[] shown = {false};
            eye.setOnClickListener(x -> {
                shown[0] = !shown[0];
                e.setTransformationMethod(shown[0] ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
                e.setSelection(e.getText().length());
                eye.set(shown[0] ? "eyeoff" : "eye", MUTED);
            });
            box.addView(eye, lp(22, 22));
        }
        LinearLayout.LayoutParams p = lp(-1, 52);
        p.bottomMargin = dp(10);
        parent.addView(box, p);
        return e;
    }

    Sel select(LinearLayout parent, String hint, final String[] opts, String initial) {
        final Sel s = new Sel();
        s.v = initial == null ? "" : initial;
        LinearLayout box = row();
        box.setBackground(rr(CARD, BORDER, 10));
        box.setPadding(dp(12), 0, dp(12), 0);
        s.t = tv(s.v.isEmpty() ? hint : s.v, 13, s.v.isEmpty() ? HINT : INK, false);
        box.addView(s.t, wt(1));
        box.addView(new IconView(this, "down", MUTED), lp(19, 19));
        box.setOnClickListener(x -> listDialog(hint, opts, i -> {
            s.v = opts[i];
            s.t.setText(s.v);
            s.t.setTextColor(INK);
        }));
        LinearLayout.LayoutParams p = lp(-1, 52);
        p.bottomMargin = dp(10);
        parent.addView(box, p);
        return s;
    }

    Sel dateSelect(LinearLayout parent, String hint, String initial) {
        final Sel s = new Sel();
        s.v = initial == null ? "" : initial;
        LinearLayout box = row();
        box.setBackground(rr(CARD, BORDER, 10));
        box.setPadding(dp(12), 0, dp(12), 0);
        s.t = tv(s.v.isEmpty() ? hint : fmtDate(s.v), 13, s.v.isEmpty() ? HINT : INK, false);
        box.addView(s.t, wt(1));
        box.addView(new IconView(this, "down", MUTED), lp(19, 19));
        box.setOnClickListener(x -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog d = new DatePickerDialog(this, (pk, y, m, dd) -> {
                s.v = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, dd);
                s.t.setText(fmtDate(s.v));
                s.t.setTextColor(INK);
            }, c.get(Calendar.YEAR) - 8, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            d.getDatePicker().setMaxDate(System.currentTimeMillis());
            d.show();
        });
        LinearLayout.LayoutParams p = lp(-1, 52);
        p.bottomMargin = dp(10);
        parent.addView(box, p);
        return s;
    }

    Switch sw(boolean on) {
        Switch s = new Switch(this);
        s.setChecked(on);
        int[][] st = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}};
        s.setThumbTintList(new ColorStateList(st, new int[]{Color.WHITE, Color.WHITE}));
        s.setTrackTintList(new ColorStateList(st, new int[]{TEAL, 0xFFB8C7CF}));
        return s;
    }
}
