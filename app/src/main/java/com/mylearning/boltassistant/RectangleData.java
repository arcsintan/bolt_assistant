package com.mylearning.boltassistant;

import java.util.Date;

public class RectangleData {
    private int x;
    private int y;
    private int width;
    private int height;
    private int index;
    private String type;
    private Date date; // Date object
    private Date time; // Time object
    private String category;
    private float km;
    private float price;
    private String pickup;
    private String dropoff;

    // Existing constructor
    public RectangleData(int x, int y, int width, int height, int index, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.index = index;
        this.type = type;
    }

    // New constructor with additional fields
    public RectangleData(int x, int y, int width, int height, int index, String type, Date date, Date time, String category, float km, float price, String pickup, String dropoff) {
        this(x, y, width, height, index, type);
        this.date = date;
        this.time = time;
        this.category = category;
        this.km = km;
        this.price = price;
        this.pickup = pickup;
        this.dropoff = dropoff;
    }

    // Constructor with default values for optional parameters
    public RectangleData(int x, int y, int width, int height, int index) {
        this(x, y, width, height, index, "rectangle", new Date(), new Date(), "bolt", 0.0f, 0.0f, "", "");
    }

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

    public Date getDate() {
        return date;
    }

    public Date getTime() {
        return time;
    }

    public String getCategory() {
        return category;
    }

    public float getKm() {
        return km;
    }

    public float getPrice() {
        return price;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDropoff() {
        return dropoff;
    }

    @Override
    public String toString(){
        return "Rectangle["+index+"]("+x+", "+y+", "+width+", "+height+", "+date+", "+time+", "+category+", "+km+", "+price+", "+pickup+", "+dropoff+")";
    }
}
