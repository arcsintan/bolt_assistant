package com.mylearning.boltassistant;

import android.util.Log;

public class ReadAllTextInAllDepthCommand implements Command {
    private final String TAG="ReadAllTextInAllDepthCommand";
    private final int typeTag=4;
    private MyAccessibilityService service;
    private RectangleData rectangleData;


    public ReadAllTextInAllDepthCommand(MyAccessibilityService service, RectangleData rectangleData) {
        this.rectangleData = rectangleData;
        this.service = service;
    }

    public ReadAllTextInAllDepthCommand(MyAccessibilityService service) {
        this.service = service;
    }
    @Override
    public void execute() {
        service.extractAllTextInAllDepth(new Runnable() {
            @Override
            public void run() {
                synchronized (service.lock) {
                    service.lock.notify(); // Notify the waiting thread that the operation is complete
                }
            }
        });
    }

    @Override
    public int getTimeUntilNextCommand() {
        return 0;
    }

    @Override
    public int getTypeTag() {
        return typeTag;
    }

    @Override
    public CircleData getCircleData() {
        return null;
    }

    @Override
    public RectangleData getRectangleData() {
        return rectangleData;
    }
}
