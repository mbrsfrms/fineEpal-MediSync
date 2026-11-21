package com.medisync.app;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.manager.AlarmScheduler;
import com.medisync.app.manager.HealthAlertService;
import com.medisync.app.manager.Validator;
import com.medisync.app.model.*;
import java.util.Locale;

/** Medication list, details dialog and supply update. */
abstract class MedListScreen extends MedFormSave {
    void meds() {
        final LinearLayout b = page();
        final Patient p = needPatient(b);
        if (p == null) return;
        topBar(b, p.getName(), "Medications", p);

        LinearLayout sbox = row();
        sbox.setBackground(rr(CARD, BORDER, 10));
        sbox.setPadding(dp(12), 0, dp(12), 0);
        sbox.addView(new IconView(this, "search", MUTED), lp(22, 22));
        EditText se = new EditText(this);
        se.setHint("Search medication...");
        se.setHintTextColor(HINT);
        se.setTextColor(INK);
        se.setTextSize(15);
        se.setSingleLine(true);
        se.setBackground(null);
        se.setPadding(dp(10), 0, 0, 0);
        se.setText(medQuery);
        sbox.addView(se, new LinearLayout.LayoutParams(0, -1, 1));
        LinearLayout.LayoutParams sp1 = lp(-1, 50);
        sp1.bottomMargin = dp(12);
        b.addView(sbox, sp1);

        LinearLayout tabs = row();
        LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, dp(36), 1);
        a.rightMargin = dp(8);
        tabs.addView(chip("Active", showActive, () -> { showActive = true; render(); }), a);
        tabs.addView(chip("Inactive", !showActive, () -> { showActive = false; render(); }), new LinearLayout.LayoutParams(0, dp(36), 1));
        LinearLayout.LayoutParams tp = lp(-1, -2);
        tp.bottomMargin = dp(14);
        b.addView(tabs, tp);

        final LinearLayout list = col();
        b.addView(list);
        fillMeds(list, p);
        sp(b, 96); // keeps the last item clear of the + button and makes the page scroll
        se.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a1, int b1, int c1) { }
            public void onTextChanged(CharSequence s, int a1, int b1, int c1) { }
            public void afterTextChanged(Editable s) { medQuery = s.toString(); fillMeds(list, p); }
        });

        FrameLayout fab = new FrameLayout(this);
        GradientDrawable oval = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xFF25B3AF, 0xFF10858C});
        oval.setShape(GradientDrawable.OVAL);
        fab.setBackground(oval);
        fab.setElevation(dp(6));
        fab.addView(new IconView(this, "plus", Color.WHITE), new FrameLayout.LayoutParams(dp(31), dp(31), Gravity.CENTER));
        fab.setOnClickListener(x -> go("addmed"));
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(dp(56), dp(56), Gravity.BOTTOM | Gravity.END);
        fp.setMargins(0, 0, dp(18), dp(18));
        content.addView(fab, fp);
    }

    void fillMeds(LinearLayout list, Patient p) {
        list.removeAllViews();
        String q = medQuery.toLowerCase(Locale.US).trim();
        int n = 0;
        for (Medication m : db.meds(p.getId(), showActive)) {
            if (!q.isEmpty() && !m.getName().toLowerCase(Locale.US).contains(q)) continue;
            medRow(list, m);
            n++;
        }
        if (n == 0) list.addView(tv(showActive ? "No active medications found." : "No inactive medications.", 13, MUTED, false));
    }

    void addMed() { editingMed = null; medForm(null); }

    void medDialog(final Medication m) {
        final AlertDialog[] dd = new AlertDialog[1];
        LinearLayout body = col();
        body.addView(tv(m.getDosage() + " \u00B7 " + m.getForm() + " \u00B7 " + shortFreq(m.getFrequency()), 12, MUTED, false));
        body.addView(tv("Schedule: " + m.getTimes() + (m.getIntervalHours() > 0 ? " (every " + m.getIntervalHours() + " hr)" : "") + "   \u00B7   " + m.getRemaining() + " " + unit(m.getForm()) + " left", 12, MUTED, false));
        if (m.getNotes() != null && !m.getNotes().isEmpty()) body.addView(tv("Notes: " + m.getNotes(), 12, MUTED, false));
        String dlgHit = HealthAlertService.findAllergyMatch(curPatient(), m.getName());
        if (dlgHit != null) {
            sp(body, 6);
            body.addView(tv("\u26A0 Allergy warning: matches recorded allergy (" + dlgHit + "). Check with your doctor or pharmacist.", 12, RED, true));
        }
        sp(body, 12);
        if (m.isActive()) {
            body.addView(tv("Record a dose", 12, INK, true));
            sp(body, 6);
            LinearLayout r = row();
            String[] st = {"Taken", "Missed", "Skipped"};
            int[] sc = {TEAL, RED, 0xFF8A9BA6};
            for (int i = 0; i < 3; i++) {
                final String status = st[i];
                TextView c = tv(status, 12, Color.WHITE, true);
                c.setGravity(Gravity.CENTER);
                c.setBackground(rr(sc[i], 0, 10));
                c.setOnClickListener(x -> { dd[0].dismiss(); recordDose(m, status); });
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(40), 1);
                if (i < 2) p.rightMargin = dp(6);
                r.addView(c, p);
            }
            body.addView(r);
            sp(body, 8);
        }
        TextView ed = tv("Edit medication", 13, INK, false);
        ed.setPadding(dp(2), dp(10), 0, dp(10));
        ed.setOnClickListener(x -> { dd[0].dismiss(); editingMed = m; go("editmed"); });
        body.addView(ed);
        divider(body);
        TextView e = tv("Update remaining supply", 13, INK, false);
        e.setPadding(dp(2), dp(10), 0, dp(10));
        e.setOnClickListener(x -> { dd[0].dismiss(); editSupply(m); });
        body.addView(e);
        divider(body);
        TextView ac = tv(m.isActive() ? "Mark as inactive" : "Mark as active", 13, INK, false);
        ac.setPadding(dp(2), dp(10), 0, dp(10));
        ac.setOnClickListener(x -> { dd[0].dismiss(); if (m.isActive()) AlarmScheduler.cancelMedication(this, m); db.setActive(m.getId(), !m.isActive()); scheduleAll(); render(); });
        body.addView(ac);
        divider(body);
        TextView de = tv("Delete medication", 13, RED, false);
        de.setPadding(dp(2), dp(10), 0, dp(10));
        de.setOnClickListener(x -> {
            dd[0].dismiss();
            confirm("Delete medication", "Delete " + m.getName() + " and its supply record? Intake history is kept.", "Delete",
                    () -> { AlarmScheduler.cancelMedication(this, m); db.deleteMed(m.getId()); toast("Medication deleted."); render(); });
        });
        body.addView(de);
        dd[0] = dialog(m.getName(), body, null, null, "Close");
    }

    void editSupply(final Medication m) {
        LinearLayout body = col();
        final EditText e = input(body, "Remaining quantity", null, false);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        e.setText("" + m.getRemaining());
        dialog("Update supply \u00B7 " + m.getName(), body, "Save", () -> {
            try {
                int n = Validator.parseQuantity(e.getText().toString());
                db.updateSupply(m.getId(), n);
                toast("Supply updated.");
                render();
                return true;
            } catch (IllegalArgumentException ex) { toast(ex.getMessage()); return false; }
        }, "Cancel");
    }
}
