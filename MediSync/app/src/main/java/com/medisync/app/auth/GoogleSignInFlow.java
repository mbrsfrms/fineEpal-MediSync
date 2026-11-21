package com.medisync.app.auth;

import android.app.Activity;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.GetCredentialInterruptedException;
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException;
import androidx.credentials.exceptions.NoCredentialException;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

abstract class GoogleSignInFlow extends GoogleSignInBase {
    public static void start(final Activity activity, final SignInCallback cb) {
        if (configurationProblem(activity) != null) { cb.onFailure(Failure.NOT_CONFIGURED); return; }
        final CredentialManager manager;
        GetCredentialRequest request;
        try {
            manager = CredentialManager.create(activity);
            GetSignInWithGoogleOption option = new GetSignInWithGoogleOption.Builder(webClientId(activity)).build();
            request = new GetCredentialRequest.Builder().addCredentialOption(option).build();
        } catch (RuntimeException e) {
            cb.onFailure(Failure.PROVIDER_UNAVAILABLE);
            return;
        }
        try {
            manager.getCredentialAsync(activity, request, new CancellationSignal(),
                    ContextCompat.getMainExecutor(activity),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            if (activity.isFinishing() || activity.isDestroyed()) return;
                            if (result == null || result.getCredential() == null) {
                                cb.onFailure(Failure.FAILED);
                                return;
                            }
                            handleCredential(activity, result.getCredential(), cb);
                        }

                        @Override
                        public void onError(@NonNull GetCredentialException e) {
                            if (!activity.isFinishing() && !activity.isDestroyed()) {
                                cb.onFailure(classify(e));
                            }
                        }
                    });
        } catch (RuntimeException e) {
            cb.onFailure(Failure.PROVIDER_UNAVAILABLE);
        }
    }

    static void handleCredential(Activity activity, Credential credential, final SignInCallback cb) {
        if (!(credential instanceof CustomCredential)
                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            cb.onFailure(Failure.FAILED);
            return;
        }
        final String idToken = GoogleIdTokenCredential.createFrom(
                ((CustomCredential) credential).getData()
        ).getIdToken();

        try {
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(activity, task -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        if (!task.isSuccessful()) {
                            cb.onFailure(classify(task.getException()));
                            return;
                        }
                        FirebaseUser user = task.getResult() == null ? null : task.getResult().getUser();
                        if (user == null || !isGoogleBacked(user)) {
                            FirebaseAuth.getInstance().signOut();
                            cb.onFailure(Failure.FAILED);
                            return;
                        }
                        cb.onSignedIn(user);
                    });
        } catch (RuntimeException e) {
            cb.onFailure(Failure.FAILED);
        }
    }

    static boolean isGoogleBacked(FirebaseUser user) {
        for (UserInfo info : user.getProviderData()) {
            if (GoogleAuthProvider.PROVIDER_ID.equals(info.getProviderId())) return true;
        }
        return false;
    }

    static Failure classify(GetCredentialException e) {
        if (e instanceof GetCredentialCancellationException) return Failure.CANCELLED;
        if (e instanceof NoCredentialException) return Failure.NO_ACCOUNT;
        if (e instanceof GetCredentialInterruptedException) return Failure.NETWORK;
        if (e instanceof GetCredentialProviderConfigurationException) return Failure.PROVIDER_UNAVAILABLE;
        return Failure.FAILED;
    }

    static Failure classify(Exception e) {
        if (e instanceof FirebaseNetworkException) return Failure.NETWORK;
        if (e instanceof FirebaseTooManyRequestsException) return Failure.TOO_MANY_REQUESTS;
        if (e instanceof FirebaseAuthInvalidUserException) return Failure.ACCOUNT_DISABLED;
        if (e instanceof FirebaseAuthInvalidCredentialsException) return Failure.FAILED;
        if (e instanceof FirebaseAuthException) return Failure.FAILED;
        return Failure.FAILED;
    }
}
