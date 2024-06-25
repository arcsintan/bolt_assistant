package com.mylearning.boltassistant;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mylearning.boltassistant.ShowDataBase.TripListActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean isOverlayServiceRunning = false;

    private ActivityResultLauncher<Intent> overlayPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;
    // private ActivityResultLauncher<Intent> mediaProjectionLauncher; // Commented out
    private Button toggleOverlayButton;
    private Button showTripsButton;

    // private MediaProjectionManager projectionManager; // Commented out

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleOverlayButton = findViewById(R.id.button_toggle_overlay);
        // projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE); // Commented out
        showTripsButton = findViewById(R.id.button_show_trips);

        toggleOverlayButton.setOnClickListener(v -> {
            if (isOverlayServiceRunning) {
                stopOverlayService();
            } else {
                checkOverlayPermissionAndStartService();
            }
        });
        showTripsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TripListActivity.class);
            startActivity(intent);
        });

        overlayPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                        if (Settings.canDrawOverlays(this)) {
                            checkAccessibilityPermission();
                        } else {
                            Toast.makeText(this, "Overlay permission is required for this app to function", Toast.LENGTH_LONG).show();
                        }

                });

        storagePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Storage permission granted.");
                    } else {
                        Log.d(TAG, "Storage permission denied.");
                        Toast.makeText(this, "Storage permission is required to save images to the gallery.", Toast.LENGTH_SHORT).show();
                    }
                });

        // mediaProjectionLauncher = registerForActivityResult( // Commented out
        //         new ActivityResultContracts.StartActivityForResult(),
        //         result -> {
        //             if (result.getResultCode() == Activity.RESULT_OK) {
        //                 Intent serviceIntent = new Intent(this, OverlayService.class);
        //                 serviceIntent.putExtra("resultCode", result.getResultCode());
        //                 serviceIntent.putExtra("data", result.getData());
        //                 startService(serviceIntent);
        //                 isOverlayServiceRunning = true;
        //                 toggleOverlayButton.setText("Disable");
        //             } else {
        //                 Toast.makeText(this, "Screen capture permission denied.", Toast.LENGTH_SHORT).show();
        //             }
        //         });

        checkStoragePermissionAndRequest();
        checkOverlayPermissionAndStartService();
    }

    // private void startMediaProjectionRequest() { // Commented out
    //     Intent captureIntent = projectionManager.createScreenCaptureIntent();
    //     mediaProjectionLauncher.launch(captureIntent);
    // }

    private void checkStoragePermissionAndRequest() {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            }

    }

    private void requestStoragePermission() {
        new AlertDialog.Builder(this)
                .setTitle("Storage Permission Required")
                .setMessage("This app needs storage permission to save images to the gallery. Please grant storage permission.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Storage permission is required to save images to the gallery.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void checkOverlayPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission();
            } else {
                checkAccessibilityPermission();
            }
        } else {
            checkAccessibilityPermission();
        }
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        overlayPermissionLauncher.launch(intent);
    }

    private void checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Accessibility Permission Required")
                    .setMessage("This app needs accessibility permission to function properly. Please enable accessibility permission.")
                    .setPositiveButton("Grant", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Toast.makeText(this, "Accessibility permission is required for this app to function.", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        } else {
            startOverlayService(); // Start the overlay service directly
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String prefString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (prefString == null) {
            return false;
        }

        ComponentName componentName = new ComponentName(this, MyAccessibilityService.class);
        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(prefString);
        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledComponent = ComponentName.unflattenFromString(componentNameString);
            if (enabledComponent != null && enabledComponent.equals(componentName)) {
                return true;
            }
        }

        return false;
    }

    private void startOverlayService() {
        Intent overlayServiceIntent = new Intent(this, OverlayService.class);
        startService(overlayServiceIntent);
        isOverlayServiceRunning = true;
        toggleOverlayButton.setText("Disable");
    }

    private void stopOverlayService() {
        Intent overlayServiceIntent = new Intent(this, OverlayService.class);
        stopService(overlayServiceIntent);
        isOverlayServiceRunning = false;
        toggleOverlayButton.setText("Enable");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityManager.getInstance().setCurrentActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().setCurrentActivity(null);
        stopOverlayService();
    }
}
