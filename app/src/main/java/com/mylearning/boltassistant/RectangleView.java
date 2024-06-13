package com.mylearning.boltassistant;
import android.graphics.Rect;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

public class RectangleView extends AbstractShapeView {

    private int index;
    private int width, height;
    private final String type=ShapeViewType.RECTANGLE.toString();

    public RectangleView(Context context, int x, int y, int width, int height, int index) {
        super(context, x, y);
        this.index = index;
        this.width = width;
        this.height = height;
        init();
    }
    public RectangleView(Context context, RectangleData rectangleData) {
        super(context, rectangleData.getX(), rectangleData.getY());
        this.index = rectangleData.getIndex();
        this.width = rectangleData.getWidth();
        this.height = rectangleData.getHeight();
        init();
    }

    public RectangleView(Context context,int x, int y,  AttributeSet attrs) {
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
    }

    @Override
    String getType() {
        return type;
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

    @Override
    public void execute(MyAccessibilityService service) {
        Rect rect=new Rect(layoutParams.x, layoutParams.y, layoutParams.x+width, layoutParams.y+height);
        MyLog.d("ReadTextCommand", "called to execute the extract text from All view");
            //service.enqueueCommand(new ReadTextCommand(service, rect));
            service.enqueueCommand(new ReadAllTextCommand(service));
    }

    @Override
    public String toJson() {
        Gson gson = new Gson();
        RectangleData rectangleData = new RectangleData(layoutParams.x, layoutParams.y, width, height,  index, type);
        return gson.toJson(rectangleData);
    }



    @Override
    public String toString(){
        return "Rectangle["+index+"]("+layoutParams.x+", "+layoutParams.y+", "+width+", "+height+")";
    }

    public boolean isTouched(int touchX, int touchY) {
        return touchX >= getShapeX() && touchX <= getShapeX() + width && touchY >= getShapeY() && touchY <= getShapeY() + height;
    }
    public void setShapeHeight(int height) {
        this.height = height;
        View rectangleView = findViewById(R.id.rectangle);
        rectangleView.getLayoutParams().height = height;
        rectangleView.requestLayout();
    }
    public int getIndex() {
        return index;
    }

}
