package com.mylearning.boltassistant;

public class ReadAllTextCommand implements Command {
    private MyAccessibilityService service;
    private boolean result;

    public ReadAllTextCommand(MyAccessibilityService service) {

        this.service = service;
    }

    @Override
    public void execute() {
        service.extractAllText(new Runnable() {
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
        return 2;
    }
}