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
    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");

    public static TripData parse(List<String> importantTextData) {
        MyLog.d(TAG, "Parsing importantTextData: " + importantTextData.toString());
            importantTextData.forEach(data -> MyLog.d(TAG, "Data: " + data));
        if (importantTextData.size() < 5) {
            Log.e(TAG, "importantTextData does not contain enough elements");
            return null; // or throw an exception
        }

        // Extract price and pickup date-time from the second element
        String priceAndPickup = importantTextData.get(1);
        String[] pricePickupParts = priceAndPickup.split(" • Pick-up ");
        float price = Float.parseFloat(pricePickupParts[0].replace("€", "").trim());
        Date pickupDateTime = parsePickupDate(pricePickupParts[1]);

        // Extract day from the first element, ensure the correct format
        String day = importantTextData.get(0);
        if ("Today".equals(day)) {
            day = DAY_FORMAT.format(new Date());
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
        int quality = 4;

        return new TripData(day, price, pickupDateTime, category, distance, addressStart, addressEnd, platform, tripType, quality);
    }

    private static Date parsePickupDate(String pickupDateStr) {
        try {
            Calendar calendar = Calendar.getInstance();
            int currentYear = Integer.parseInt(YEAR_FORMAT.format(calendar.getTime()));

            if (pickupDateStr.contains("Today")) {
                String timeStr = pickupDateStr.split(",")[1].trim();
                String todayDateStr = new SimpleDateFormat("dd MMM").format(calendar.getTime()) + ", " + timeStr;
                Date parsedDate = DATE_FORMAT.parse(todayDateStr);

                calendar.setTime(parsedDate);
                calendar.set(Calendar.YEAR, currentYear); // Set to current year
            } else {
                Date parsedDate = DATE_FORMAT.parse(pickupDateStr.trim());

                calendar.setTime(parsedDate);
                calendar.set(Calendar.YEAR, currentYear); // Set to current year
            }

            return calendar.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse pickup date: " + pickupDateStr, e);
            return null; // or handle the error as needed
        }
    }
}
