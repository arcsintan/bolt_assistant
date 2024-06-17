package com.mylearning.boltassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetCircleValuesActivity extends Activity {
    String TAG="SetCircleValuesActivity";
    public static final String EXTRA_DURATION = "com.mylearning.boltassistant.DURATION";
    public static final String EXTRA_TIME_UNTIL_NEXT_COMMAND = "com.mylearning.boltassistant.TIME_UNTIL_NEXT_COMMAND";
    public static final String ACTION_UPDATE_VALUES = "com.mylearning.boltassistant.ACTION_UPDATE_VALUES";

    private EditText durationEditText;
    private EditText timeUntilNextCommandEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_values);

        durationEditText = findViewById(R.id.duration_edit_text);
        timeUntilNextCommandEditText = findViewById(R.id.time_until_next_command_edit_text);

        Intent intent = getIntent();
        if (intent != null) {
            int duration1 = intent.getIntExtra(EXTRA_DURATION, 10);
            int timeUntilNextCommand1 = intent.getIntExtra(EXTRA_TIME_UNTIL_NEXT_COMMAND, 60);

            durationEditText.setText(String.valueOf(duration1));
            timeUntilNextCommandEditText.setText(String.valueOf(timeUntilNextCommand1));
        }

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int duration = Integer.parseInt(durationEditText.getText().toString());
                    int timeUntilNextCommand = Integer.parseInt(timeUntilNextCommandEditText.getText().toString());

                    // Send broadcast with updated values
                    sendUpdateBroadcast(duration, timeUntilNextCommand);

                    Toast.makeText(SetCircleValuesActivity.this, "Values updated", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (NumberFormatException e) {
                    Toast.makeText(SetCircleValuesActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void sendUpdateBroadcast(int duration, int timeUntilNextCommand) {
        Log.d(TAG,"data to broad cast, duration="+duration+" ,"+timeUntilNextCommand );
        Intent intent = new Intent(ACTION_UPDATE_VALUES);
        intent.putExtra(EXTRA_DURATION, duration);
        intent.putExtra(EXTRA_TIME_UNTIL_NEXT_COMMAND, timeUntilNextCommand);
        sendBroadcast(intent);
    }
}
