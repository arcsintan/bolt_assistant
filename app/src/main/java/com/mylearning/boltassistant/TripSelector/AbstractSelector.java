package com.mylearning.boltassistant.TripSelector;

public interface AbstractSelector {
    boolean selectInput();
    void analyzeText(String inputText);
}
