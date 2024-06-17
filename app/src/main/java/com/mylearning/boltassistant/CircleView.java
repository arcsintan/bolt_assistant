package com.mylearning.boltassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Random;

public class CircleView extends AbstractShapeView {
    private final String TAG = "CircleView";
    private int index;
    private int radius;
    private int duration;
    private int timeUntilNextCommand;
    private int centerX, centerY;





    public CircleView(Context context, int x, int y, int radius, int duration, int timeUntilNextCommand, int index) {
        super(context, x, y);
        this.index = index;
        this.radius = radius;
        this.duration=duration;
        this.timeUntilNextCommand=timeUntilNextCommand;

        init();
    }

    @Override
    public void handleBroadcast(Intent intent) {
        if (intent != null && intent.getIntExtra(SetCircleValuesActivity.ACTION_ID, 1)==uniq_id) {
            int newDuration = intent.getIntExtra(SetCircleValuesActivity.EXTRA_DURATION, duration);
            int newTimeUntilNextCommand = intent.getIntExtra(SetCircleValuesActivity.EXTRA_TIME_UNTIL_NEXT_COMMAND, timeUntilNextCommand);
            updateValues(newDuration, newTimeUntilNextCommand);
        }else{
            Log.e(TAG, "A null intent received from SetCircleValuesActivity");
        }

    }

    public CircleView(Context context, CircleData circleData) {
        this(context, circleData.getX(), circleData.getY(), circleData.getRadius(), circleData.getDuration(), circleData.getTimeUntilNextCommand(), circleData.getIndex());
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected String getBroadcastAction() {
        return SetCircleValuesActivity.ACTION_UPDATE_VALUES;
    }


    private void init() {
        inflateLayout(R.layout.circle_view);
        TextView indexView = findViewById(R.id.circle_index);
        indexView.setText(String.valueOf(index));

        View circleView = findViewById(R.id.circle);
        circleView.getLayoutParams().width = (int) (2 * radius);
        circleView.getLayoutParams().height = (int) (2 * radius);
        circleView.requestLayout();

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                launchSetValuesActivity();
                return true;
            }
        });
    }
    private  void updateValues(int duration, int timeUntilNextCommand){
        Log.d(TAG,"A borad cast recieved duration="+duration+" , next command="+timeUntilNextCommand);
        this.duration=duration;
        this.timeUntilNextCommand=timeUntilNextCommand;
        Log.d(TAG, this.toString());
    }

    private void launchSetValuesActivity() {
        setUniqID();
        Log.d(TAG, this.toString());
        Context context = getContext();
        Intent intent = new Intent(context, SetCircleValuesActivity.class);
        intent.putExtra(SetCircleValuesActivity.EXTRA_DURATION, duration);
        intent.putExtra(SetCircleValuesActivity.EXTRA_TIME_UNTIL_NEXT_COMMAND, timeUntilNextCommand);
        intent.putExtra(SetCircleValuesActivity.ACTION_INDEX, index);
        intent.putExtra(SetCircleValuesActivity.ACTION_ID, uniq_id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void handleLongTouch() {
        Log.d(TAG, "handleLongTouch event triggered");
        launchSetValuesActivity();
    }

    public int getViewWidth() {
        return 2 * radius;
    }

    public int getViewHeight() {
        return 2 * radius;
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
@Override
    protected boolean isTouched(float touchX, float touchY) {
        float dx = touchX - (getShapeX() + radius);
        float dy = touchY - (getShapeY() + radius);
        return dx * dx + dy * dy <= radius * radius;
    }

    @Override
    protected void setUniqID() {
        uniq_id= rn_gen.nextInt();
    }

    @Override
    public String toJson() {
        Gson gson = new Gson();
        CircleData circleData = new CircleData(layoutParams.x, layoutParams.y, radius, duration, timeUntilNextCommand, index, getType());
        return gson.toJson(circleData);
    }

    @Override
    String getType() {
        return ShapeViewType.CIRCLE.toString();
    }

    @Override
    public void execute(MyAccessibilityService service) {
        service.addCommand(new SimulateTouchCommand(service, layoutParams.x + radius, layoutParams.y + radius + 35, 45, timeUntilNextCommand));
    }

    @Override
    public String toString() {
        return new CircleData(layoutParams.x, layoutParams.y, radius, duration, timeUntilNextCommand, index, getType()).toString();
    }
}