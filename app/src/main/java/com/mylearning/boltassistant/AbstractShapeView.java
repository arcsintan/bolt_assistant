package com.mylearning.boltassistant;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
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
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = x;
        layoutParams.y = y;

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouch(event);
            }
        });
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

    public boolean handleTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                MyLog.d(TAG, "ACTION_DOWN case is running");
                initialX = layoutParams.x;
                initialY = layoutParams.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                MyLog.d(TAG, "ACTION_MOVE case is running");
                layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                layoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                updateView();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return true;
        }
        return false;
    }

    public abstract int getViewWidth();

    public abstract int getViewHeight();
    public abstract void execute(MyAccessibilityService service);
    public abstract String toJson();
    abstract String getType();

}
