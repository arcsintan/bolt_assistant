package com.mylearning.boltassistant.TripSelector;

import com.mylearning.boltassistant.MyLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class BoltNormal implements AbstractSelector {
    final String TAG="BoltNormal";
    private String text;

    public BoltNormal(String text) {
        this.text = text;
    }

    @Override
    public boolean selectInput() {
        analyzeText(text);
        return true;
    }

    @Override
    public void analyzeText(String inputText) {
        MyLog.d(TAG, inputText);

    }

    public static class TripData {
        int km;
        LocalDateTime now;

        DateTimeFormatter reservationTime;
        String pickup;
        String dropoff;
        String category;

        public TripData(int km, LocalDateTime now, DateTimeFormatter reservationTime, String pickup, String dropoff, String category) {
            this.km = km;
            this.now = now;
            this.reservationTime = reservationTime;
            this.pickup = pickup;
            this.dropoff = dropoff;
            this.category = category;
        }


        public int getKm() {
            return km;
        }

        public LocalDateTime getNow() {
            return now;
        }

        public DateTimeFormatter getReservationTime() {
            return reservationTime;
        }

        public String getPickup() {
            return pickup;
        }

        public String getDropoff() {
            return dropoff;
        }

        public String getCategory() {
            return category;
        }
    }
}
