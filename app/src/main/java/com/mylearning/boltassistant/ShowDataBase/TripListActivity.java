package com.mylearning.boltassistant.ShowDataBase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mylearning.boltassistant.DataBase.TripDataManager;
import com.mylearning.boltassistant.R;
import com.mylearning.boltassistant.TripSelector.TripData;

import java.util.List;

public class TripListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TripAdapter tripAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        recyclerView = findViewById(R.id.recyclerViewTrips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TripDataManager tripDataManager = new TripDataManager(this);
        List<TripData> tripDataList = tripDataManager.getAllTripData();
        tripDataManager.close();

        tripAdapter = new TripAdapter(tripDataList, this::openGoogleMaps);
        recyclerView.setAdapter(tripAdapter);
    }

    private void openGoogleMaps(String addressStart, String addressEnd) {
        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + Uri.encode(addressStart) + "&destination=" + Uri.encode(addressEnd));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps is not installed", Toast.LENGTH_LONG).show();
        }
    }
}
