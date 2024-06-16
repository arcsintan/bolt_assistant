package com.mylearning.boltassistant.TripSelector;

import java.util.List;

public interface AbstractSelector {
    boolean selectInput();
    void analyzeText(String inputText);
    void analyzeText(List<String> inputText);
    public TripData getTripData();
}
