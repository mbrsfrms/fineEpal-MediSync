package com.medisync.app;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.medisync.app.model.Medication;
import com.medisync.app.model.Patient;

import java.util.List;

/** Widgets and values shared by the pieces of the add/edit medication form. */
final class MedFormState {
    final Medication ed; // null when adding a new medicine
    final List<Patient> ps;
    final String[] names;
    int idx;
    AppState.Sel pat, form, fr, iv, du;
    EditText nm, ds, qt, nt;
    TextView ttxt, ivPreview, allergyWarn;
    LinearLayout ivWrap;
    final int[] tm = {480};
    Runnable checkAllergy, refreshPreview, syncInterval;
    final String[] lastFr;

    MedFormState(Medication ed, List<Patient> ps, long currentPatientId) {
        this.ed = ed;
        this.ps = ps;
        names = new String[ps.size()];
        for (int i = 0; i < ps.size(); i++) {
            names[i] = ps.get(i).getName();
            if (ps.get(i).getId() == currentPatientId) idx = i;
        }
        if (ed != null && !ed.getDoseMinutes().isEmpty()) tm[0] = ed.getDoseMinutes().get(0);
        lastFr = new String[]{ed == null ? "" : ed.getFrequency()};
    }

    /** The patient chosen in the form (the medicine's owner when editing). */
    Patient target() {
        Patient target = ps.get(0);
        for (Patient pp : ps) if (pp.getName().equals(pat.v)) target = pp;
        if (ed != null) for (Patient pp : ps) if (pp.getId() == ed.getPatientId()) target = pp;
        return target;
    }
}
