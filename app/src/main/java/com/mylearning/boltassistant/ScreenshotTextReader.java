package com.mylearning.boltassistant;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScreenshotTextReader implements TextReader {
    private static final String TAG = "ScreenshotTextReader";
    private final Activity activity;

    public ScreenshotTextReader(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void readTextFromRectangle(Rect rectangle, TextReaderCallback callback) {
        if (rectangle.width() < 32 || rectangle.height() < 32) {
            callback.onError(new Exception("Cropped image dimensions are too small. Width and height must be at least 32 pixels."));
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isWindowReady()) {
                captureScreenArea(rectangle, callback);
            } else {
                callback.onError(new Exception("Window is not ready for pixel copy."));
            }
        } else {
            callback.onError(new Exception("API level below 26 is not supported for this method."));
        }
    }

    private boolean isWindowReady() {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        return (decorView != null && decorView.getWidth() > 0 && decorView.getHeight() > 0);
    }

    private void captureScreenArea(Rect rect, TextReaderCallback callback) {
        Window window = activity.getWindow();
        Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PixelCopy.request(window, rect, bitmap, copyResult -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    // Save bitmap using ImageUtils
                    ImageUtils.saveBitmapToGallery(activity, bitmap);
                    processBitmap(bitmap, callback);
                } else {
                    callback.onError(new Exception("Failed to capture screen area."));
                }
            }, new Handler(activity.getMainLooper()));  // Ensure the handler is created with the main looper
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
