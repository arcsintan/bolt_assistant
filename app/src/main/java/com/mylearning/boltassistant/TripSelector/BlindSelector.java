package com.mylearning.boltassistant.TripSelector;

import com.mylearning.boltassistant.MyLog;

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
}



