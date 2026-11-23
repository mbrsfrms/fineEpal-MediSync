package com.medisync.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.firebase.auth.FirebaseUser;
import com.medisync.app.auth.GoogleSignIn;
import com.medisync.app.model.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import com.medisync.app.manager.AuthManager;
import java.net.HttpURLConnection;
import java.net.URL;

/** Google sign-in flow and profile photo download. */
abstract class GoogleAuthFlow extends SessionRestore {
    void googleSignIn() {
        if (signingIn) return;
        String problem = GoogleSignIn.configurationProblem(this);
        if (problem != null) { loginNotice(problem, true); return; }
        if (!GoogleSignIn.isOnline(this)) {
            loginNotice("No internet connection. Connect to Wi-Fi or mobile data, then try again.", true);
            return;
        }
        loginMsg = "";
        signingIn = true;
        render();
        GoogleSignIn.start(this, new GoogleSignIn.SignInCallback() {
            @Override public void onSignedIn(FirebaseUser fu) {
                if (dead()) return;
                finishGoogleSignIn(fu);
            }

            @Override public void onFailure(GoogleSignIn.Failure f) {
                if (dead()) return;
                switch (f) {
                    case CANCELLED:
                        loginNotice("Sign-in cancelled. Tap Continue with Google when you're ready.", false);
                        break;
                    case NO_ACCOUNT:
                        loginNotice("No Google account is available on this device. Add one in your phone's settings, then try again.", true);
                        break;
                    case NETWORK:
                        loginNotice("Network problem while signing in. Check your connection and try again.", true);
                        break;
                    case NOT_CONFIGURED:
                        loginNotice(GoogleSignIn.configurationProblem(GoogleAuthFlow.this), true);
                        break;
                    case PROVIDER_UNAVAILABLE:
                        loginNotice("Google sign-in isn't available on this device. Update Google Play services and try again.", true);
                        break;
                    case ACCOUNT_DISABLED:
                        loginNotice("This Google account has been disabled for MediSync.", true);
                        break;
                    case TOO_MANY_REQUESTS:
                        loginNotice("Too many attempts. Please wait a few minutes and try again.", true);
                        break;
                    default:
                        loginNotice("Google sign-in failed. Please try again.", true);
                }
            }
        });
    }

    /** Firebase has authenticated the account; find, link or create the MediSync profile and open the app. */
    void finishGoogleSignIn(FirebaseUser fu) {
        AuthManager.Result r;
        try {
            r = auth.signInWithGoogle(GoogleSignIn.identityOf(fu));
        } catch (RuntimeException e) {
            GoogleSignIn.signOut(this);
            loginNotice("Could not open your MediSync account. Please try again.", true);
            return;
        }
        if (!r.isSuccess()) {
            GoogleSignIn.signOut(this); // never keep a Firebase session that MediSync did not accept
            switch (r.outcome) {
                case EMAIL_NOT_VERIFIED:
                    loginNotice("Your Google email address is not verified, so MediSync can't sign you in.", true);
                    break;
                case EMAIL_CONFLICT:
                    loginNotice("This email is already used by a different MediSync account.", true);
                    break;
                default:
                    loginNotice("Could not open your MediSync account. Please try again.", true);
            }
            return;
        }
        signingIn = false;
        loginMsg = "";
        googleSession = true;
        if (r.newAccount && fu.getPhotoUrl() != null) downloadGooglePhoto(r.user.getId(), fu.getPhotoUrl().toString());
        startSession(r.user);
    }

    /** Saves the Google profile picture as the new account's photo. Best effort; the account works without it. */
    void downloadGooglePhoto(final long forUser, String photoUrl) {
        if (photoUrl == null || !photoUrl.startsWith("https://")) return;
        final String url = photoUrl.replaceAll("=s\\d+-c$", "=s320-c");
        new Thread(() -> {
            String name = null;
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(url).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                if (c.getResponseCode() == 200) {
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int n, total = 0;
                    try (InputStream in = c.getInputStream()) {
                        while ((n = in.read(buf)) > 0 && total < 2_000_000) { bo.write(buf, 0, n); total += n; }
                    }
                    Bitmap bm = BitmapFactory.decodeByteArray(bo.toByteArray(), 0, bo.size());
                    if (bm != null) {
                        int sz = Math.min(bm.getWidth(), bm.getHeight());
                        Bitmap sq = Bitmap.createBitmap(bm, (bm.getWidth() - sz) / 2, (bm.getHeight() - sz) / 2, sz, sz);
                        Bitmap out = sz > 320 ? Bitmap.createScaledBitmap(sq, 320, 320, true) : sq;
                        String fn = "av_" + System.currentTimeMillis() + ".jpg";
                        try (FileOutputStream fo = new FileOutputStream(new File(getFilesDir(), fn))) {
                            out.compress(Bitmap.CompressFormat.JPEG, 88, fo);
                        }
                        name = fn;
                    }
                }
            } catch (Exception ignored) {
                // no photo is not an error
            } finally {
                if (c != null) c.disconnect();
            }
            final String file = name;
            if (file == null) return;
            runOnUiThread(() -> {
                User u = db.user(forUser);
                if (u == null || !u.getAvatar().isEmpty()) { new File(getFilesDir(), file).delete(); return; }
                db.setUserAvatar(forUser, "f:" + file);
                if (userId == forUser) {
                    userAvatar = "f:" + file;
                    if (!dead() && (cur.equals("role") || cur.equals("more") || cur.equals("personal"))) render();
                }
            });
        }).start();
    }
}
