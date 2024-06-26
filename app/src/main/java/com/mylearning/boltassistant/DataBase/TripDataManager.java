package com.mylearning.boltassistant.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.mylearning.boltassistant.TripSelector.TripData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripDataManager {
    private TripDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private Context context; // Store the context

    public TripDataManager(Context context) {
        this.context = context.getApplicationContext(); // Save the application context
        dbHelper = TripDatabaseHelper.getInstance(this.context);
        database = dbHelper.getWritableDatabase();
    }

    public long insertTripData(TripData tripData) {
        ContentValues values = new ContentValues();
        values.put(TripDatabaseHelper.COLUMN_DAY, tripData.getDay());
        values.put(TripDatabaseHelper.COLUMN_PRICE, tripData.getPrice());
        values.put(TripDatabaseHelper.COLUMN_PICKUP_DATETIME, tripData.getPickupDateTime().getTime());
        values.put(TripDatabaseHelper.COLUMN_CATEGORY, tripData.getCategory());
        values.put(TripDatabaseHelper.COLUMN_DISTANCE, tripData.getDistance());
        values.put(TripDatabaseHelper.COLUMN_ADDRESS_START, tripData.getAddressStart());
        values.put(TripDatabaseHelper.COLUMN_ADDRESS_END, tripData.getAddressEnd());
        values.put(TripDatabaseHelper.COLUMN_ORDER_TIME, tripData.getOrderTime().getTime());
        values.put(TripDatabaseHelper.COLUMN_SUCCESS, tripData.isSuccess() ? 1 : 0);
        values.put(TripDatabaseHelper.COLUMN_PLATFORM, tripData.getPlatform());
        values.put(TripDatabaseHelper.COLUMN_TRIP_TYPE, tripData.getTripType());
        values.put(TripDatabaseHelper.COLUMN_QUALITY, tripData.getQuality()); // New column

        return database.insert(TripDatabaseHelper.TABLE_NAME, null, values);
    }

    public List<TripData> getAllTripData() {
        List<TripData> tripDataList = new ArrayList<>();
        String orderBy ="" ;//"TripDatabaseHelper.COLUMN_PICKUP_DATETIME + " ASC";
        Cursor cursor = database.query(TripDatabaseHelper.TABLE_NAME, null, null, null, null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                long id = getLongFromCursor(cursor, TripDatabaseHelper.COLUMN_ID); // Retrieve the ID
                String day = getStringFromCursor(cursor, TripDatabaseHelper.COLUMN_DAY);
                float price = getFloatFromCursor(cursor, TripDatabaseHelper.COLUMN_PRICE);
                long pickupDateTimeMillis = getLongFromCursor(cursor, TripDatabaseHelper.COLUMN_PICKUP_DATETIME);
                String category = getStringFromCursor(cursor, TripDatabaseHelper.COLUMN_CATEGORY);
                float distance = getFloatFromCursor(cursor, TripDatabaseHelper.COLUMN_DISTANCE);
                String addressStart = getStringFromCursor(cursor, TripDatabaseHelper.COLUMN_ADDRESS_START);
                String addressEnd = getStringFromCursor(cursor, TripDatabaseHelper.COLUMN_ADDRESS_END);
                long orderTimeMillis = getLongFromCursor(cursor, TripDatabaseHelper.COLUMN_ORDER_TIME);
                boolean success = getIntFromCursor(cursor, TripDatabaseHelper.COLUMN_SUCCESS) == 1;
                int platform = getIntFromCursor(cursor, TripDatabaseHelper.COLUMN_PLATFORM);
                int tripType = getIntFromCursor(cursor, TripDatabaseHelper.COLUMN_TRIP_TYPE);
                int quality = getIntFromCursor(cursor, TripDatabaseHelper.COLUMN_QUALITY); // New column

                Date pickupDateTime = new Date(pickupDateTimeMillis);
                Date orderTime = new Date(orderTimeMillis);
                TripData tripData = new TripData(id, day, price, pickupDateTime, orderTime, category, distance, addressStart, addressEnd, platform, tripType, quality, success);
                tripDataList.add(tripData);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return tripDataList;
    }

    public List<TripData> getTripsBetweenDates(Date startDate, Date endDate) {
        List<TripData> tripDataList = new ArrayList<>();
        String selection = TripDatabaseHelper.COLUMN_PICKUP_DATETIME + " BETWEEN ? AND ?";
        String[] selectionArgs = {String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime())};
        String orderBy = TripDatabaseHelper.COLUMN_PICKUP_DATETIME + " ASC";

        Cursor cursor = database.query(TripDatabaseHelper.TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                long id = getLongFromCursor(cursor, TripDatabaseHelper.COLUMN_ID); // Retrieve the ID
                String day = getStringFromCursor(cursor, TripDatabaseHelper.COLUMN_DAY);
                float price = getFloatFromCursor(cursor, TripDatabaseHelper.COLUMN_PRICE);
                long pickupDateTimeMillis = getLongFromCursor(cursor, TripDatabaseHelper.COLUMN_PICKUP_DATETIME);
                String category = getStringFromCursor(cursor, TripDatabaseHelper.COLUMN_CATEGORY);
                float distance = getFloatFromCursor(cursor, TripDatabaseHelper.COLUMN_DISTANCE);
                String addressStart = getStringFromCursor(cursor, TripDatabaseHelper.COLUMN_ADDRESS_START);
                String addressEnd = getStringFromCursor(cursor, TripDatabaseHelper.COLUMN_ADDRESS_END);
                long orderTimeMillis = getLongFromCursor(cursor, TripDatabaseHelper.COLUMN_ORDER_TIME);
                boolean success = getIntFromCursor(cursor, TripDatabaseHelper.COLUMN_SUCCESS) == 1;
                int platform = getIntFromCursor(cursor, TripDatabaseHelper.COLUMN_PLATFORM);
                int tripType = getIntFromCursor(cursor, TripDatabaseHelper.COLUMN_TRIP_TYPE);
                int quality = getIntFromCursor(cursor, TripDatabaseHelper.COLUMN_QUALITY); // New column

                Date pickupDateTime = new Date(pickupDateTimeMillis);
                Date orderTime = new Date(orderTimeMillis);
                TripData tripData = new TripData(id, day, price, pickupDateTime, orderTime, category, distance, addressStart, addressEnd, platform, tripType, quality, success);
                tripDataList.add(tripData);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return tripDataList;
    }


    private String getStringFromCursor(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
        return cursor.getString(columnIndex);
    }

    private float getFloatFromCursor(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
        return cursor.getFloat(columnIndex);
    }

    private long getLongFromCursor(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
        return cursor.getLong(columnIndex);
    }

    private int getIntFromCursor(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
        return cursor.getInt(columnIndex);
    }

    public void close() {
        dbHelper.close();
    }

    public boolean backupDatabase() {
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "TripDataBackup");
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                Log.e("TripDataManager", "Failed to create backup directory");
                return false;
            }
        }

        String backupFileName = "tripDataBackup_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".db";
        File currentDB = context.getDatabasePath(TripDatabaseHelper.DATABASE_NAME);
        File backupDB = new File(backupDir, backupFileName);

        try (FileChannel src = new FileInputStream(currentDB).getChannel();
             FileChannel dst = new FileOutputStream(backupDB).getChannel()) {
            dst.transferFrom(src, 0, src.size());
            Log.d("TripDataManager", "Backup successful to " + backupDB.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e("TripDataManager", "Backup failed", e);
            return false;
        }
    }

    public long deleteTripData(long id) {
        return database.delete(TripDatabaseHelper.TABLE_NAME, TripDatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public boolean restoreDatabase(String backupFileName) {
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "TripDataBackup");
        File backupDB = new File(backupDir, backupFileName);
        File currentDB = context.getDatabasePath(TripDatabaseHelper.DATABASE_NAME);

        try (FileChannel src = new FileInputStream(backupDB).getChannel();
             FileChannel dst = new FileOutputStream(currentDB).getChannel()) {
            dst.transferFrom(src, 0, src.size());
            Log.d("TripDataManager", "Restore successful from " + backupDB.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e("TripDataManager", "Restore failed", e);
            return false;
        }
    }

    public void deleteAllTripData() {
        database.delete(TripDatabaseHelper.TABLE_NAME, null, null);
    }
}
