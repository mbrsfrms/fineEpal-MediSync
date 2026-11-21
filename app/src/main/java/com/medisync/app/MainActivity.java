package com.medisync.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.medisync.app.auth.GoogleSignIn;
import com.medisync.app.database.MediSyncDatabase;
import com.medisync.app.manager.AuthManager;
import com.medisync.app.manager.NotificationHelper;

/** App entry point: lifecycle, theme and new-intent handling. */
public class MainActivity extends HelpScreens {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = MediSyncDatabase.getInstance(this);
        auth = new AuthManager(db);
        pendingOpen = getIntent() == null ? null : getIntent().getStringExtra(NotificationHelper.EXTRA_OPEN);
        dark = "1".equals(db.get("dark", "0"));
        applyTheme();

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(dp(6), dp(4), dp(6), dp(4));
        nav.setElevation(dp(10));
        root.addView(nav, new LinearLayout.LayoutParams(-1, dp(72)));
        setContentView(root);
        root.setOnApplyWindowInsetsListener((v, ins) -> {
            v.setPadding(ins.getSystemWindowInsetLeft(), ins.getSystemWindowInsetTop(),
                    ins.getSystemWindowInsetRight(), ins.getSystemWindowInsetBottom());
            return ins;
        });
        NotificationHelper.createChannels(this);

        restoreFirebaseSession();
        render();
    }

    @Override public void onBackPressed() { back(); }

    @Override
    protected void onResume() {
        super.onResume();
        // re-check a saved session now and then so a revoked or expired one is noticed
        if (userId >= 0 && System.currentTimeMillis() - lastSessionCheck > 15 * 60 * 1000L) verifySavedSession();
    }

    @Override
    protected void onDestroy() {
        if (authListener != null && GoogleSignIn.isFirebaseReady(this)) {
            try { FirebaseAuth.getInstance().removeAuthStateListener(authListener); } catch (RuntimeException ignored) { }
        }
        super.onDestroy();
    }

    void applyTheme() {
        if (dark) {
            BG = 0xFF0E1A21; CARD = 0xFF16252E; INK = 0xFFE8F3F5; MUTED = 0xFF93AAB5;
            BORDER = 0xFF26404C; MINT = 0xFF173A3F; HINT = 0xFF5F7886;
        } else {
            BG = 0xFFF1FAFA; CARD = 0xFFFFFFFF; INK = 0xFF173B55; MUTED = 0xFF6D8797;
            BORDER = 0xFFD5E9ED; MINT = 0xFFE4F6F4; HINT = 0xFF9DB0BA;
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String open = intent.getStringExtra(NotificationHelper.EXTRA_OPEN);
        if (open != null) {
            pendingOpen = open;
            openPending();
        }
    }
}
