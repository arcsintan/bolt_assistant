package com.mylearning.boltassistant;

import androidx.annotation.NonNull;

public  class CircleData {
    private int x;
    private int y;
    private int radius;
    private int duration;

    private int timeUntilNextCommand;

    private int index;
    private String type;
    public CircleData(int x, int y, int radius, int duration, int timeUntilNextCommand, int index, String type) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.duration = duration;
        this.timeUntilNextCommand = timeUntilNextCommand;
        this.index = index;
        this.type=type;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public int getDuration() {
        return duration;
    }

    public int getTimeUntilNextCommand() {
        return timeUntilNextCommand;
    }

    public int getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }


    @Override
    public String toString() {
        return "Circle["+index+"](x="+x+", y="+y+",R="+radius+", d="+duration+", n_c="+timeUntilNextCommand+")";
    }
}
