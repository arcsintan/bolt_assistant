package com.mylearning.boltassistant;

import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ScreenshotService extends Service {
    private static final String TAG = "ScreenshotService";
    public static final String ACTION_OCR_RESULT = "com.mylearning.boltassistant.ACTION_OCR_RESULT";

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Rect rect;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");

        if (intent != null && intent.hasExtra("resultCode") && intent.hasExtra("data") && intent.hasExtra("rect")) {
            int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_OK);
            Intent resultData = intent.getParcelableExtra("data");
            rect = intent.getParcelableExtra("rect"); // Receive the Rect here

            projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData);
            setupVirtualDisplay();
        } else {
            Log.e(TAG, "Intent does not contain required extras");
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void setupVirtualDisplay() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int screenDensity = getResources().getDisplayMetrics().densityDpi;

        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 1);
        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture",
                screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);

        imageReader.setOnImageAvailableListener(reader -> {
            Image image = null;
            Bitmap bitmap = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    Log.d(TAG, "Image acquired for processing");
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * screenWidth;

                    bitmap = Bitmap.createBitmap(screenWidth + rowPadding / pixelStride, screenHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    Log.d(TAG, "Full bitmap size: width=" + bitmap.getWidth() + ", height=" + bitmap.getHeight());

                    // Log the coordinates and dimensions of the rectangle to be cropped
                    Log.d(TAG, "Cropping rectangle: left=" + rect.left + ", top=" + rect.top + ", width=" + rect.width() + ", height=" + rect.height());

                    // Check if the cropping rectangle is within the bounds of the full bitmap
                    if (rect.left >= 0 && rect.top >= 0 && rect.right <= bitmap.getWidth() && rect.bottom <= bitmap.getHeight()) {
                        // Crop the bitmap to the specified rectangle
                        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
                        Log.d(TAG, "Cropped bitmap size: width=" + croppedBitmap.getWidth() + ", height=" + croppedBitmap.getHeight());

                        // Perform OCR on the cropped bitmap
                        performOCR(croppedBitmap);

                        // Save the full screenshot to the gallery for debugging
                        String fullScreenshotFileName = "full_screenshot_" + System.currentTimeMillis() + ".png";
                        saveBitmapToGallery(bitmap, fullScreenshotFileName);

                        // Save the cropped bitmap to the gallery for debugging
                        String croppedScreenshotFileName = "cropped_screenshot_" + System.currentTimeMillis() + ".png";
                        saveBitmapToGallery(croppedBitmap, croppedScreenshotFileName);

                        // Log the file names
                        Log.d(TAG, "Full screenshot saved as: " + fullScreenshotFileName);
                        Log.d(TAG, "Cropped screenshot saved as: " + croppedScreenshotFileName);
                    } else {
                        Log.e(TAG, "Cropping rectangle is out of bounds of the full bitmap");
                    }

                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    if (image != null) {
                        image.close();
                        Log.d(TAG, "Image closed after processing");
                    }
                }

            } finally {
                if (image != null) {
                    image.close();
                    Log.d(TAG, "Image closed after processing");
                }
                if (bitmap != null) {
                    bitmap.recycle();
                }
                stopSelf();
            }
        }, new Handler(getMainLooper()));
    }


    private void performOCR(Bitmap bitmap) {
        Log.d(TAG, "Starting OCR process");
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    StringBuilder recognizedText = new StringBuilder();
                    for (com.google.mlkit.vision.text.Text.TextBlock block : visionText.getTextBlocks()) {
                        recognizedText.append(block.getText()).append("\n");
                    }
                    String resultText = recognizedText.toString();
                    Log.d(TAG, "Recognized text: " + resultText);

                    // Broadcast the recognized text
                    broadcastOCRResult(resultText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed", e);
                });
    }

    private void broadcastOCRResult(String recognizedText) {
        Intent intent = new Intent(ACTION_OCR_RESULT);
        intent.putExtra("recognizedText", recognizedText);
        sendBroadcast(intent);
        Log.d(TAG, "Broadcasting OCR result");
    }

    private void saveBitmapToGallery(Bitmap bitmap, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Log.d(TAG, "Screenshot saved to gallery: " + fileName);
            } catch (IOException e) {
                Log.e(TAG, "Failed to save screenshot", e);
            }
        } else {
            Log.e(TAG, "Failed to create new MediaStore record");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (imageReader != null) {
            imageReader.close();
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
    }
}
