package com.mylearning.boltassistant.TripSelector;

import java.util.Date;

public class TripData  {
    private String day;
    private float price;
    private Date pickupDateTime;
    private String category;
    private float distance;
    private String addressStart;
    private String addressEnd;
    private Date orderTime; // Change the type to Date
    private boolean success;
    private int platform; // 0 or 1
    private int tripType; // 0 or 1
    private int quality;  // New field
    private long id; // Add this field

    // Updated constructor
    public TripData(String day, float price, Date pickupDateTime, String category, float distance, String addressStart, String addressEnd, int platform, int tripType, int quality) {
        this.day = day;
        this.price = price;
        this.pickupDateTime = pickupDateTime;
        this.category = category;
        this.distance = distance;
        this.addressStart = addressStart;
        this.addressEnd = addressEnd;
        this.orderTime = new Date(); // Initialize to current date and time

        this.success = false;
        this.platform = platform;
        this.tripType = tripType;
        this.quality = quality;
    }

    // Updated constructor
    public TripData(long id, String day, float price, Date pickupDateTime,Date orderTime,  String category, float distance, String addressStart, String addressEnd, int platform, int tripType, int quality, boolean success) {
        this.day = day;
        this.price = price;
        this.pickupDateTime = pickupDateTime;
        this.category = category;
        this.distance = distance;
        this.addressStart = addressStart;
        this.addressEnd = addressEnd;
        this.orderTime = orderTime;// Initialize to current date and time
        this.success = success;
        this.platform = platform;
        this.tripType = tripType;
        this.quality = quality;
        this.id = id;
    }

    // Default constructor
    public TripData(){
        this.day = "";
        this.price = 0;
        this.pickupDateTime = new Date();
        this.category = "";
        this.distance = 0;
        this.addressStart = "";
        this.addressEnd = "";
        this.orderTime = new Date(); // Initialize to current date and time

        this.success = false;
        this.platform = 2;
        this.tripType = 2;
        this.quality = 4;
    }

    // Getter and setter for timestamp
    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    // Getters and Setters for other fields
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDay() {
        return day;
    }

    public float getPrice() {
        return price;
    }

    public Date getPickupDateTime() {
        return pickupDateTime;
    }

    public String getCategory() {
        return category;
    }

    public float getDistance() {
        return distance;
    }

    public String getAddressStart() {
        return addressStart;
    }

    public String getAddressEnd() {
        return addressEnd;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getPlatform() {
        return platform;
    }

    public int getTripType() {
        return tripType;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    @Override
    public String toString() {
        return "TripData{" +
                "day='" + day + '\'' +
                ", price=" + price +
                ", pickupDateTime=" + pickupDateTime +
                ", category='" + category + '\'' +
                ", distance=" + distance +
                ", addressStart='" + addressStart + '\'' +
                ", addressEnd='" + addressEnd + '\'' +
                ", orderTime=" + orderTime +
                ", success=" + success +
                ", platform=" + platform +
                ", tripType=" + tripType +
                ", quality=" + quality +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof TripData)) {
            return false;
        }

        TripData anotherTripData = (TripData) obj;
        return this.distance == anotherTripData.distance && this.price == anotherTripData.price && this.price == anotherTripData.price;
    }
}
