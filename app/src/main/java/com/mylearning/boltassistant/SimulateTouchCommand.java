package com.mylearning.boltassistant;

class SimulateTouchCommand implements Command {
    final String TAG="SimulateTouchCommand";

    private MyAccessibilityService service;
    private float x, y;
    private int duration;
    private int timeUntilNextCommand;
    private int radius;
    public CircleData circleData;
    public RectangleData rectangleData;
    //layoutParams.x + radius, layoutParams.y + radius + 35, duration, timeUntilNextCommand)
    public SimulateTouchCommand(MyAccessibilityService service, CircleView circleView) {
        this.service = service;
        this.circleData=circleView.createCircleData();
        x=circleData.getX()+circleData.getRadius();
        y=circleData.getY()+circleData.getRadius()+35;
        radius=circleData.getRadius();
        duration=circleData.getDuration();
        timeUntilNextCommand=circleData.getTimeUntilNextCommand();
    }


    @Override
    public void execute() {
        service.simulateTouch(x, y, duration);
    }

    @Override
    public int getTimeUntilNextCommand() {
        return timeUntilNextCommand;
    }

    @Override
    public int getTypeTag() {
        return 0;
    }

    @Override
    public CircleData getCircleData() {
        return this.circleData;
    }

    @Override
    public RectangleData getRectangleData() {
        return new RectangleData();
    }


}
