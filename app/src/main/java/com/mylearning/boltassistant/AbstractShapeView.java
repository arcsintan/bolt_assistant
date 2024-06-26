package com.mylearning.boltassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public abstract class AbstractShapeView extends RelativeLayout {
    final String TAG = "AbstractShapeView";
    protected WindowManager windowManager;
    protected WindowManager.LayoutParams layoutParams;
    protected boolean isTouchable = true;


    private int initialX, initialY;
    private float initialTouchX, initialTouchY;

    private GestureDetector gestureDetector;


    // BroadcastReceiver for receiving updated values
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "The ocReceive method of abstractShapeView Called");
            handleBroadcast(intent);
        }
    };



    public AbstractShapeView(Context context, int x, int y) {
        super(context);
        init(context, x, y);
    }

    public AbstractShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, 0, 0);
    }

    private void init(Context context, int x, int y) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = x;
        layoutParams.y = y;

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress event triggered");
                handleLongTouch();
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "onSingleTapUp event triggered");
                return super.onSingleTapUp(e);
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "ACTION_DOWN event triggered");
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "ACTION_MOVE event triggered");
                        layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        layoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        updateView();
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        Log.d(TAG, "ACTION_UP or ACTION_CANCEL event triggered");
                        return true;
                }
                return false;
            }
        });

        // Register BroadcastReceiver
        IntentFilter filter = new IntentFilter(getBroadcastAction());
        context.registerReceiver(broadcastReceiver, filter);
    }



    protected void inflateLayout(int layoutRes) {
        LayoutInflater.from(getContext()).inflate(layoutRes, this, true);
    }

    public void toggleFlags() {
        isTouchable = !isTouchable;
        layoutParams.flags = isTouchable ? WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        updateView();
    }

    public void updateView() {
        windowManager.updateViewLayout(this, layoutParams);
    }

    public int getShapeX() {
        return layoutParams.x;
    }

    public void setShapeX(int x) {
        layoutParams.x = x;
        updateView();
    }

    public int getShapeY() {
        return layoutParams.y;
    }

    public void setShapeY(int y) {
        layoutParams.y = y;
        updateView();
    }

    public abstract int getViewWidth();
    public abstract int getViewHeight();
    public abstract void addAccesibilityCommand(MyAccessibilityService service);
    public abstract String toJson();
    abstract String getType();

    public abstract void handleLongTouch();
    protected abstract String getBroadcastAction();

    protected abstract void handleBroadcast(Intent intent);
    protected void unregisterBroadcastReceiver(Context context) {
        context.unregisterReceiver(broadcastReceiver);
    }
    protected abstract boolean isTouched(float x, float y);
    public int[] getLocationOnScreen() {
        int[] location = new int[2];
        this.getLocationOnScreen(location);
        return location; // returns an array where location[0] is x and location[1] is y
    }
    public void setTouchThrough(boolean allowTouchThrough) {
        if (allowTouchThrough) {
            // Add FLAG_NOT_TOUCHABLE to allow touches to pass through this view to underlying windows
            isTouchable=false;
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        } else {
            // Remove FLAG_NOT_TOUCHABLE to make the view intercept touch events
            layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            isTouchable=true;
        }
        // Apply the new flags to the window
        windowManager.updateViewLayout(this, layoutParams);
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterBroadcastReceiver(getContext());
    }
}
