package com.mylearning.boltassistant;

public  class RectangleData {
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    private int x;
    private int y;
    private int width;
    private int height;

    private int index;
    private String type;

    public RectangleData(int x, int y, int width, int height, int index, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.width = height;
        this.index = index;
        this.type=type;
    }
    @Override
    public String toString(){
        return "Rectangle["+index+"]("+x+", "+y+", "+width+", "+height+")";
    }
}