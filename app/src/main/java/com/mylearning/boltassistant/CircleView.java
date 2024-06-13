package com.mylearning.boltassistant;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

public class CircleView extends AbstractShapeView{
    private final String TAG="CircleView";
    private int index;
    private int radius;
    private int centerX, centerY;
    private int duration;
    private int timeUtilNextCommand;
    private final String type=ShapeViewType.CIRCLE.toString();



    public CircleView(Context context,int x, int y,  int radius,int duration, int timeUntilNextCommand,  int index) {
        super(context, x, y);
        this.index = index;
        this.radius = radius;
        this.duration=duration;
        this.timeUtilNextCommand=timeUntilNextCommand;

        init();
    }
    public CircleView(Context context,CircleData circleData) {
        this(context, circleData.getX(), circleData.getY(), circleData.getRadius(), circleData.getDuration(), circleData.getTimeUntilNextCommand(), circleData.getIndex());
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }



    private void init() {
        inflateLayout(R.layout.circle_view);
        TextView indexView = findViewById(R.id.circle_index);
        indexView.setText(String.valueOf(index));

        View circleView = findViewById(R.id.circle);
        circleView.getLayoutParams().width = (int) (2 * radius);
        circleView.getLayoutParams().height = (int) (2 * radius);
        circleView.requestLayout();
    }

    public int getViewWidth() {
        return   2 * radius;
    }
    private void setCenter(){

        centerX=layoutParams.x+radius;
        centerY=layoutParams.y+radius;
    }

    public int getViewHeight() {

        return 2 * radius;
    }
    @Override
    public String toJson() {
        Gson gson = new Gson();
        CircleData circleData = new CircleData(layoutParams.x, layoutParams.y, radius, duration, timeUtilNextCommand, index, type);
        return gson.toJson(circleData);
    }

    @Override
    String getType() {
        return type;
    }








    @Override
    public void execute(MyAccessibilityService service) {
        service.enqueueCommand(new SimulateTouchCommand(service, layoutParams.x+radius, layoutParams.y+radius+30, 45, timeUtilNextCommand));
    }




    public boolean isTouched(int touchX, int touchY) {
        int dx = touchX - (getShapeX() + (int) radius);
        int dy = touchY - (getShapeY() + (int) radius);
        return dx * dx + dy * dy <= radius * radius;
    }
    int getRadius(){
        return radius;
    }
    int getIndex(){
        return index;
    }
    int getDuration(){
        return duration;
    }
    int getTimeUntilNextCommand(){
        return timeUtilNextCommand;
    }
    @Override
    public String toString() {
        return "Circle View["+index+"] ("+layoutParams.x+", "+layoutParams.y+", d)";
    }
}