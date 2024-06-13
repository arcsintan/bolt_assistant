
package com.mylearning.boltassistant;

import android.graphics.Rect;

public interface TextReader {
    interface TextReaderCallback {
        void onTextRead(String text);
        void onError(Exception e);
    }

    void readTextFromRectangle(Rect rectangle, TextReaderCallback callback);
}
