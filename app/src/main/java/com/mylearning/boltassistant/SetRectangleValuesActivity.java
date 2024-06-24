package com.mylearning.boltassistant;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SetRectangleValuesActivity extends Activity {
    private final String TAG = "RectangleActivity";
    public static final String EXTRA_DATE = "com.mylearning.boltassistant.DATE";
    public static final String EXTRA_CATEGORY = "com.mylearning.boltassistant.CATEGORY";
    public static final String EXTRA_KM = "com.mylearning.boltassistant.KM";
    public static final String EXTRA_PRICE = "com.mylearning.boltassistant.PRICE";
    public static final String EXTRA_PICKUP = "com.mylearning.boltassistant.PICKUP";
    public static final String EXTRA_DROPOFF = "com.mylearning.boltassistant.DROPOFF";
    public static final String EXTRA_WIDTH = "com.mylearning.boltassistant.WIDTH";
    public static final String EXTRA_HEIGHT = "com.mylearning.boltassistant.HEIGHT";
    public static final String ACTION_UPDATE_VALUES = "com.mylearning.boltassistant.ACTION_UPDATE_VALUES";
    public static final String ACTION_ID = "com.mylearning.boltassistant.ACTION_ID";
    public static int action_index;
    public static boolean is_running = false;

    private EditText dateEditText;
    private EditText timeEditText;
    private Spinner categorySpinner;
    private EditText kmEditText;
    private EditText priceEditText;
    private EditText pricePerKmEditText;
    private EditText pickupEditText;
    private EditText dropoffEditText;
    private EditText widthEditText;
    private EditText heightEditText;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_rectangle_prop_menu);

        dateEditText = findViewById(R.id.date_edit_text);
        timeEditText = findViewById(R.id.time_edit_text);
        categorySpinner = findViewById(R.id.category_spinner);
        kmEditText = findViewById(R.id.km_edit_text);
        priceEditText = findViewById(R.id.price_edit_text);
        pricePerKmEditText = findViewById(R.id.price_per_km_edit_text);
        pickupEditText = findViewById(R.id.pickup_edit_text);
        dropoffEditText = findViewById(R.id.dropoff_edit_text);
        widthEditText = findViewById(R.id.width_edit_text);
        heightEditText = findViewById(R.id.height_edit_text);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        calendar = Calendar.getInstance();

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SetRectangleValuesActivity.this, dateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(SetRectangleValuesActivity.this, timeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });

        // Set default values
        Intent intent = getIntent();
        if (intent != null) {
            Log.d(TAG, " An intent received to show setting for rectangle");
            long dateMillis = intent.getLongExtra(EXTRA_DATE, new Date().getTime());
            String category = intent.getStringExtra(EXTRA_CATEGORY);
            float km = intent.getFloatExtra(EXTRA_KM, 0.0f);
            float price = intent.getFloatExtra(EXTRA_PRICE, 0.0f);
            String pickup = intent.getStringExtra(EXTRA_PICKUP);
            String dropoff = intent.getStringExtra(EXTRA_DROPOFF);
            int width = intent.getIntExtra(EXTRA_WIDTH, 100);
            int height = intent.getIntExtra(EXTRA_HEIGHT, 50);
            action_index = intent.getIntExtra(ACTION_ID, 1);

            Date combinedDateTime = new Date(dateMillis);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            dateEditText.setText(dateFormat.format(combinedDateTime));
            timeEditText.setText(timeFormat.format(combinedDateTime));
            if (category != null) {
                int spinnerPosition = adapter.getPosition(category);
                categorySpinner.setSelection(spinnerPosition);
            }
            kmEditText.setText(String.valueOf(km));
            priceEditText.setText(String.valueOf(price));
            pickupEditText.setText(pickup);
            dropoffEditText.setText(dropoff);
            widthEditText.setText(String.valueOf(width));
            heightEditText.setText(String.valueOf(height));
        }

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " A broadcast for Updating the Rectangle Data is prepared");
                sendUpdateBroadcast();
                Toast.makeText(SetRectangleValuesActivity.this, "Values updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        is_running = true;
    }

    private void sendUpdateBroadcast() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date datePart = dateFormat.parse(dateEditText.getText().toString());
            Date timePart = timeFormat.parse(timeEditText.getText().toString());

            // Combine date and time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(datePart);
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(timePart);
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

            Date combinedDateTime = calendar.getTime();

            String category = categorySpinner.getSelectedItem().toString();
            float km = Float.parseFloat(kmEditText.getText().toString());
            float price = Float.parseFloat(priceEditText.getText().toString());
            String pickup = pickupEditText.getText().toString();
            String dropoff = dropoffEditText.getText().toString();
            int width = Integer.parseInt(widthEditText.getText().toString());
            int height = Integer.parseInt(heightEditText.getText().toString());

            // Create an Intent with the specified action
            Intent resultIntent = new Intent(ACTION_UPDATE_VALUES);
            resultIntent.putExtra(EXTRA_DATE, combinedDateTime.getTime());  // Send combined date and time
            resultIntent.putExtra(EXTRA_CATEGORY, category);
            resultIntent.putExtra(EXTRA_KM, km);
            resultIntent.putExtra(EXTRA_PRICE, price);
            resultIntent.putExtra(EXTRA_PICKUP, pickup);
            resultIntent.putExtra(EXTRA_DROPOFF, dropoff);
            resultIntent.putExtra(EXTRA_WIDTH, width);
            resultIntent.putExtra(EXTRA_HEIGHT, height);
            resultIntent.putExtra(ACTION_ID, action_index);

            // Broadcast the intent
            sendBroadcast(resultIntent);

        } catch (Exception e) {
            Toast.makeText(SetRectangleValuesActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateEditText();
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateTimeEditText();
        }
    };

    private void updateDateEditText() {
        dateEditText.setText(String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
    }

    private void updateTimeEditText() {
        timeEditText.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
    }

    @Override
    public void finish() {
        is_running = false;
        super.finish();
    }

    @Override
    protected void onPause() {
        is_running = false;
        super.onPause();
    }
}
