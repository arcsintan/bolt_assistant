package com.mylearning.boltassistant.TripSelector;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TripDataParser {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM, HH:mm");

    public static TripData parse(List<String> importantTextData) {
        // Extract day
        String day = importantTextData.get(0);

        // Extract price and pickup date-time
        String priceAndPickup = importantTextData.get(1);
        String[] pricePickupParts = priceAndPickup.split(" • Pick-up ");
        float price = Float.parseFloat(pricePickupParts[0].replace("€", "").trim());
        Date pickupDateTime = null;
        try {
            pickupDateTime = DATE_FORMAT.parse(pricePickupParts[1].trim());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Extract category
        String category = importantTextData.get(2);

        // Extract distance
        String distanceString = importantTextData.get(3);
        float distance = Float.parseFloat(distanceString.replace("km", "").trim());

        // Extract addresses
        String addressStart = importantTextData.get(4);
        String addressEnd = importantTextData.get(5);

        return new TripData(day, price, pickupDateTime, category, distance, addressStart, addressEnd);
    }
}
