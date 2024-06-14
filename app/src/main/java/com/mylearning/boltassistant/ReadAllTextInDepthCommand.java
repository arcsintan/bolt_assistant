package com.mylearning.boltassistant;

public class ReadAllTextInDepthCommand implements Command{

    private MyAccessibilityService service;
    public ReadAllTextInDepthCommand(MyAccessibilityService service) {
        this.service = service;
    }

    @Override
    public void execute() {
        service.extractAllTextInDepth(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public int getTimeUntilNextCommand() {
        return 0;
    }

    @Override
    public int getType() {
        return 3;
    }
}
