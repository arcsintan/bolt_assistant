package com.mylearning.boltassistant;

public class ReadAllTextCommand implements Command {
    private MyAccessibilityService service;
    private boolean result;

    public ReadAllTextCommand(MyAccessibilityService service) {
        this.service = service;
    }

    @Override
    public void execute() {
        service.extractAllText(() -> {
            String extractedText = service.getFullText();
            result = service.analyzeExtractedText(extractedText);
        });
    }

    public boolean getResult() {
        return result;
    }
}