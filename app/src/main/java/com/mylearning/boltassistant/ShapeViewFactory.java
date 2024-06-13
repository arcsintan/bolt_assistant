package com.mylearning.boltassistant;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ShapeViewFactory {
    private static final String TAG="Shape View Factory";
    private static Gson gson = new Gson();

    public static AbstractShapeView createShapeView(Context context, String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        switch (type) {
            case "CIRCLE":
                CircleData circleData = gson.fromJson(json, CircleData.class);
                MyLog.d(TAG, circleData.toString());
                return new CircleView(context, circleData);
            case "RECTANGLE":
                RectangleData rectangleData = gson.fromJson(json, RectangleData.class);
                MyLog.d(TAG, rectangleData.toString());
                return new RectangleView(context, rectangleData);
            default:
                throw new IllegalArgumentException("Unknown shape type: " + type);
        }
    }
}
