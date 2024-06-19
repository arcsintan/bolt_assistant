package com.mylearning.boltassistant;

class SimulateTouchCommand implements Command {
    private MyAccessibilityService service;
    private float x, y;
    private int duration;
    private int timeUntilNextCommand;

    public SimulateTouchCommand(MyAccessibilityService service, float x, float y, int duration, int timeUntilNextCommand) {
        this.service = service;
        this.x = x;
        this.y = y;
        this.duration = duration;
        this.timeUntilNextCommand=timeUntilNextCommand;

    }

    @Override
    public void execute() {
        service.simulateTouch(x, y, duration,timeUntilNextCommand, new Runnable() {
            @Override
            public void run() {
                // No need to call processNextCommand here, it's handled by BlockingQueue
            }
        });
    }

    @Override
    public int getTimeUntilNextCommand() {
        return timeUntilNextCommand;
    }

    @Override
    public int getTypeTag() {
        return 0;
    }
}
