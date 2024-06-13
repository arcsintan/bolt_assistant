package com.mylearning.boltassistant;

import androidx.annotation.NonNull;

public class CoordinateSystem {

    private float x;
    private float y;
    int duration;
    int type;

    public CoordinateSystem(float x, float y, int type, int duration) {
        this.x = x;
        this.y = y;
        this.type=type;
        this.duration=duration;
    }

    @NonNull
    @Override
    public String toString() {
        return  "x="+x+", y="+y+", duration="+duration;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
