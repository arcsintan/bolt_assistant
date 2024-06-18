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
        MyLog.d(TAG, "A request arrived: "+importantTextData.toString());
        analyzeText(importantTextData);
        return selected;
    }

    @Override
    public void analyzeText(String inputText) {

    }


    public void analyzeText(List<String> importantTextData) {
        Log.d(TAG, "A trip with 6 important data has been ordered");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            importantTextData.forEach(data -> MyLog.d(TAG, "Data: " + data));
//        }
        tripData = TripDataParser.parse(importantTextData);
        //Log.d(TAG, tripData.toString());
        if(tripData.getCategory().contains("XL")){
            //Log.d(TAG, "an XL request arrived!");

            selected=true;
        } else if (tripData.getCategory().contains("Bo")) {
            //Log.d(TAG, "A Bolt trip receipt");
            if(checkDistance()&&checkTime() && checkPrice() && checkTime() && checkPickup()){
                selected= true;
            }
        }
    }
    public TripData getTripData() {
        return tripData;
    }

    @Override
    public boolean checkDistance() {
        if(tripData.getDistance()>8){
            quality=2;
            return false;
        }
        return true;
    }

    @Override
    public boolean checkTime() {
        return true;
    }

    @Override
    public boolean checkDestination() {
        return true;
    }

    @Override
    public boolean checkPickup() {
        return true;
    }

    @Override
    public boolean checkPrice() {
        return true;
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
