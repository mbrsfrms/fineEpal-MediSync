package com.medisync.app.auth;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;

public abstract class GoogleSignInBase {
    public enum Failure {
        CANCELLED,
        NO_ACCOUNT,
        NETWORK,
        NOT_CONFIGURED,
        PROVIDER_UNAVAILABLE,
        ACCOUNT_DISABLED,
        TOO_MANY_REQUESTS,
        FAILED
    }

    public interface SignInCallback {
        void onSignedIn(FirebaseUser user);
        void onFailure(Failure failure);
    }

    public enum SessionState {
        VALID,
        REVOKED,
        UNKNOWN
    }

    public interface SessionCallback {
        void onResult(SessionState state);
    }

    public static boolean isFirebaseReady(Context ctx) {
        return !FirebaseApp.getApps(ctx).isEmpty();
    }

    public static String configurationProblem(Context ctx) {
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            return "Firebase is not set up in this build. Add google-services.json to the app folder (see FIREBASE_SETUP.md) and rebuild.";
        }
        if (webClientId(ctx) == null) {
            return "Google sign-in is not enabled for this Firebase project. Enable the Google provider, add your SHA-1 and SHA-256, then download google-services.json again (see FIREBASE_SETUP.md).";
        }
        return null;
    }

    static String webClientId(Context ctx) {
        int id = ctx.getResources().getIdentifier("default_web_client_id", "string", ctx.getPackageName());
        if (id == 0) return null;
        String v = ctx.getString(id);
        return v == null || v.trim().isEmpty() ? null : v;
    }

    public static boolean isOnline(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return true; // cannot tell; let the real request decide
            Network n = cm.getActiveNetwork();
            if (n == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(n);
            return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (RuntimeException e) {
            return true;
        }
    }
}
