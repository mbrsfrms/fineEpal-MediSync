package com.medisync.app;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.medisync.app.manager.AuthManager;
import com.medisync.app.manager.Validator;

/** Login and sign-up screens. */
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
            showMsg(nmMsg, null, 0);
            showMsg(emMsg, null, 0);
            showMsg(cfMsg, null, 0);
            AuthManager.Result r = auth.register(nm.getText().toString(), em.getText().toString(),
                    pw.getText().toString(), cf.getText().toString());
            switch (r.outcome) {
                case SUCCESS:
                    toast("Account created!");
                    googleSession = false;
                    startSession(r.user);
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
}
