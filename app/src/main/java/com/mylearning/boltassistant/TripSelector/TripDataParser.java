package com.mylearning.boltassistant.TripSelector;

import android.os.Build;
import android.util.Log;

import com.mylearning.boltassistant.MyLog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TripDataParser {
    public static final String TAG = "TripDataParser";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM, HH:mm");
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("EEE, dd MMM");

    public static TripData parse(List<String> importantTextData) {
        MyLog.d(TAG, "Parsing importantTextData: " + importantTextData.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importantTextData.forEach(data -> MyLog.d(TAG, "Data: " + data));
        }

        if (importantTextData.size() < 6) {
            Log.e(TAG, "importantTextData does not contain enough elements");
            return null; // or throw an exception
        }

        // Extract day
        String day = importantTextData.get(0);
        day = handleSpecialDayCases(day);

        // Extract price and pickup date-time
        String priceAndPickup = importantTextData.get(1);
        String[] pricePickupParts = priceAndPickup.split(" • Pick-up ");
        float price = Float.parseFloat(pricePickupParts[0].replace("€", "").trim());
        Date pickupDateTime = parsePickupDate(pricePickupParts[1]);

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
        int quality = 4;

        return new TripData(day, price, pickupDateTime, category, distance, addressStart, addressEnd, platform, tripType, quality);
    }

    private static String handleSpecialDayCases(String day) {
        if (day != null && !day.isEmpty()) {
            char firstChar = day.charAt(0);
            Calendar calendar = Calendar.getInstance();

            switch (firstChar) {
                case 'T':
                    // Assume it's "Today"
                    if (day.equalsIgnoreCase("Today")) {
                        return DAY_FORMAT.format(calendar.getTime());
                    }
                    break;
                case 'I':
                    // Assume it's "In next 30 minutes"
                    if (day.equalsIgnoreCase("In next 30 minutes")) {
                        calendar.add(Calendar.MINUTE, 30);
                        return DAY_FORMAT.format(calendar.getTime());
                    }
                    break;
                default:
                    // Regular case, return the day as it is
                    return day;
            }
        }
        return day; // Return the original day if the string is null or empty
    }

    private static Date parsePickupDate(String pickupDateStr) {
        try {
            return DATE_FORMAT.parse(pickupDateStr.trim());
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse pickup date: " + pickupDateStr, e);
            return null; // or handle the error as needed
        }
    }
}
