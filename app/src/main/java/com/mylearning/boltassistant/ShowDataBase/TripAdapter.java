package com.mylearning.boltassistant.ShowDataBase;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mylearning.boltassistant.DataBase.TripDataManager;
import com.mylearning.boltassistant.R;
import com.mylearning.boltassistant.TripSelector.TripData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private final List<TripData> tripDataList;
    private final OnItemClickListener listener;
    private final Context context;

    public interface OnItemClickListener {
        void onItemClick(String addressStart, String addressEnd);
    }

    public TripAdapter(List<TripData> tripDataList, OnItemClickListener listener, Context context) {
        Collections.reverse(tripDataList); // Reverse the list to show the last entered data first
        this.tripDataList = tripDataList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        TripData tripData = tripDataList.get(position);
        holder.bind(tripData, listener);

        holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            MenuInflater inflater = new MenuInflater(v.getContext());
            inflater.inflate(R.menu.menu_trip_options, menu);

            menu.findItem(R.id.action_copy).setOnMenuItemClickListener(item -> {
                copyToClipboard(v.getContext(), tripData);
                return true;
            });

            menu.findItem(R.id.action_delete).setOnMenuItemClickListener(item -> {
                deleteTripData(tripData, position);
                return true;
            });
        });
    }

    private void deleteTripData(TripData tripData, int position) {
        TripDataManager tripDataManager = new TripDataManager(context);
        tripDataManager.deleteTripData(tripData.getId());
        tripDataList.remove(position);
        notifyItemRemoved(position);
    }

    private void copyToClipboard(Context context, TripData tripData) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String tripDataString = String.format("Pickup DateTime: %s\nOrder Time: %s\nCategory: %s\nDistance: %.2f km\nNet Price: €%.2f\nNet Price per Distance: €%.2f/km\nPickup Point: %s\nDropoff Point: %s",
                new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(tripData.getPickupDateTime()),
                new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(tripData.getOrderTime()),
                tripData.getCategory(),
                tripData.getDistance(),
                tripData.getPrice() * 0.75f,
                (tripData.getPrice() * 0.75f) / tripData.getDistance(),
                tripData.getAddressStart(),
                tripData.getAddressEnd());
        ClipData clip = ClipData.newPlainText("Trip Data", tripDataString);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Trip data copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to copy trip data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return tripDataList.size();
    }

    class TripViewHolder extends RecyclerView.ViewHolder {
        private final TextView pickupDateTimeTextView;
        private final TextView orderTimeTextView;
        private final TextView categoryTextView;
        private final TextView distanceTextView;
        private final TextView netPriceTextView;
        private final TextView netPricePerDistanceTextView;
        private final TextView addressStartTextView;
        private final TextView addressEndTextView;
        private final CardView cardView;
        private final LinearLayout pickupDateTimeContainer;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            pickupDateTimeContainer = itemView.findViewById(R.id.pickupDateTimeContainer);
            cardView = itemView.findViewById(R.id.cardView);
            pickupDateTimeTextView = itemView.findViewById(R.id.textPickupDateTime);
            orderTimeTextView = itemView.findViewById(R.id.textOrderTime);
            categoryTextView = itemView.findViewById(R.id.textCategory);
            distanceTextView = itemView.findViewById(R.id.textDistance);
            netPriceTextView = itemView.findViewById(R.id.textNetPrice);
            netPricePerDistanceTextView = itemView.findViewById(R.id.textNetPricePerDistance);
            addressStartTextView = itemView.findViewById(R.id.textAddressStart);
            addressEndTextView = itemView.findViewById(R.id.textAddressEnd);
        }

        public void bind(TripData tripData, OnItemClickListener listener) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
            String pickupDateTime = sdf.format(tripData.getPickupDateTime());
            String orderTime = sdf.format(tripData.getOrderTime());

            pickupDateTimeTextView.setText(pickupDateTime);
            orderTimeTextView.setText(orderTime);
            categoryTextView.setText(tripData.getCategory() + ", ");
            distanceTextView.setText(String.format(Locale.getDefault(), "%.1f km.", tripData.getDistance()));

            float netPrice = tripData.getPrice() * 0.75f;
            float netPricePerKm = netPrice / tripData.getDistance();
            netPriceTextView.setText(String.format(Locale.getDefault(), "%.2f€, ", netPrice));
            netPricePerDistanceTextView.setText(String.format(Locale.getDefault(), "%.2f€/km", netPricePerKm));

            // Color handling for net price per km
            if (netPricePerKm >= 1.0f) {
                netPricePerDistanceTextView.setBackgroundColor(Color.GREEN);
            } else {
                float ratio = netPricePerKm / 1.0f;
                int green = (int) (255 * ratio);
                int red = 255 - green;
                netPricePerDistanceTextView.setBackgroundColor(Color.rgb(red, green, 0));
            }

            addressStartTextView.setText(tripData.getAddressStart());
            addressEndTextView.setText(tripData.getAddressEnd());

            // Set background color for pickup part based on success
            if (tripData.isSuccess()) {
                pickupDateTimeContainer.setBackgroundColor(Color.parseColor("#ADD8E6")); // Light blue
            } else {
                pickupDateTimeContainer.setBackgroundColor(Color.rgb(255, 235, 59));
            }

            // Set click listener to show dialog with options
            pickupDateTimeTextView.setOnClickListener(v -> showOptionsDialog(v.getContext(), tripData));
            addressStartTextView.setOnClickListener(v -> showNavigationDialog(v.getContext(), tripData.getAddressStart()));
            addressEndTextView.setOnClickListener(v -> showNavigationDialog(v.getContext(), tripData.getAddressEnd()));

            // Set long press listener to show context menu
            itemView.setOnLongClickListener(v -> {
                v.showContextMenu();
                return true;
            });
        }

        private void openLocationInGoogleMaps(Context context, String address) {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                Toast.makeText(context, "Google Maps app not found", Toast.LENGTH_SHORT).show();
            }
        }

        private void openLocationInWaze(Context context, String address) {
            Uri wazeUri = Uri.parse("https://waze.com/ul?q=" + Uri.encode(address));
            Intent wazeIntent = new Intent(Intent.ACTION_VIEW, wazeUri);
            wazeIntent.setPackage("com.waze");

            if (wazeIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(wazeIntent);
            } else {
                Toast.makeText(context, "Waze app not found", Toast.LENGTH_SHORT).show();
            }
        }

        private void showOptionsDialog(Context context, TripData tripData) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Choose an action")
                    .setItems(new String[]{"Add to Calendar", "Set Alarm"}, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                addEventToCalendar(context, tripData);
                                break;
                            case 1:
                                setAlarmForPickup(context, tripData);
                                break;
                        }
                    })
                    .show();
        }

        private void showNavigationDialog(Context context, String address) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Choose an action")
                    .setItems(new String[]{"Open in Google Maps", "Open in Waze"}, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                openLocationInGoogleMaps(context, address);
                                break;
                            case 1:
                                openLocationInWaze(context, address);
                                break;
                        }
                    })
                    .show();
        }

        private void addEventToCalendar(Context context, TripData tripData) {
            try {
                // Prepare the start and end times in the required format
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault());

                // Get the pickup time and adjust for the start time (1 hour before pickup)
                Date pickupDateTime = tripData.getPickupDateTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(pickupDateTime);
                calendar.add(Calendar.HOUR, -1);
                Date oneHourBeforePickup = calendar.getTime();

                // Format the start and end times
                String startTime = sdf.format(oneHourBeforePickup);
                String endTime = sdf.format(pickupDateTime);

                // Construct the Google Calendar event URL
                Uri.Builder builder = Uri.parse("https://www.google.com/calendar/render").buildUpon();
                builder.appendQueryParameter("action", "TEMPLATE");
                builder.appendQueryParameter("text", "Trip Pickup");
                builder.appendQueryParameter("dates", startTime + "/" + endTime);
                builder.appendQueryParameter("details", "Trip from " + tripData.getAddressStart() + " to " + tripData.getAddressEnd());
                builder.appendQueryParameter("location", tripData.getAddressStart() + " to " + tripData.getAddressEnd());
                builder.appendQueryParameter("trp", "true"); // To specify a reminder

                // Create an intent to view the URL
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(builder.build());

                // Check if an app can handle this intent
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "No application found to handle the event", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Error adding event to Google Calendar", Toast.LENGTH_SHORT).show();
            }
        }


        private void setAlarmForPickup(Context context, TripData tripData) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tripData.getPickupDateTime());

            // Subtract one hour from the pickup time
            calendar.add(Calendar.HOUR_OF_DAY, -1);

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                    .putExtra(AlarmClock.EXTRA_HOUR, hour)
                    .putExtra(AlarmClock.EXTRA_MINUTES, minute)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, "Trip Pickup Reminder");

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "No clock app found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
