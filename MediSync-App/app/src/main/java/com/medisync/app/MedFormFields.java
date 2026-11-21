package com.medisync.app;

import android.app.TimePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.manager.HealthAlertService;
import com.medisync.app.model.*;

/** Add/edit medication form: patient, name and schedule fields. */
abstract class MedFormFields extends HomeScreen {
    /** Add/edit medication screen; ed is null when adding a new medicine. */
    void medForm(final Medication ed) {
        LinearLayout b = page();
        topBar(b, ed == null ? "Add Medication" : "Edit Medication", null, null);
        final MedFormState s = new MedFormState(ed, db.patients(userId), patientId);
        if (s.ps.isEmpty()) { needPatient(b); return; }
        LinearLayout f = card(b);
        medFormPatientName(f, s);
        medFormSchedule(f, s);
        medFormInterval(f, s);
        medFormSupply(f, s);
        btn(f, ed == null ? "Save" : "Save changes", () -> saveMedForm(s));
    }

    /** Patient, medicine name and the live allergy warning. */
    void medFormPatientName(LinearLayout f, final MedFormState s) {
        final Medication ed = s.ed;
        if (ed == null) {
            lbl(f, "Patient");
            s.pat = select(f, "Select patient", s.names, s.names[s.idx]);
        } else {
            s.pat = new Sel();
            s.pat.t = new TextView(this);
            s.pat.v = s.names[s.idx];
            for (Patient pp : s.ps) if (pp.getId() == ed.getPatientId()) s.pat.v = pp.getName();
            f.addView(tv("Patient: " + s.pat.v, 12, MUTED, false));
            sp(f, 10);
        }
        lbl(f, "Medicine Name");
        s.nm = input(f, "e.g. Paracetamol", null, false);
        if (ed != null) s.nm.setText(ed.getName());
        s.allergyWarn = tv("", 12, RED, true);
        s.allergyWarn.setVisibility(View.GONE);
        f.addView(s.allergyWarn);
        s.checkAllergy = () -> {
            Patient tp = s.target();
            String hit = HealthAlertService.findAllergyMatch(tp, s.nm.getText().toString());
            if (hit == null) { s.allergyWarn.setVisibility(View.GONE); return; }
            s.allergyWarn.setText("\u26A0 " + tp.getName() + " is allergic to " + hit + ". This medicine matches that allergy.");
            s.allergyWarn.setVisibility(View.VISIBLE);
        };
        s.nm.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence q, int a1, int b1, int c1) { }
            public void onTextChanged(CharSequence q, int a1, int b1, int c1) { }
            public void afterTextChanged(Editable q) { s.checkAllergy.run(); }
        });
        if (ed == null) {
            ((View) s.pat.t.getParent()).setOnClickListener(x -> listDialog("Select patient", s.names, i -> {
                s.pat.v = s.names[i];
                s.pat.t.setText(s.pat.v);
                s.pat.t.setTextColor(INK);
                s.checkAllergy.run();
            }));
        }
    }

    /** Dosage, form, frequency and first dose time. */
    void medFormSchedule(LinearLayout f, final MedFormState s) {
        Medication ed = s.ed;
        lbl(f, "Dosage");
        s.ds = input(f, "e.g. 500mg", null, false);
        if (ed != null) s.ds.setText(ed.getDosage());
        lbl(f, "Form");
        s.form = select(f, "Select form", FORMS, ed == null ? null : ed.getForm());
        lbl(f, "Schedule");
        s.fr = select(f, "Select frequency", FREQS, ed == null ? null : ed.getFrequency());
        lbl(f, "First dose time");
        LinearLayout tbox = row();
        tbox.setBackground(rr(CARD, BORDER, 10));
        tbox.setPadding(dp(12), 0, dp(12), 0);
        s.ttxt = tv(fmtMin(s.tm[0]), 13, INK, false);
        tbox.addView(s.ttxt, wt(1));
        tbox.addView(new IconView(this, "clock", MUTED), lp(22, 22));
        tbox.setOnClickListener(x -> new TimePickerDialog(this, (pk, h, m) -> {
            s.tm[0] = h * 60 + m;
            s.ttxt.setText(fmtMin(s.tm[0]));
            s.refreshPreview.run();
        }, s.tm[0] / 60, s.tm[0] % 60, false).show());
        LinearLayout.LayoutParams tl = lp(-1, 52);
        tl.bottomMargin = dp(10);
        f.addView(tbox, tl);
    }
}
