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
        service.extractTextFromRect(targetRect, () -> {
            String extractedText = service.getFullText();
            result = service.analyzeExtractedText(extractedText);
        });
    }

    public boolean getResult() {
        return result;
    }
}