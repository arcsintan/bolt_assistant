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

    public BoltNormal(String text) {
        this.text = text;
    }

    public BoltNormal(List<String> importantTextData) {
        this.importantTextData=importantTextData;
    }

    @Override
    public boolean selectInput() {
        analyzeText(text);
        return true;
    }

    @Override
    public void analyzeText(String inputText) {
        MyLog.d(TAG, inputText);

    }
    public void analyzeText(List<String> importantTextData) {

            Log.d(TAG, "A trip with 6 important data has been ordered");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importantTextData.forEach(System.out::println);
        }


    }

}
