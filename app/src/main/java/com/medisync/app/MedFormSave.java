package com.medisync.app;

import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import com.medisync.app.manager.AlarmScheduler;
import com.medisync.app.manager.HealthAlertService;
import com.medisync.app.manager.Validator;
import com.medisync.app.model.*;

/** Add/edit medication form: interval, supply fields and saving. */
abstract class MedFormSave extends MedFormFields {
    /** Time between doses: only shown for medicines taken more than once a day. */
    void medFormInterval(LinearLayout f, final MedFormState s) {
        s.ivWrap = col();
        lbl(s.ivWrap, "Time between doses");
        s.iv = new Sel();
        s.ivPreview = tv("", 12, MUTED, false);
        s.refreshPreview = () -> {
            String frq = s.fr.v.isEmpty() ? Medication.FREQ_ONCE : s.fr.v;
            if (Medication.doseCountFor(frq) < 2) { s.ivPreview.setText(""); return; }
            s.ivPreview.setText("Doses at: " + makeTimes(frq, s.tm[0], intervalFromLabel(s.iv.v, frq)));
        };
        LinearLayout ivBox = row();
        ivBox.setBackground(rr(CARD, BORDER, 10));
        ivBox.setPadding(dp(12), 0, dp(12), 0);
        s.iv.t = tv("Select interval", 13, HINT, false);
        ivBox.addView(s.iv.t, wt(1));
        ivBox.addView(new IconView(this, "down", MUTED), lp(19, 19));
        ivBox.setOnClickListener(x -> {
            String frq = s.fr.v.isEmpty() ? Medication.FREQ_ONCE : s.fr.v;
            final String[] opts = intervalOptions(frq);
            listDialog("Time between doses", opts, i -> {
                s.iv.v = opts[i];
                s.iv.t.setText(s.iv.v);
                s.iv.t.setTextColor(INK);
                s.refreshPreview.run();
            });
        });
        LinearLayout.LayoutParams ivl = lp(-1, 52);
        ivl.bottomMargin = dp(6);
        s.ivWrap.addView(ivBox, ivl);
        s.ivWrap.addView(s.ivPreview);
        sp(s.ivWrap, 10);
        f.addView(s.ivWrap);
        s.ivWrap.setVisibility(View.GONE);
        medFormIntervalSync(s);
    }

    /** Keeps the interval picker in step with the chosen frequency and first dose time. */
    void medFormIntervalSync(final MedFormState s) {
        final Medication ed = s.ed;
        s.syncInterval = () -> {
            String frq = s.fr.v.isEmpty() ? Medication.FREQ_ONCE : s.fr.v;
            boolean multi = Medication.doseCountFor(frq) >= 2;
            s.ivWrap.setVisibility(multi ? View.VISIBLE : View.GONE);
            if (multi && !frq.equals(s.lastFr[0])) {
                s.iv.v = intervalLabel(Medication.defaultIntervalHours(frq));
                s.iv.t.setText(s.iv.v);
                s.iv.t.setTextColor(INK);
            }
            s.lastFr[0] = frq;
            s.refreshPreview.run();
        };
        ((View) s.fr.t.getParent()).setOnClickListener(x -> listDialog("Select frequency", FREQS, i -> {
            s.fr.v = FREQS[i];
            s.fr.t.setText(s.fr.v);
            s.fr.t.setTextColor(INK);
            s.syncInterval.run();
        }));
        if (ed != null && Medication.doseCountFor(ed.getFrequency()) >= 2) {
            int g = ed.getIntervalHours();
            s.iv.v = intervalLabel(g > 0 && g <= Medication.maxIntervalHours(ed.getFrequency())
                    ? g : Medication.defaultIntervalHours(ed.getFrequency()));
            s.iv.t.setText(s.iv.v);
            s.iv.t.setTextColor(INK);
        }
        s.syncInterval.run();
    }

    /** Duration, quantity and notes. */
    void medFormSupply(LinearLayout f, final MedFormState s) {
        Medication ed = s.ed;
        lbl(f, "Duration");
        s.du = select(f, "Select duration", DURS, ed == null ? null : ed.getDuration());
        lbl(f, ed == null ? "Quantity on hand (optional)" : "Remaining supply");
        s.qt = input(f, "e.g. 30", null, false);
        s.qt.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (ed != null) s.qt.setText("" + ed.getRemaining());
        lbl(f, "Notes (optional)");
        s.nt = input(f, "e.g. Take after meals", null, false);
        if (ed != null) s.nt.setText(ed.getNotes());
    }

    /** Validates the form, warns about allergies and saves or updates the medicine. */
    void saveMedForm(final MedFormState s) {
        final Medication ed = s.ed;
        String n = s.nm.getText().toString().trim(), d = s.ds.getText().toString().trim();
        if (n.isEmpty()) { toast("Enter the medicine name."); return; }
        String dosageErr = Validator.dosageError(d);
        if (dosageErr != null) { toast(dosageErr); return; }
        String frq = s.fr.v.isEmpty() ? Medication.FREQ_ONCE : s.fr.v;
        String fm = s.form.v.isEmpty() ? "Tablet" : s.form.v;
        String dur = s.du.v.isEmpty() ? "Ongoing" : s.du.v;
        final Patient tgt = s.target();
        int gapHours = Medication.doseCountFor(frq) >= 2 ? intervalFromLabel(s.iv.v, frq) : 0;
        if (gapHours > Medication.maxIntervalHours(frq)) { toast("That gap is too long to fit every dose in one day."); return; }
        String times = makeTimes(frq, s.tm[0], gapHours);
        String qtxt = s.qt.getText().toString().trim();
        int total;
        if (qtxt.isEmpty()) {
            if (ed != null) total = ed.getRemaining();
            else if (dur.equals("Ongoing")) total = 30;
            else total = (int) Math.ceil(Integer.parseInt(dur.split(" ")[0]) * Medication.dosesPerDay(frq, times));
        } else {
            try {
                total = Validator.parseQuantity(qtxt);
            } catch (IllegalArgumentException ex) {
                toast(ex.getMessage());
                return;
            }
        }
        final int tot = total;
        final String fn = n, fd = d, ffm = fm, ffrq = frq, ftimes = times, fdur = dur, fnotes = s.nt.getText().toString().trim();
        final Runnable doSave = () -> {
            if (ed == null) {
                db.addMed(tgt.getId(), fn, fd, ffm, ffrq, ftimes, fdur, fnotes, tot);
            } else {
                AlarmScheduler.cancelMedication(this, ed); // old dose times may no longer apply
                db.updateMed(ed.getId(), fn, fd, ffm, ffrq, ftimes, fdur, fnotes, tot);
            }
            scheduleAll();
            toast(ed == null ? "Medication saved." : "Medication updated.");
            back();
        };
        String hit = HealthAlertService.findAllergyMatch(tgt, n);
        if (hit != null) {
            confirm("Allergy warning", tgt.getName() + " has a recorded allergy to " + hit + ", and " + n
                    + " matches it. Check with a doctor or pharmacist before taking it. Save anyway?", "Save anyway", doSave);
        } else {
            doSave.run();
        }
    }
}
