package com.mylearning.boltassistant;
public class ReadAllTextAtDepth implements Command{
    private MyAccessibilityService service;
    private RectangleData rectangleData;

    private final int typeTag=3;
    public ReadAllTextAtDepth(MyAccessibilityService service, RectangleView rectangleView) {
        this.service = service;
        this.rectangleData=rectangleView.createRectangleData();
    }
    public ReadAllTextAtDepth(MyAccessibilityService service) {
        this.service = service;
    }


    @Override

    public void execute() {
        service.extractAllTextInDepth(new Runnable() {
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
        return this.rectangleData;
    }


}
