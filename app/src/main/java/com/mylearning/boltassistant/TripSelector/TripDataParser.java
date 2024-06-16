package com.mylearning.boltassistant.TripSelector;

import android.os.Build;

import com.mylearning.boltassistant.MyLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TripDataParser {
    public static String TAG="TripDataParser";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM, HH:mm");

    public static TripData parse(List<String> importantTextData) {
        MyLog.d(TAG, "Parsing importantTextData: " + importantTextData.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importantTextData.forEach(data -> MyLog.d(TAG, "Data: " + data));
        }

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

        // For demonstration, assume platform and tripType are derived from some logic
        int platform = 1; // Or some logic to determine platform
        int tripType = 1; // Or some logic to determine trip type
        int quality=4;

        return new TripData(day, price, pickupDateTime, category, distance, addressStart, addressEnd, platform, tripType, quality);
    }
}
