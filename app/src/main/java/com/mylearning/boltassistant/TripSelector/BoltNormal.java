package com.mylearning.boltassistant.TripSelector;

import android.os.Build;
import android.util.Log;

import com.mylearning.boltassistant.MyLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BoltNormal implements AbstractSelector {
    final String TAG="BoltNormal";
    private String text;
    List<String> importantTextData;

    private int quality;



    boolean selected=false;



    TripData tripData;

    public BoltNormal(String text) {
        this.text = text;
    }

    public BoltNormal(List<String> importantTextData) {

        this.importantTextData=importantTextData;
    }

    @Override
    public boolean selectInput() {
        MyLog.d(TAG, "the trip to be selected is "+importantTextData.toString());
        analyzeText(importantTextData);
        return true;
    }

    @Override
    public void analyzeText(String inputText) {
        MyLog.d(TAG, inputText);


    }

    public void analyzeText(List<String> importantTextData) {
        Log.d(TAG, "A trip with 6 important data has been ordered");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importantTextData.forEach(data -> MyLog.d(TAG, "Data: " + data));
        }
        tripData = TripDataParser.parse(importantTextData);
        Log.d(TAG, tripData.toString());
        if(tripData.getDistance()<8){
            Log.d(TAG, "A trip with distance "+ tripData.getDistance()+" has been ordered");
            setQuality(1);
        }


    }
    public TripData getTripData() {
        return tripData;
    }

    public boolean isSelected() {
        return selected;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

}
