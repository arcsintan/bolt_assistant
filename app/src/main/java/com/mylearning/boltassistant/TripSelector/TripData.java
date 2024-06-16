package com.mylearning.boltassistant.TripSelector;
import java.util.Random;
import java.util.Date;

public class TripData {
    private String day;
    private float price;
    private Date pickupDateTime;
    private String category;
    private float distance;
    private String addressStart;
    private String addressEnd;
    private long timestamp;
    private boolean success;
    private int platform; // 0 or 1
    private int tripType; // 0 or 1
    private int quality;  // New field

    public TripData(String day, float price, Date pickupDateTime, String category, float distance, String addressStart, String addressEnd, int platform, int tripType, int quality) {
        this.day = day;
        this.price = price;
        this.pickupDateTime = pickupDateTime;
        this.category = category;
        this.distance = distance;
        this.addressStart = addressStart;
        this.addressEnd = addressEnd;
        this.timestamp = System.currentTimeMillis();
        Random random=new Random();
        this.success = random.nextBoolean();
        this.platform = platform;
        this.tripType = tripType;
        this.quality = quality;
    }

    // Getters and Setters

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

    public long getTimestamp() {
        return timestamp;
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
                ", timestamp=" + timestamp +
                ", success=" + success +
                ", platform=" + platform +
                ", tripType=" + tripType +
                ", quality=" + quality +
                '}';
    }
}
