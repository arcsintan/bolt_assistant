package com.mylearning.boltassistant;

import android.graphics.Rect;

public class ReadTextCommand implements Command {
    private MyAccessibilityService service;
    private Rect targetRect;
    private boolean result;
    private final int typeTag=1;
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
    public int getTypeTag() {
        return typeTag;
    }

    public boolean getResult() {
        return result;
    }
}