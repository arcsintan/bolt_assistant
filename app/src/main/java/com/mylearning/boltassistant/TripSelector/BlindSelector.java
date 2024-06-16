package com.mylearning.boltassistant.TripSelector;

import com.mylearning.boltassistant.MyLog;

import java.util.List;

public class BlindSelector implements AbstractSelector{
    String text;
    public BlindSelector(String text) {
        this.text = text;
    }
    @Override
    public boolean selectInput() {
        analyzeText(text);
        return true;
    }

    @Override
    public void analyzeText(String inputText) {
        MyLog.d("BlinedSelector", inputText);

    }

    @Override
    public void analyzeText(List<String> inputText) {

    }

    @Override
    public TripData getTripData() {
        return null;
    }
}



