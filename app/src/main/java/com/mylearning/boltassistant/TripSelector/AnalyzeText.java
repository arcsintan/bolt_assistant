package com.mylearning.boltassistant.TripSelector;

import android.util.Log;

import com.mylearning.boltassistant.Command;
import com.mylearning.boltassistant.RectangleData;
import com.mylearning.boltassistant.TripSelector.AbstractSelector;
import com.mylearning.boltassistant.TripSelector.BoltNormal;
import com.mylearning.boltassistant.TripSelector.TripData;

import java.util.List;
import java.util.Map;

public class AnalyzeText {
    private static TripData tripData = null;
    final private static String TAG="AnalyzeText";
    public static boolean analyzeTextMap(Map<Integer, List<String>> text, RectangleData rectangleData, List<Command> commandList) {
        if (text.containsKey(6)) {
            Log.d(TAG, "A new trip received: " + text.get(6));
            AbstractSelector tripSelector = new BoltNormal(text.get(6), rectangleData);
            Boolean res = tripSelector.selectInput();
            tripData = tripSelector.getTripData();
            Log.d(TAG, res ? "Acceptable" : "Rejected");
            return res;
        } else if (text.containsKey(4)) {
            if (text.get(4).size()!=2){
                Log.d(TAG, "The next click should be done!:\n"+text);
                return true;
            }else return false;

        } else if(text.containsKey(2)){
            if (text.get(2).get(0).contains("Err")){
                try{
                    commandList.get(7).execute();
                }
                catch (IndexOutOfBoundsException e){
                    Log.d(TAG, "The code can't handle the error with 8 commands if it fails to accept ");
                }
            }
        }

    return false;
    }
    public static TripData getTripData(){
        return tripData;
    }

}