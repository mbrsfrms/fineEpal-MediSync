package com.medisync.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import java.io.File;
import java.util.HashMap;

/** Avatar: a saved photo or a drawn animal/person icon. */
class AvatarView extends View {
    static final HashMap<String, Bitmap> CACHE = new HashMap<>();
    final String avatar;
    final int fallback;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint bp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    AvatarView(Context c, String avatar, int fallback) {
        super(c);
        this.avatar = avatar == null ? "" : avatar;
        this.fallback = fallback;
    }

    static Bitmap photo(Context ctx, String name) {
        Bitmap b = CACHE.get(name);
        if (b != null) return b;
        try {
            b = BitmapFactory.decodeFile(new File(ctx.getFilesDir(), name).getAbsolutePath());
            if (b != null) CACHE.put(name, b);
        } catch (Exception ignored) { }
        return b;
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), s = Math.min(w, h);
        if (s <= 0) return;
        float cx = w / 2f, cy = h / 2f;
        if (avatar.startsWith("f:")) {
            Bitmap bm = photo(getContext(), avatar.substring(2));
            if (bm != null) {
                Path clip = new Path();
                clip.addCircle(cx, cy, s / 2f, Path.Direction.CW);
                c.save();
                c.clipPath(clip);
                c.drawBitmap(bm, null, new RectF(cx - s / 2f, cy - s / 2f, cx + s / 2f, cy + s / 2f), bp);
                c.restore();
                return;
            }
        }
        int kind = fallback + 8;
        if (avatar.startsWith("p:")) {
            try { kind = Integer.parseInt(avatar.substring(2)); } catch (Exception ignored) { }
        }
        if (kind < 0 || kind > 11) kind = fallback + 8;
        if (kind < 8) AnimalArt.drawAnimal(c, p, cx, cy, s, kind);
        else PersonArt.drawPerson(c, p, cx, cy, s, kind - 8);
    }

}
