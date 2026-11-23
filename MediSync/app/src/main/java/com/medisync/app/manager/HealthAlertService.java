package com.medisync.app.manager;

import com.medisync.app.model.Appointment;
import com.medisync.app.model.HealthAlert;
import com.medisync.app.model.Medication;
import com.medisync.app.model.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Health risk flags. These highlight information the user recorded; they do not give medical advice.
 * The allergy check is a simple text match between the medicine name and the recorded allergies.
 */
public final class HealthAlertService {
    private HealthAlertService() { }

    /** Common drugs of the same family as a recorded allergy, so "penicillin" also flags "amoxicillin". */
    private static final String[][] FAMILIES = {
            {"penicillin", "amoxicillin", "ampicillin", "amoxil", "augmentin", "co-amoxiclav", "cloxacillin",
                    "dicloxacillin", "flucloxacillin", "piperacillin", "oxacillin", "nafcillin"},
            {"sulfa", "sulfamethoxazole", "cotrimoxazole", "co-trimoxazole", "bactrim", "septra", "sulfasalazine"},
            {"nsaid", "ibuprofen", "naproxen", "diclofenac", "mefenamic", "ketorolac", "celecoxib", "aspirin"},
            {"cephalosporin", "cefalexin", "cephalexin", "cefuroxime", "cefixime", "ceftriaxone", "cefaclor", "cefadroxil"},
    };

    /**
     * Returns the recorded allergy that matches a medicine name (or the same drug family), or null when none does.
     * Matching is case-insensitive and works both ways, so allergy "Penicillin" flags medicine "Penicillin V 500mg".
     */
    public static String findAllergyMatch(Patient patient, String medicineName) {
        if (patient == null || !patient.hasAllergies() || medicineName == null) return null;
        String med = medicineName.toLowerCase(Locale.US).trim();
        if (med.length() < 3) return null;
        for (String raw : patient.getAllergies().split("[,;/\\n]| and ")) {
            String allergy = raw.toLowerCase(Locale.US).trim();
            if (allergy.length() < 3) continue;
            if (med.contains(allergy) || allergy.contains(med)) return raw.trim();
            for (String[] family : FAMILIES) {
                boolean allergyInFamily = false, medInFamily = false;
                for (String member : family) {
                    if (allergy.contains(member)) allergyInFamily = true;
                    if (med.contains(member)) medInFamily = true;
                }
                if (allergyInFamily && medInFamily) return raw.trim();
            }
        }
        return null;
    }

    public static List<HealthAlert> build(Patient patient, List<Medication> activeMeds,
                                          List<Appointment> appointments, long nowMillis) {
        List<HealthAlert> out = new ArrayList<>();
        if (patient.hasAllergies()) {
            out.add(new HealthAlert(HealthAlert.Kind.ALLERGY, HealthAlert.Severity.INFO,
                    "Recorded allergies: " + patient.getAllergies()));
        }
        for (Medication m : activeMeds) {
            String hit = findAllergyMatch(patient, m.getName());
            if (hit != null) {
                out.add(new HealthAlert(HealthAlert.Kind.ALLERGY, HealthAlert.Severity.DANGER,
                        m.getName() + " matches a recorded allergy (" + hit + "). Check with your doctor or pharmacist before taking it."));
            }
            int days = m.daysOfSupplyLeft();
            if (days <= Medication.LOW_STOCK_DAYS) {
                out.add(new HealthAlert(HealthAlert.Kind.LOW_STOCK, HealthAlert.Severity.DANGER,
                        m.getName() + " is running low (about " + Math.max(0, days) + " day(s) left)."));
            } else if (days <= Medication.REFILL_SOON_DAYS) {
                out.add(new HealthAlert(HealthAlert.Kind.LOW_STOCK, HealthAlert.Severity.WARNING,
                        m.getName() + " should be refilled soon (about " + days + " days left)."));
            }
        }
        for (Appointment a : appointments) {
            if (a.isOverdue(nowMillis)) {
                out.add(new HealthAlert(HealthAlert.Kind.OVERDUE_APPOINTMENT, HealthAlert.Severity.WARNING,
                        "Overdue: " + a.getTitle() + " on " + a.getDate() + " " + a.getTime()
                                + " is not marked completed or cancelled."));
            }
        }
        return out;
    }
}
