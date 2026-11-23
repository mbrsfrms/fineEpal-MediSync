package com.medisync.app;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.auth.EmailVerifier;
import com.medisync.app.auth.GoogleSignIn;
import com.medisync.app.manager.AuthManager;
import com.medisync.app.manager.Validator;
import java.util.Locale;

/** Login, sign-up and e-mail verification screens. */
abstract class LoginScreens extends GoogleAuthUi {
    void login() {
        LinearLayout b = wavePage();
        authHeader(b, "MediSync", "Sign in to continue");
        final EditText em = input(b, "Email address", "mail", false);
        em.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        final TextView emMsg = errView(b);
        final EditText pw = input(b, "Password", "lock", true);
        final TextView pwMsg = errView(b);
        clearOnType(em, emMsg);
        clearOnType(pw, pwMsg);
        sp(b, 4);
        btn(b, "Log In", () -> {
            showMsg(emMsg, null, 0);
            showMsg(pwMsg, null, 0);
            loginMsg = "";
            AuthManager.Result r = auth.login(em.getText().toString(), pw.getText().toString());
            switch (r.outcome) {
                case SUCCESS:
                    googleSession = false;
                    startSession(r.user);
                    break;
                case INVALID_EMAIL:
                    showMsg(emMsg, Validator.emailError(em.getText().toString()), RED);
                    break;
                case EMAIL_NOT_REGISTERED:
                    showMsg(emMsg, "No account is registered with this email. Please sign up first.", RED);
                    break;
                case GOOGLE_ACCOUNT:
                    showMsg(emMsg, "This account uses Google. Tap Continue with Google below.", RED);
                    break;
                case EMPTY_PASSWORD:
                    showMsg(pwMsg, "Enter your password to log in.", RED);
                    break;
                case WRONG_PASSWORD:
                    showMsg(pwMsg, "Incorrect password. Please try again.", RED);
                    break;
                default:
                    toast("Could not log in. Please try again.");
            }
        });
        orDivider(b);
        googleButton(b, this::googleSignIn);
        googleMessage(b);
        sp(b, 4);
        linkRow(b, "Don't have an account?", "Sign Up", () -> { loginMsg = ""; go("signup"); });
        sp(b, 4);
    }

    void signup() {
        clearPending(); // arriving here (or coming back from the verify screen) discards any half-finished sign-up
        LinearLayout b = wavePage();
        b.addView(new View(this), lp(1, 10));
        TextView t = tv("Create Your Account", 22, INK, true);
        b.addView(t);
        TextView s = tv("Start your journey to better medication management.", 12, MUTED, false);
        s.setPadding(0, dp(4), 0, dp(18));
        b.addView(s);
        final EditText nm = input(b, "Full Name", "person", false);
        final TextView nmMsg = errView(b);
        final EditText em = input(b, "Email Address", "mail", false);
        em.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        final TextView emMsg = errView(b);
        final EditText pw = input(b, "Password", "lock", true);
        pwChecklist(b, pw);
        final EditText cf = input(b, "Confirm Password", "lock", true);
        final TextView cfMsg = errView(b);
        clearOnType(nm, nmMsg);
        clearOnType(em, emMsg);
        clearOnType(cf, cfMsg);
        // format is checked when the field is left; the account check happens on submit
        em.setOnFocusChangeListener((v, has) -> {
            if (has) return;
            String e = em.getText().toString().trim();
            if (e.isEmpty()) return;
            showMsg(emMsg, Validator.emailError(e), RED);
        });
        sp(b, 4);
        btn(b, "Sign Up", () -> {
            if (sendingCode) return;
            showMsg(nmMsg, null, 0);
            showMsg(emMsg, null, 0);
            showMsg(cfMsg, null, 0);
            AuthManager.Result r = auth.validateRegistration(nm.getText().toString(), em.getText().toString(),
                    pw.getText().toString(), cf.getText().toString());
            switch (r.outcome) {
                case SUCCESS:
                    // fields are fine: prove the email is theirs before creating anything
                    startEmailVerification(nm.getText().toString().trim(), em.getText().toString().trim(),
                            pw.getText().toString(), emMsg);
                    break;
                case INVALID_NAME:
                    showMsg(nmMsg, Validator.nameError(nm.getText().toString()), RED);
                    break;
                case INVALID_EMAIL:
                    showMsg(emMsg, Validator.emailError(em.getText().toString()), RED);
                    break;
                case EMAIL_ALREADY_REGISTERED:
                    showMsg(emMsg, "This email is already registered. Please log in instead.", RED);
                    break;
                case WEAK_PASSWORD:
                    showMsg(cfMsg, Validator.passwordError(pw.getText().toString()), RED);
                    break;
                case PASSWORD_MISMATCH:
                    showMsg(cfMsg, "Passwords do not match.", RED);
                    break;
                default:
                    toast("Could not create the account. Please try again.");
            }
        });
        orDivider(b);
        googleButton(b, this::googleSignIn);
        googleMessage(b);
        sp(b, 6);
        linkRow(b, "Already have an account?", "Log In", () -> back());
    }

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