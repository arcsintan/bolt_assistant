package com.mylearning.boltassistant;

import android.app.Activity;

public class ActivityManager {
    private static ActivityManager instance;
    private Activity currentActivity;

    private ActivityManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized ActivityManager getInstance() {
        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    public void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }
}
