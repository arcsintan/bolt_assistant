package com.mylearning.boltassistant;

import android.graphics.Rect;

public class ReadTextCommand implements Command {
    private MyAccessibilityService service;
    private Rect targetRect;
    private boolean result;

    public ReadTextCommand(MyAccessibilityService service, Rect targetRect) {
        this.service = service;
        this.targetRect = targetRect;
    }

    @Override
    public void execute() {
        service.extractTextFromRect(targetRect, new Runnable() {
                    @Override
                    public void run() {

                    }
                }
        );
    }

    @Override
    public int getTimeUntilNextCommand() {
        return 0;
    }

    @Override
    public int getType() {
        return 1;
    }

    public boolean getResult() {
        return result;
    }
}