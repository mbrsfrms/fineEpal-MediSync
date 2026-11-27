package com.medisync.app;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.auth.EmailVerifier;
import com.medisync.app.auth.GoogleSignIn;
import com.medisync.app.manager.AuthManager;
import java.util.Locale;

/** E-mail verification step of sign-up (verify screen and account creation). */
abstract class VerifyScreens extends GoogleAuthUi {
    void clearPending() {
        pendingName = "";
        pendingEmail = "";
        pendingPassword = "";
    }

    /** Creates the temporary Firebase user and sends the verification e-mail, then opens the verify screen. */
    void startEmailVerification(final String name, String email, final String password, final TextView emMsg) {
        if (!GoogleSignIn.isOnline(this)) {
            showMsg(emMsg, "No internet connection. Connect to Wi-Fi or mobile data, then try again.", RED);
            return;
        }
        final String normalized = email.trim().toLowerCase(Locale.US);
        sendingCode = true;
        toast("Sending verification email\u2026");
        EmailVerifier.start(this, normalized, password, f -> {
            sendingCode = false;
            if (dead()) return;
            if (f != null) {
                showMsg(emMsg, EmailVerifier.describe(f), RED);
                return;
            }
            pendingName = name;
            pendingEmail = normalized;
            pendingPassword = password;
            resendAt = System.currentTimeMillis() + 60_000;
            go("verify");
        });
    }

    void verifyEmail() {
        if (pendingEmail.isEmpty()) { // nothing to verify (e.g. the app was restarted): back to the start
            stack.clear();
            cur = "login";
            login();
            return;
        }
        LinearLayout b = wavePage();
        b.addView(new View(this), lp(1, 10));
        b.addView(tv("Verify Your Email", 22, INK, true));
        TextView s = tv("We sent a verification link to " + pendingEmail + ". Open the email, tap the link, then come back "
                + "here and tap the button below. Check your spam folder too.", 13, MUTED, false);
        s.setPadding(0, dp(4), 0, dp(18));
        b.addView(s);
        final TextView msg = errView(b);
        btn(b, "I've verified my email", () -> {
            if (verifyingCode) return;
            if (!GoogleSignIn.isOnline(this)) {
                showMsg(msg, "No internet connection. Connect to Wi-Fi or mobile data, then try again.", RED);
                return;
            }
            showMsg(msg, null, 0);
            verifyingCode = true;
            EmailVerifier.check(this, f -> {
                verifyingCode = false;
                if (dead()) return;
                if (f != null) { showMsg(msg, EmailVerifier.describe(f), RED); return; }
                createVerifiedAccount();
            });
        });
        sp(b, 4);
        linkRow(b, "Didn't get the email?", "Resend", () -> {
            long wait = (resendAt - System.currentTimeMillis() + 999) / 1000;
            if (wait > 0) { toast("Please wait " + wait + " seconds before requesting another email."); return; }
            if (sendingCode) return;
            sendingCode = true;
            EmailVerifier.resend(this, f -> {
                sendingCode = false;
                if (dead()) return;
                if (f != null) { showMsg(msg, EmailVerifier.describe(f), RED); return; }
                resendAt = System.currentTimeMillis() + 60_000;
                showMsg(msg, "A new email was sent. Check your inbox (and spam folder).", GREEN);
            });
        });
        linkRow(b, "Wrong email?", "Go back", () -> { EmailVerifier.cancel(this); clearPending(); back(); });
    }

    /** The link was confirmed, so the email belongs to this person: now create the account. */
    void createVerifiedAccount() {
        AuthManager.Result r = auth.register(pendingName, pendingEmail, pendingPassword, pendingPassword);
        clearPending();
        EmailVerifier.finish(this); // drop the temporary Firebase session
        switch (r.outcome) {
            case SUCCESS:
                toast("Email verified. Account created!");
                googleSession = false;
                startSession(r.user);
                break;
            case EMAIL_ALREADY_REGISTERED:
                toast("This email is already registered. Please log in instead.");
                tab("login");
                break;
            default:
                toast("Could not create the account. Please try again.");
                tab("signup");
        }
    }
}
