package com.mylearning.boltassistant;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    public static void saveBitmapToGallery(Context context, Bitmap bitmap) {
        OutputStream fos = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "pic_rect_" + timeStamp + ".png";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BoltAssistant");

                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    fos = context.getContentResolver().openOutputStream(uri);
                    if (fos != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                        Log.d(TAG, "Bitmap saved to gallery at " + uri.toString());
                    }
                }
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/BoltAssistant";
                File directory = new File(imagesDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                File image = new File(imagesDir, fileName);
                fos = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();

                // Add the image to the gallery
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, image.getAbsolutePath());
                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Log.d(TAG, "Bitmap saved to gallery at " + image.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing OutputStream", e);
                }
            }
        }
    }
}
