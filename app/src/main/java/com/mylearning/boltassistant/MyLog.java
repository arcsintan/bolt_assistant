package com.mylearning.boltassistant;
import android.util.Log;
public class MyLog {

    public static void d(String TAG, String message) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        // Index 3 corresponds to the caller of this method
        if (stackTraceElements.length > 3) {
            StackTraceElement element = stackTraceElements[3];
            String fullClassName = element.getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            String methodName = element.getMethodName();
            int lineNumber = element.getLineNumber();
            Log.d(TAG, className + "." + methodName + " (Line " + lineNumber + "): " + message);
        } else {
            Log.d(TAG, message); // Fallback if stack trace is not available
        }
    }
}
