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
        int tripKey=5;
        if (text.containsKey(tripKey)) {
            Log.d(TAG, "A new trip received: " + text.get(tripKey));
            AbstractSelector tripSelector = new BoltNormal(text.get(tripKey), rectangleData);
            Boolean res = tripSelector.selectInput();
            tripData = tripSelector.getTripData();
            Log.d(TAG, res ? "Acceptable" : "Rejected");
            return res;
        } else if (text.containsKey(4)) {
            Log.d(TAG, "key-4 ="+text.get(4));
            if (text.get(4).size()!=2){
                Log.d(TAG, "The next click should be done!:\n");
                return true;
            }else return false;

        } else if(text.containsKey(2)){
            Log.d(TAG, "key-2 ="+text.get(2));
            if (text.get(2).get(0).contains("Err")){
                try{
                    commandList.get(7).execute();
                }
                catch (IndexOutOfBoundsException e){
                    Log.d(TAG, "The code can't handle the error with 8 commands if it fails to accept ");
                }
            }
        }else{
            Log.d(TAG, text.toString());
        }

    return false;
    }
    public static TripData getTripData(){
        return tripData;
    }

}
