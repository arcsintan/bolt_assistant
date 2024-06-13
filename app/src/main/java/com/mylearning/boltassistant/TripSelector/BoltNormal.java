package com.mylearning.boltassistant.TripSelector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class BoltNormal implements AbstractSelector {
    private String tripString;
    public BoltNormal(String tripString) {
        this.tripString = tripString;
    }

    @Override
    public boolean selectTrip() {
        return true;
    }

    @Override
    public void analyzeTrip(String tripData) {


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
