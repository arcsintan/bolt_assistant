package com.mylearning.boltassistant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.nio.ByteBuffer;

public class MediaProjectionTextReader implements TextReader {
    private static final String TAG = "M_project_texReader";
    private static final int MIN_WIDTH_HEIGHT = 32;

    private final Context context;
    private final MediaProjection mediaProjection;
    private final int screenWidth;
    private final int screenHeight;
    private final int screenDensity;

    public MediaProjectionTextReader(Context context, MediaProjection mediaProjection) {
        this.context = context;
        this.mediaProjection = mediaProjection;

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenDensity = metrics.densityDpi;
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    @Override
    public void readTextFromRectangle(Rect rectangle, TextReaderCallback callback) {
        if (rectangle.width() < MIN_WIDTH_HEIGHT || rectangle.height() < MIN_WIDTH_HEIGHT) {
            callback.onError(new Exception("Cropped image dimensions are too small. Width and height must be at least " + MIN_WIDTH_HEIGHT + " pixels."));
            return;
        }

        if (mediaProjection == null) {
            callback.onError(new Exception("MediaProjection is not available."));
            return;
        }

        captureScreenArea(rectangle, callback);
    }

    private void captureScreenArea(Rect rect, TextReaderCallback callback) {
        ImageReader imageReader = ImageReader.newInstance(rect.width(), rect.height(), ImageFormat.JPEG, 2);
        Surface surface = imageReader.getSurface();

        VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                rect.width(),
                rect.height(),
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null);

        HandlerThread handlerThread = new HandlerThread("ImageListener");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());

        imageReader.setOnImageAvailableListener(reader -> {
            try (Image image = reader.acquireLatestImage()) {
                if (image != null) {
                    processImage(image, rect, callback);
                }
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                reader.close();
                virtualDisplay.release();
                handlerThread.quitSafely();
            }
        }, handler);
    }

    private void processImage(Image image, Rect rect, TextReaderCallback callback) {
        Bitmap bitmap = null;
        try {
            Image.Plane[] planes = image.getPlanes();
            if (planes.length > 0) {
                ByteBuffer buffer = planes[0].getBuffer();
                bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                ImageUtils.saveBitmapToGallery(context, bitmap);
                processBitmap(bitmap, callback);
            }
        } catch (Exception e) {
            callback.onError(e);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    private void processBitmap(Bitmap bitmap, TextReaderCallback callback) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            com.google.mlkit.vision.text.TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        StringBuilder recognizedText = new StringBuilder();
                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            recognizedText.append(block.getText()).append("\n");
                        }
                        callback.onTextRead(recognizedText.toString());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Text recognition failed", e);
                        callback.onError(e);
                    });
        } catch (Exception e) {
            callback.onError(e);
        }
    }
}

