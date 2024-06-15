package com.mylearning.boltassistant.TripSelector;
import java.util.Date;

public class TripData {
    private String day;
    private float price;
    private Date pickupDateTime;
    private String category;
    private float distance;
    private String addressStart;
    private String addressEnd;

    public TripData(String day, float price, Date pickupDateTime, String category, float distance, String addressStart, String addressEnd) {
        this.day = day;
        this.price = price;
        this.pickupDateTime = pickupDateTime;
        this.category = category;
        this.distance = distance;
        this.addressStart = addressStart;
        this.addressEnd = addressEnd;
    }

    // Getters for all fields

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
                '}';
    }
}
