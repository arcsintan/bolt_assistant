package com.mylearning.boltassistant.TripSelector;

import java.util.List;

public interface AbstractSelector {
    boolean selectInput();
    void analyzeText(String inputText);
    void analyzeText(List<String> inputText);
    public TripData getTripData();
    public boolean checkDistance();
    public boolean checkTime();
    public boolean checkDestination();
    public boolean checkPickup();
    public boolean checkPricePerKmBolt();
    public boolean checkPricePerKmXL();
}
