package com.mylearning.boltassistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class OverlayService extends Service {
    private static final String TAG = "OverlayService";
    private static final String CHANNEL_ID = "ForegroundServiceChannel";



    private TextView transparentTextView;
    private boolean isTextViewVisible = false;
    private WindowManager windowManager;
    private View buttonOverlayView;
    private Deque<AbstractShapeView> shapeViews;
    private BroadcastReceiver settingsResultReceiver;
    private String recognizedText;
    private int centerX;
    private int centerY;
    private volatile boolean is_running_loop = false;
    private Thread mainLoopThread;
    private PowerManager.WakeLock wakeLock;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");

        if (!Settings.canDrawOverlays(this)) {
            Log.d(TAG, "Overlay permission not granted, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        recognizedText = "";
        createNotificationChannel();
        startForegroundService();
        acquireWakeLock(); // Acquire the wake lock here

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        shapeViews = new LinkedList<>();
        initializeCenterCoordinates();
        setupButtonOverlay();
        registerSettingsResultReceiver();
        // Call this method in onStartCommand or setupButtonOverlay
        setupTransparentTextView();

        return START_STICKY;
    }





    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Overlay Service")
                .setContentText("Your app is running in the background")
                .setSmallIcon(R.drawable.ic_notification) // Ensure you have an appropriate icon
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void setupButtonOverlay() {
        if (buttonOverlayView == null) {
            buttonOverlayView = LayoutInflater.from(this).inflate(R.layout.button_overlay_layout, null, false);

            int layoutFlag =
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            WindowManager.LayoutParams buttonParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            buttonParams.gravity = Gravity.TOP | Gravity.START;
            buttonParams.x = 10;
            buttonParams.y = 750;

            windowManager.addView(buttonOverlayView, buttonParams);
            Log.d(TAG, "Button overlay view added");

            setButtonListeners();
            setButtonTouchListener(buttonParams);
        }
    }

    private void setButtonListeners() {
        buttonOverlayView.findViewById(R.id.button_play).setOnClickListener(v -> handlePlayButtonClick());
        buttonOverlayView.findViewById(R.id.button_add_circle).setOnClickListener(v -> handleAddCircleButtonClick());
        buttonOverlayView.findViewById(R.id.button_read_text).setOnClickListener(v -> handleAddRectangleButtonClick());
        buttonOverlayView.findViewById(R.id.button_stop).setOnClickListener(v -> handleStopButtonClick());
        buttonOverlayView.findViewById(R.id.button_settings).setOnClickListener(v -> handleSettingsButtonClick());
        buttonOverlayView.findViewById(R.id.button_remove).setOnClickListener(v -> handleRemoveButtonClick());
        buttonOverlayView.findViewById(R.id.button_toggle_textview).setOnClickListener(v -> handleToggleTextView());
    }

    private void handlePlayButtonClick() {
        if (!is_running_loop){
        MyAccessibilityService service = MyAccessibilityService.getInstance();
        // clearing all commands before each run


            if (service != null) {
                service.clearCommandList();
                if(shapeViews.size()==0){
                    Log.d(TAG, "No command to be executed!");
                    return;}
                for (AbstractShapeView shapeView : shapeViews) {
                    shapeView.setTouchThrough(true);
                    shapeView.addAccesibilityCommand(service);
                }
                MyLog.d(TAG, Thread.currentThread().getName());
                service.executeAllCommands();
            }
            for(AbstractShapeView shapeView:shapeViews) {
                Log.d(TAG, shapeView.toString());
                int []location=shapeView.getLocationOnScreen();
                Log.d(TAG, "location on screen="+location[0]+", "+location[1]);
            }

            is_running_loop=true;

        }else{
        Log.d(TAG, "Still busy with another loop");
    }

    }

    private void handleAddCircleButtonClick() {
        CircleView newCircle = new CircleView(getApplicationContext(), centerX, centerY, 50, 50, 200, shapeViews.size() + 1);
        shapeViews.add(newCircle);
        addShapeView(newCircle);
        Log.d(TAG, "Add Button clicked, new circle added");
    }

    private void handleAddRectangleButtonClick() {
        RectangleView newRectangle = new RectangleView(getApplicationContext(), centerX, centerY, 400, 200, shapeViews.size() + 1);
        shapeViews.add(newRectangle);
        addShapeView(newRectangle);
        Log.d(TAG, "Rotate Button clicked, new rectangle added");
    }

    private void handleStopButtonClick() {

        MyAccessibilityService service = MyAccessibilityService.getInstance();
        if (service != null) {
            service.stopAllCommands(); // Implement a method to safely stop command execution
            is_running_loop=false;
        }
        Log.d(TAG, "Stop Button clicked");
        for (AbstractShapeView shapeView : shapeViews) {
            shapeView.setTouchThrough(false);
        }
    }

    private void handleSettingsButtonClick() {
        if (SettingsActivity.is_running){
            Log.d(TAG, "There is another setting popup running");
            return;
        }
        Intent intent = new Intent(this, SettingsActivity.class);
        ArrayList<String> shapeViewJsons = new ArrayList<>();
        for (AbstractShapeView shapeView : shapeViews) {
            shapeViewJsons.add(shapeView.toJson());
        }
        intent.putExtra("shapeViewsJson", (Serializable) shapeViewJsons);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }




    private void handleRemoveButtonClick() {
        if (!shapeViews.isEmpty()) {
            AbstractShapeView shapeView = shapeViews.pollLast();
            MyAccessibilityService service=MyAccessibilityService.getInstance();
            service.removeLastCommand();
            Log.d(TAG, shapeView.toString());
            windowManager.removeView(shapeView);

        } else {
            Log.d(TAG, "Remove Button clicked, but no shapes to remove");
            Toast.makeText(this, "No shapes to remove", Toast.LENGTH_SHORT).show();
        }
    }

    private void setButtonTouchListener(WindowManager.LayoutParams buttonParams) {
        buttonOverlayView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = buttonParams.x;
                        initialY = buttonParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        buttonParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        buttonParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(buttonOverlayView, buttonParams);
                        return true;
                }
                return false;
            }
        });
    }

    private void addShapeView(AbstractShapeView shapeView) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,

                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.x = shapeView.getShapeX();
        params.y = shapeView.getShapeY();
        params.gravity = Gravity.TOP | Gravity.START;

        windowManager.addView(shapeView, params);
    }
    private void registerSettingsResultReceiver() {
        settingsResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<String> loadedJsons = (List<String>) intent.getSerializableExtra("loadedConfig");
                if (loadedJsons != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // first removing the shape like delete button
                        shapeViews.forEach(
                                windowManager::removeView);
                        shapeViews.clear();
                        for (String json : loadedJsons) {
                            MyLog.d(TAG, json);
                            AbstractShapeView shapeView=ShapeViewFactory.createShapeView(getApplication(), json);
                            shapeViews.add(shapeView);
                            addShapeView(shapeView);

                        }
                    }
                }
            }
        };


        IntentFilter filter = new IntentFilter(SettingsActivity.ACTION_SETTINGS_RESULT);

        registerReceiver(settingsResultReceiver, filter);


    }
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "OverlayService:WakeLock");
            wakeLock.acquire();
            Log.d(TAG, "WakeLock acquired");
        }
    }
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d(TAG, "WakeLock released");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock(); // Release the wake lock here
        if (mainLoopThread != null) {
            mainLoopThread.interrupt();
        }
        if (buttonOverlayView != null) {
            windowManager.removeView(buttonOverlayView);
            Log.d(TAG, "Button overlay view removed");
        }
        if(settingsResultReceiver!=null){
            unregisterReceiver(settingsResultReceiver);
        }

        while (!shapeViews.isEmpty()) {
            AbstractShapeView shapeView = shapeViews.poll();
            windowManager.removeView(shapeView);
        }
        Log.d(TAG, "OverlayService destroyed");
    }

    private void initializeCenterCoordinates() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        centerX = displayMetrics.widthPixels / 2;
        centerY = displayMetrics.heightPixels / 2;
    }

    private void createNotificationChannel() {

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d(TAG, "Notification channel created");
            }

    }

    // Add a method to set up the transparent TextView
    private void setupTransparentTextView() {
        transparentTextView = new TextView(this);
        transparentTextView.setBackgroundColor(0x55FF0000); // Transparent red background
        transparentTextView.setTextColor(0xFFFFFFFF); // White text color
        transparentTextView.setTextSize(24);
        transparentTextView.setVisibility(View.GONE); // Initially hidden

        WindowManager.LayoutParams textViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        textViewParams.gravity = Gravity.TOP | Gravity.START;
        textViewParams.x = 10;
        textViewParams.y = 10;

        windowManager.addView(transparentTextView, textViewParams);
    }

    private void handleToggleTextView() {
        isTextViewVisible = !isTextViewVisible;
        transparentTextView.setVisibility(isTextViewVisible ? View.VISIBLE : View.GONE);
        MyAccessibilityService.setCapture_screen(isTextViewVisible);
        buttonOverlayView.findViewById(R.id.button_toggle_textview).setBackgroundResource(isTextViewVisible? R.color.red: R.color.happy_green);
        if(isTextViewVisible)transparentTextView.setText("trip data");
        else transparentTextView.setText(null);

    }






















}
