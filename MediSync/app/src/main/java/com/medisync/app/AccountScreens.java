package com.medisync.app;

import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.manager.AuthManager;
import com.medisync.app.manager.Validator;


abstract class AccountScreens extends MoreScreen {
    void personal() {
        LinearLayout b = page();
        topBar(b, "Personal Information", null, null);
        LinearLayout c = card(b);
        LinearLayout.LayoutParams avp = lp(104, 104);
        avp.gravity = Gravity.CENTER_HORIZONTAL;
        c.addView(avatarEdit(userAvatar, 3, 104, v -> { deleteAvatarFile(userAvatar, v); userAvatar = v; db.setUserAvatar(userId, v); render(); }), avp);
        TextView chg = tv("Tap to change your photo", 12, MUTED, false);
        chg.setGravity(Gravity.CENTER);
        chg.setPadding(0, dp(6), 0, dp(14));
        c.addView(chg);
        lbl(c, "Full Name");
        final EditText n = input(c, "Full name", "person", false);
        n.setText(userName);
        final boolean pwAccount = db.hasPassword(userId);
        lbl(c, pwAccount ? "Email Address" : "Google Account");
        LinearLayout ge = row();
        ge.setPadding(dp(14), dp(12), dp(14), dp(12));
        ge.setBackground(rr(MINT, 0, 12));
        if (pwAccount) {
            ge.addView(new IconView(this, "mail", TEAL), lp(20, 20));
        } else {
            ImageView gl = new ImageView(this);
            gl.setImageResource(R.drawable.ic_google_logo);
            ge.addView(gl, lp(20, 20));
        }
        LinearLayout gt = col();
        gt.setPadding(dp(12), 0, 0, 0);
        gt.addView(tv(userEmail, 13, INK, true));
        gt.addView(tv(pwAccount ? "Signed in with email and password" : "Signed in with Google \u2022 verified", 11, MUTED, false));
        ge.addView(gt, wt(1));
        LinearLayout.LayoutParams gp = lp(-1, -2);
        gp.bottomMargin = dp(6);
        c.addView(ge, gp);
        TextView note = tv(pwAccount ? "Your email is your login and can't be changed here." : "Your email comes from your Google account and can't be changed here.", 11, MUTED, false);
        note.setPadding(dp(2), dp(2), 0, dp(8));
        c.addView(note);
        btn(c, "Save", () -> {
            String nn = n.getText().toString().trim();
            String err = Validator.nameError(nn);
            if (err != null) { toast(err); return; }
            if (!db.updateUserName(userId, nn)) { toast("Could not save your name."); return; }
            userName = nn;
            toast("Profile updated.");
            back();
        });
    }

    void password() {
        LinearLayout b = page();
        topBar(b, "Change Password", null, null);
        LinearLayout c = card(b);
        final EditText o = input(c, "Current password", "lock", true);
        final EditText n = input(c, "New password", "lock", true);
        pwChecklist(c, n);
        final EditText f = input(c, "Confirm new password", "lock", true);
        btn(c, "Update Password", () -> {
            String np = n.getText().toString();
            String pe = pwError(np);
            if (pe != null) { toast(pe); return; }
            if (!np.equals(f.getText().toString())) { toast("Passwords do not match."); return; }
            AuthManager.Result r = auth.changePassword(userId, userEmail, o.getText().toString(), np, f.getText().toString());
            if (r.outcome == AuthManager.Outcome.WRONG_PASSWORD) { toast("Current password is incorrect."); return; }
            if (!r.isSuccess()) { toast("Could not change the password."); return; }
            toast("Password updated.");
            back();
        });
    }
}
