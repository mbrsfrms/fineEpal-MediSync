package com.medisync.app;

import android.content.Intent;
import android.net.Uri;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.model.*;

/** Help, privacy, terms and text pages. */
abstract class HelpScreens extends AccountScreens {
    void dial(String number) {
        try { startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number))); }
        catch (Exception e) { toast("No dialer available."); }
    }

    void help() {
        LinearLayout b = page();
        topBar(b, "Help & Support", null, null);
        LinearLayout em = card(b);
        em.setBackground(rr(dark ? 0xFF3A1D1F : 0xFFFDEDEC, 0xFFF5B7B1, 14));
        em.addView(tv("Emergency Medical Assistance", 14, RED, true));
        sp(em, 6);
        String[][] hot = {{"National Emergency Hotline", "911"}, {"DOH Health Line", "02-8651-7800"}};
        for (final String[] hh : hot) {
            TextView t = tv("\u2022 " + hh[0] + ": " + hh[1] + "  (tap to call)", 12, RED, false);
            t.setPadding(0, dp(5), 0, dp(5));
            t.setOnClickListener(x -> dial(hh[1]));
            em.addView(t);
        }
        LinearLayout fq = card(b);
        fq.addView(tv("Frequently Asked Questions", 14, INK, true));
        sp(fq, 6);
        String[][] faqs = {
                {"How do I record a dose?", "Tap any medication on Home or Medications, then choose Taken, Missed or Skipped. Taken doses reduce your remaining supply."},
                {"How do I add another family member?", "Open My Dependents (More > My Dependents or tap the patient name on Home) and tap Add Patient."},
                {"How do reminders work?", "Open Reminders, switch the alerts on and set the reminder time. MediSync sends a daily notification. Allow notifications when asked."},
                {"Why is a supply marked low?", "Supply is low when the remaining amount covers 3 days or less at the scheduled dose rate."}
        };
        for (String[] q : faqs) {
            LinearLayout qb = col();
            qb.setPadding(0, dp(8), 0, dp(8));
            qb.addView(tv(q[0], 13, INK, true));
            qb.addView(tv(q[1], 12, MUTED, false));
            fq.addView(qb);
            divider(fq);
        }
        LinearLayout ct = card(b);
        ct.addView(tv("MediSync Support", 14, INK, true));
        sp(ct, 6);
        TextView mail = tv("support@medisync.app", 13, TEAL, false);
        mail.setOnClickListener(x -> {
            try { startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@medisync.app"))); }
            catch (Exception e) { toast("No email app available."); }
        });
        ct.addView(mail);
    }

    void textPage(String title, String text) {
        LinearLayout b = page();
        topBar(b, title, null, null);
        LinearLayout c = card(b);
        TextView t = tv(text, 13, INK, false);
        t.setLineSpacing(dp(3), 1f);
        c.addView(t);
    }
}
