package com.mylearning.boltassistant.TripSelector;

import android.util.Log;

import com.mylearning.boltassistant.MyLog;
import com.mylearning.boltassistant.RectangleData;

import java.util.List;

public class BoltNormal implements AbstractSelector {
    final String TAG="BoltNormal";
    private String text;
    List<String> importantTextData;
RectangleData rectangleData;
    private int quality;



    boolean selected=false;



    TripData tripData;

    public BoltNormal(String text) {
        this.text = text;
    }

    public BoltNormal(List<String> importantTextData, RectangleData rectangleData) {
        this.rectangleData=rectangleData;

        this.importantTextData=importantTextData;
    }

    @Override
    public boolean selectInput() {
        MyLog.d(TAG, "A request arrived: "+importantTextData.toString());
        Log.d(TAG, "Reference Data is "+rectangleData.toString());

        analyzeText(importantTextData);
        tripData.setSuccess(selected);
        return selected;
    }

    @Override
    public void analyzeText(String inputText) {

    }


    public void analyzeText(List<String> importantTextData) {
        Log.d(TAG, "A trip with 6 important data has been ordered");

        tripData = TripDataParser.parse(importantTextData);
        //Log.d(TAG, tripData.toString());
        //
        if(tripData.getCategory().contains("XL")){
            // XL and other expensive categories are accepted by default
            Log.d(TAG, "an XL request arrived!");
            selected=checkTime() && checkPricePerKmXL();

            //end of XL analysis.
        } else if (tripData.getCategory().contains("Bo")){
            MyLog.d(TAG, "A Bolt trip receipt");
            if(checkPricePerKmBolt()&&checkTime()&& checkPickup()){
                selected= true;
            }
        }else{
            MyLog.d(TAG, "Unknown category");
        }
    }
    public TripData getTripData() {
        return tripData;
    }

    @Override
    public boolean checkDistance() {
        if(tripData.getDistance()>10){
            return false;
        }
        return true;
    }

    @Override
    public boolean checkTime() {
        if(tripData.getPickupDateTime().compareTo(rectangleData.getDate())>=0){
            Log.d(TAG, tripData.getPickupDateTime().toString()+">"+rectangleData.getDate().toString() );
            Log.d(TAG, "Time is okay for this trip");
            return true;
        }else{
            Log.d(TAG, tripData.getPickupDateTime().toString()+"<"+rectangleData.getDate().toString() );
            Log.d(TAG, "Failed due to the time");
            return false;
        }

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
    public boolean checkPricePerKmBolt() {
        return tripData.getNetPricePerKm() >rectangleData.pricePerKm();

    }

    @Override
    public boolean checkPricePerKmXL() {
        return tripData.getNetPricePerKm() > rectangleData.pricePerKm();
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
