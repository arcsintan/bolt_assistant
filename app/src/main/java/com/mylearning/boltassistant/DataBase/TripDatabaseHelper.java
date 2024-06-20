package com.mylearning.boltassistant.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TripDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "tripData.db";
    public static final int DATABASE_VERSION = 2; // Incremented the version

    public static final String TABLE_NAME = "trips";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_PICKUP_DATETIME = "pickup_datetime";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_ADDRESS_START = "address_start";
    public static final String COLUMN_ADDRESS_END = "address_end";
    public static final String COLUMN_ORDER_TIME = "order_time";
    public static final String COLUMN_SUCCESS = "success";
    public static final String COLUMN_PLATFORM = "platform";
    public static final String COLUMN_TRIP_TYPE = "trip_type";
    public static final String COLUMN_QUALITY = "quality"; // New column

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DAY + " TEXT, " +
                    COLUMN_PRICE + " REAL, " +
                    COLUMN_PICKUP_DATETIME + " INTEGER, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_DISTANCE + " REAL, " +
                    COLUMN_ADDRESS_START + " TEXT, " +
                    COLUMN_ADDRESS_END + " TEXT, " +
                    COLUMN_ORDER_TIME + " INTEGER, " +
                    COLUMN_SUCCESS + " INTEGER, " +
                    COLUMN_PLATFORM + " INTEGER, " +
                    COLUMN_TRIP_TYPE + " INTEGER, " +
                    COLUMN_QUALITY + " INTEGER);"; // New column

    private static TripDatabaseHelper instance;

    public static synchronized TripDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TripDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private TripDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_QUALITY + " INTEGER DEFAULT 0");
        }
    }
}
