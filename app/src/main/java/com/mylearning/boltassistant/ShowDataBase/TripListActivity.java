package com.mylearning.boltassistant.ShowDataBase;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mylearning.boltassistant.DataBase.TripDataManager;
import com.mylearning.boltassistant.R;
import com.mylearning.boltassistant.TripSelector.TripData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripListActivity extends AppCompatActivity {
    final String TAG="TripListActivity";
    private RecyclerView recyclerView;
    private TripAdapter tripAdapter;
    private TripDataManager tripDataManager;
    private EditText startDateEditText, endDateEditText;
    private Button filterButton;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_data_base_view);

        recyclerView = findViewById(R.id.recyclerViewTrips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        filterButton = findViewById(R.id.filterButton);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Set default dates
        Calendar calendar = Calendar.getInstance();
        endDateEditText.setText(dateFormat.format(calendar.getTime())); // Today
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        startDateEditText.setText(dateFormat.format(calendar.getTime())); // Yesterday

        tripDataManager = new TripDataManager(this);
        List<TripData> tripDataList = tripDataManager.getAllTripData();
        tripAdapter = new TripAdapter(tripDataList, this::openGoogleMaps, this);
        recyclerView.setAdapter(tripAdapter);

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        filterButton.setOnClickListener(v -> filterTrips());
    }

    private void filterTrips() {
        try {
            String startDateStr = startDateEditText.getText().toString();
            String endDateStr = endDateEditText.getText().toString();

            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            List<TripData> filteredTrips = tripDataManager.getTripsBetweenDates(startDate, endDate);
            tripAdapter = new TripAdapter(filteredTrips, this::openGoogleMaps, this);
            recyclerView.setAdapter(tripAdapter);

        } catch (Exception e) {
            Toast.makeText(this, "Invalid date range", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            editText.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trip_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_all) {
            deleteAllTripData();
            return true;
        } else if (id == R.id.action_backup) {
            backupTripData();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllTripData() {
        tripDataManager.deleteAllTripData();
        List<TripData> tripDataList = tripDataManager.getAllTripData();
        tripAdapter = new TripAdapter(tripDataList, this::openGoogleMaps, this);
        recyclerView.setAdapter(tripAdapter);
        Toast.makeText(this, "All trip data deleted", Toast.LENGTH_SHORT).show();
    }

    private void backupTripData() {
        boolean success = tripDataManager.backupDatabase();
        if (success) {
            Toast.makeText(this, "Backup successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Backup failed", Toast.LENGTH_SHORT).show();
        }
    }


    private void openGoogleMaps(String addressStart, String addressEnd) {
        // Construct the URL
        String url = "https://www.google.com/maps/dir/?api=1&origin=" + Uri.encode(addressStart) + "&destination=" + Uri.encode(addressEnd);

        // Print the URL to log
        Log.d(TAG, "Google Maps URL: " + url);

        // Show the URL in a toast
        Toast.makeText(this, "Google Maps URL:\n" + url, Toast.LENGTH_LONG).show();

        try {
            Uri gmmIntentUri = Uri.parse(url);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

            // First, try to open with Google Maps if available
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Remove package restriction and try with any available map app
                mapIntent.setPackage(null);
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "No map applications available", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening map application: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
