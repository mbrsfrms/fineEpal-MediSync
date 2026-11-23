package com.medisync.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

abstract class 2PhotoStorage extends DoseLogic {
    /** Copies the chosen picture into the app's private storage as a small square JPEG. */
    String savePhoto(Uri uri) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            InputStream in = getContentResolver().openInputStream(uri);
            if (in == null) return null;
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            int sample = 1;
            while (o.outWidth / sample > 700 || o.outHeight / sample > 700) sample *= 2;
            o = new BitmapFactory.Options();
            o.inSampleSize = sample;
            in = getContentResolver().openInputStream(uri);
            if (in == null) return null;
            Bitmap bm = BitmapFactory.decodeStream(in, null, o);
            in.close();
            if (bm == null) return null;
            int rot = 0;
            if (Build.VERSION.SDK_INT >= 24) {
                try (InputStream ei = getContentResolver().openInputStream(uri)) {
                    if (ei != null) {
                        ExifInterface ex = new ExifInterface(ei);
                        int ori = ex.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        if (ori == 6) rot = 90; else if (ori == 3) rot = 180; else if (ori == 8) rot = 270;
                    }
                } catch (Exception ignored) { }
            }
            int sz = Math.min(bm.getWidth(), bm.getHeight());
            Matrix m = new Matrix();
            m.postScale(320f / sz, 320f / sz);
            if (rot != 0) m.postRotate(rot);
            Bitmap out = Bitmap.createBitmap(bm, (bm.getWidth() - sz) / 2, (bm.getHeight() - sz) / 2, sz, sz, m, true);
            String name = "av_" + System.currentTimeMillis() + ".jpg";
            try (FileOutputStream fo = new FileOutputStream(new File(getFilesDir(), name))) {
                out.compress(Bitmap.CompressFormat.JPEG, 88, fo);
            }
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    void deleteAvatarFile(String old, String neu) {
        if (old != null && old.startsWith("f:") && !old.equals(neu)) {
            try { new File(getFilesDir(), old.substring(2)).delete(); } catch (Exception ignored) { }
            AvatarView.CACHE.remove(old.substring(2));
        }
    }
}
