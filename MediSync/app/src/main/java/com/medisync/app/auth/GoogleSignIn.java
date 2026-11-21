package com.medisync.app.auth;

import android.app.Activity;
import android.content.Context;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.medisync.app.manager.AuthManager;

public final class GoogleSignIn extends GoogleSignInFlow {
    private GoogleSignIn() { }

    public static FirebaseUser currentUser(Context ctx) {
        if (FirebaseApp.getApps(ctx).isEmpty()) return null;
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static AuthManager.GoogleIdentity identityOf(FirebaseUser u) {
        String email = u.getEmail();
        if (email == null || email.isEmpty()) {
            for (UserInfo info : u.getProviderData()) {
                if (GoogleAuthProvider.PROVIDER_ID.equals(info.getProviderId()) && info.getEmail() != null) {
                    email = info.getEmail();
                }
            }
        }
        String photo = u.getPhotoUrl() == null ? null : u.getPhotoUrl().toString();
        return new AuthManager.GoogleIdentity(u.getUid(), email, u.isEmailVerified() && isGoogleBacked(u),
                u.getDisplayName(), photo);
    }

    public static void checkSession(final Activity activity, final FirebaseUser user, final SessionCallback cb) {
        user.reload().addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) { cb.onResult(SessionState.VALID); return; }
            Exception e = task.getException();
            if (e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException) {
                cb.onResult(SessionState.REVOKED); 
            } else {
                cb.onResult(SessionState.UNKNOWN);
            }
        });
    }

    public static void signOut(Context ctx) {
        if (FirebaseApp.getApps(ctx).isEmpty()) return;
        FirebaseAuth.getInstance().signOut();
        try {
            CredentialManager.create(ctx).clearCredentialStateAsync(new ClearCredentialStateRequest(),
                    new CancellationSignal(), ContextCompat.getMainExecutor(ctx),
                    new CredentialManagerCallback<Void, ClearCredentialException>() {
                        @Override public void onResult(Void result) { }
                        @Override public void onError(@NonNull ClearCredentialException e) { }
                    });
        } catch (RuntimeException ignored) {
        }
    }
}
