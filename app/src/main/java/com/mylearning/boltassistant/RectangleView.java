package com.mylearning.boltassistant;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Date;

public class RectangleView extends AbstractShapeView {

    private static final int REQUEST_SET_VALUES = 2;
    private final String TAG = "RectangleView";
    private int index;
    private int width, height;
    private String type = ShapeViewType.RECTANGLE.toString();
    private Date date;
    private Date time;
    private String category;
    private float km;
    private float price;
    private String pickup;
    private String dropoff;





    public RectangleView(Context context, int x, int y, int width, int height, int index) {
        super(context, x, y);
        this.index = index;
        this.width = width;
        this.height = height;
        this.date = new Date();
        this.time = new Date();
        this.category = "bolt";
        this.km =7;
        this.price = 12;
        this.pickup = "Lisbon";
        this.dropoff ="Lisbon";
        init();
    }


    public RectangleView(Context context, RectangleData rectangleData) {
        this(context, rectangleData.getX(), rectangleData.getY(), rectangleData.getWidth(), rectangleData.getHeight(), rectangleData.getIndex());
        this.date = rectangleData.getDate();
        this.time = rectangleData.getTime();
        this.category = rectangleData.getCategory();
        this.km = rectangleData.getKm();
        this.price = rectangleData.getPrice();
        this.pickup = rectangleData.getPickup();
        this.dropoff = rectangleData.getDropoff();
    }
    public RectangleData createRectangleData(){
        //RectangleData(int x, int y, int width, int height, int index, String type, Date date, Date time,
                //String category, float km, float price, String pickup, String dropoff) {
        return new RectangleData(layoutParams.x, layoutParams.y, getViewWidth(), getViewHeight(),index, ShapeViewType.RECTANGLE.name(),
                date, time, category, km, price, pickup, dropoff);
    }

    public RectangleView(Context context, int x, int y, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflateLayout(R.layout.rectangle_view);
        TextView indexView = findViewById(R.id.rectangle_index);
        indexView.setText(String.valueOf(index));

        View rectangleView = findViewById(R.id.rectangle);
        rectangleView.getLayoutParams().width = width;
        rectangleView.getLayoutParams().height = height;
        rectangleView.requestLayout();

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                launchSetValuesActivity();
                return true;
            }
        });
    }

    private void launchSetValuesActivity() {
        if(SetRectangleValuesActivity.is_running){
            Log.d(TAG, "Another Rectangle is going to update itself");
            return;
        }

        Context context = getContext();
        Intent intent = new Intent(context, SetRectangleValuesActivity.class);
        intent.putExtra(SetRectangleValuesActivity.EXTRA_DATE, date.getTime());
        intent.putExtra(SetRectangleValuesActivity.EXTRA_TIME, time.getTime());
        intent.putExtra(SetRectangleValuesActivity.EXTRA_CATEGORY, category);
        intent.putExtra(SetRectangleValuesActivity.EXTRA_KM, km);
        intent.putExtra(SetRectangleValuesActivity.EXTRA_PRICE, price);
        intent.putExtra(SetRectangleValuesActivity.EXTRA_PICKUP, pickup);
        intent.putExtra(SetRectangleValuesActivity.EXTRA_DROPOFF, dropoff);
        intent.putExtra(SetRectangleValuesActivity.EXTRA_WIDTH, width);
        intent.putExtra(SetRectangleValuesActivity.EXTRA_HEIGHT, height);
        intent.putExtra(SetRectangleValuesActivity.ACTION_ID, index);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @Override
    protected String getBroadcastAction() {
        return SetRectangleValuesActivity.ACTION_UPDATE_VALUES;
    }

    @Override
    protected void handleBroadcast(Intent intent) {
        if (intent.getAction() != null && intent.getIntExtra(SetRectangleValuesActivity.ACTION_ID,1)==index) {
            Log.d(TAG, "A broadcast recieved to update the value of the Rectangle Data");
            date = new Date(intent.getLongExtra(SetRectangleValuesActivity.EXTRA_DATE, new Date().getTime()));
            time = new Date(intent.getLongExtra(SetRectangleValuesActivity.EXTRA_TIME, new Date().getTime()));
            category = intent.getStringExtra(SetRectangleValuesActivity.EXTRA_CATEGORY);
            km = intent.getFloatExtra(SetRectangleValuesActivity.EXTRA_KM, 0.0f);
            price = intent.getFloatExtra(SetRectangleValuesActivity.EXTRA_PRICE, 0.0f);
            pickup = intent.getStringExtra(SetRectangleValuesActivity.EXTRA_PICKUP);
            dropoff = intent.getStringExtra(SetRectangleValuesActivity.EXTRA_DROPOFF);
            int newWidth = intent.getIntExtra(SetRectangleValuesActivity.EXTRA_WIDTH, 100);
            int newHeight = intent.getIntExtra(SetRectangleValuesActivity.EXTRA_HEIGHT, 50);
            if(newWidth!=width || newHeight!=height){
                updateViewData(newWidth,newHeight);
                width=newWidth;
                height=newHeight;
            }


        }
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        return touchX >= getShapeX() && touchX <= getShapeX() + width && touchY >= getShapeY() && touchY <= getShapeX() + height;
    }




    private void updateViewData(int newWidth, int newHeight) {
        setShapeWidth(newWidth);
        setShapeHeight(newHeight);

    }

    @Override
    public void handleLongTouch() {
        Log.d(TAG, "handleLongTouch event triggered");
        launchSetValuesActivity();
    }

    public int getViewWidth() {
        return width;
    }

    public void setShapeWidth(int width) {
        this.width = width;
        View rectangleView = findViewById(R.id.rectangle);
        rectangleView.getLayoutParams().width = width;
        rectangleView.requestLayout();
    }

    public int getViewHeight() {
        return height;
    }

    public void setShapeHeight(int height) {
        this.height = height;
        View rectangleView = findViewById(R.id.rectangle);
        rectangleView.getLayoutParams().height = height;
        rectangleView.requestLayout();
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

    public boolean isTouched(int touchX, int touchY) {
        return touchX >= getShapeX() && touchX <= getShapeX() + width && touchY >= getShapeY() && touchY <= getShapeY() + height;
    }

    @Override
    public String toJson() {
        Gson gson = new Gson();
        RectangleData rectangleData = new RectangleData(layoutParams.x, layoutParams.y, width, height, index, type, date, time, category, km, price, pickup, dropoff);
        return gson.toJson(rectangleData);
    }

    @Override
    String getType() {
        return type;
    }

    @Override
    public void execute(MyAccessibilityService service) {
        service.addCommand(new ReadAllTextInAllDepthCommand(service, this));
        // Your execution logic here
    }

    @Override
    public String toString() {
        return "Rectangle[" + index + "](" + layoutParams.x + ", " + layoutParams.y + ", " + width + ", " + height + ")";
    }
}
