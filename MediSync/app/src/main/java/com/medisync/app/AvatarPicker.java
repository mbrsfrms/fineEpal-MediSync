package com.medisync.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


abstract class AvatarPicker extends PhotoStorage {
    FrameLayout avatarEdit(String avatar, final int fallback, int sizeDp, final StrCb cb) {
        final FrameLayout f = new FrameLayout(this);
        final String[] cur = {avatar == null ? "" : avatar};
        f.addView(new AvatarView(this, cur[0], fallback), new FrameLayout.LayoutParams(-1, -1));
        int bs = Math.max(24, sizeDp / 3);
        FrameLayout badge = new FrameLayout(this);
        GradientDrawable og = new GradientDrawable();
        og.setShape(GradientDrawable.OVAL);
        og.setColor(TEAL);
        og.setStroke(dp(2), CARD);
        badge.setBackground(og);
        badge.addView(new IconView(this, "pencil", Color.WHITE), new FrameLayout.LayoutParams(dp(bs * 0.56f), dp(bs * 0.56f), Gravity.CENTER));
        f.addView(badge, new FrameLayout.LayoutParams(dp(bs), dp(bs), Gravity.BOTTOM | Gravity.END));
        f.setOnClickListener(x -> pickAvatar(cur[0], v -> {
            cur[0] = v;
            f.removeViewAt(0);
            f.addView(new AvatarView(this, v, fallback), 0, new FrameLayout.LayoutParams(-1, -1));
            cb.on(v);
        }));
        return f;
    }

    void pickAvatar(String current, final StrCb cb) {
        final AlertDialog[] dd = new AlertDialog[1];
        final String cur = current == null ? "" : current;
        LinearLayout body = col();
        body.addView(tv("Pick a cute avatar", 12, MUTED, true));
        sp(body, 8);
        for (int r = 0; r < 3; r++) {
            LinearLayout rw = row();
            for (int k = 0; k < 4; k++) {
                final String val = "p:" + (r * 4 + k);
                FrameLayout cell = new FrameLayout(this);
                if (val.equals(cur)) {
                    GradientDrawable ring = new GradientDrawable();
                    ring.setShape(GradientDrawable.OVAL);
                    ring.setColor(Color.TRANSPARENT);
                    ring.setStroke(dp(3), TEAL);
                    cell.setBackground(ring);
                }
                cell.addView(new AvatarView(this, val, 0), new FrameLayout.LayoutParams(dp(54), dp(54), Gravity.CENTER));
                cell.setOnClickListener(x -> { dd[0].dismiss(); cb.on(val); });
                rw.addView(cell, new LinearLayout.LayoutParams(0, dp(66), 1));
            }
            body.addView(rw);
        }
        sp(body, 10);
        divider(body);
        LinearLayout g = row();
        g.setPadding(dp(2), dp(14), dp(2), dp(14));
        g.addView(new IconView(this, "image", TEAL), lp(28, 28));
        TextView gt = tv("Choose from gallery", 14, INK, true);
        gt.setPadding(dp(12), 0, 0, 0);
        g.addView(gt);
        g.setOnClickListener(x -> { dd[0].dismiss(); openGallery(cb); });
        body.addView(g);
        if (!cur.isEmpty()) {
            divider(body);
            TextView rm = tv("Use default picture", 14, MUTED, false);
            rm.setPadding(dp(2), dp(14), dp(2), dp(14));
            rm.setOnClickListener(x -> { dd[0].dismiss(); cb.on(""); });
            body.addView(rm);
        }
        dd[0] = dialog("Profile photo", body, null, null, "Cancel");
    }

    void openGallery(StrCb cb) {
        pendingPhotoCb = cb;
        try {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(i, "Choose a photo"), REQ_PHOTO);
        } catch (Exception e) {
            pendingPhotoCb = null;
            toast("No gallery app available.");
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != REQ_PHOTO) return;
        StrCb cb = pendingPhotoCb;
        pendingPhotoCb = null;
        if (res != RESULT_OK || data == null || data.getData() == null || cb == null) return;
        String file = savePhoto(data.getData());
        if (file == null) { toast("Couldn't load that photo."); return; }
        cb.on("f:" + file);
    }
}
