package com.mylearning.boltassistant.TripSelector;

import android.util.Log;

import com.mylearning.boltassistant.Command;
import com.mylearning.boltassistant.MyAccessibilityService;
import com.mylearning.boltassistant.MyLog;
import com.mylearning.boltassistant.RectangleData;
import com.mylearning.boltassistant.TripSelector.AbstractSelector;
import com.mylearning.boltassistant.TripSelector.BoltNormal;
import com.mylearning.boltassistant.TripSelector.TripData;

import java.util.List;
import java.util.Map;

public class AnalyzeText {

    private static TripData tripData = null;
    final private static String TAG="AnalyzeText";

    public static boolean analyzeTextMap(MyAccessibilityService service, Map<Integer, List<String>> text, RectangleData rectangleData, List<Command> commandList) {
        int tripKey=5;
        if (text.containsKey(tripKey) && text.get(tripKey).size()>3) {
            Log.d(TAG, "key-5=: " + text.get(tripKey));
            AbstractSelector tripSelector = new BoltNormal(text.get(tripKey), rectangleData);
            Boolean res = tripSelector.selectInput();
            tripData = tripSelector.getTripData();
            Log.d(TAG, res ? "Acceptable" : "Rejected");
            return res;
        }else if (text.containsKey(tripKey) && text.get(tripKey).size()==1) {
            Log.d(TAG, "key-5, Accepted trip=,"+text.get(tripKey));
        } else if (text.containsKey(4) && text.get(4).size() != 2 && text.get(4).size()!=3) {
            Log.d(TAG, "trip complete data: ");
            Log.d(TAG, "key-4 =" + text.get(4));
            return true;
        } else if(text.containsKey(2)){
            if (text.get(2).get(0).contains("Err")){
                Log.d(TAG, "Error happened, we should ignore this trip, Command[7]");
                try{
                    synchronized (service.lock){
                    commandList.get(7).execute();
                    Log.d(TAG, "Command[7] done!");
                    service.lock.wait();
                    }
                }
                catch (IndexOutOfBoundsException e){
                    Log.d(TAG, "The code can't handle the error with 8 commands if it fails to accept ");
                } catch (InterruptedException e) {
                    Log.e(TAG, "Analysis text processing interrupted", e);
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
