package com.mylearning.boltassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends Activity {
    private ArrayList<String> shapeViewsJson;
    private ListView listViewConfigs;
    private ShapeConfigManager configManager;
    private List<ShapeConfigManager.ShapeConfig> configs;
    private ShapeConfigManager.ShapeConfig selectedConfig;
    public static final String ACTION_SETTINGS_RESULT = "com.mylearning.boltassistant.ACTION_SETTINGS_RESULT";
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shapeViewsJson = (ArrayList<String>) getIntent().getSerializableExtra("shapeViewsJson");
        configManager = ShapeConfigManager.getInstance(this);

        showSettingsDialog();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);

        dialog = builder.create();

        Button btnNewConfig = dialogView.findViewById(R.id.button_new_configuration);
        listViewConfigs = dialogView.findViewById(R.id.list_view_item);
        Button btnCancel = dialogView.findViewById(R.id.button_cancel);

        btnNewConfig.setOnClickListener(v -> showSaveConfigDialog());
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            finish();  // Finish the activity when the dialog is dismissed
        });

        loadConfigs();

        listViewConfigs.setOnItemClickListener((parent, view, position, id) -> {
            selectedConfig = configs.get(position);
            sendResultAndFinish();
            dialog.dismiss();
        });

        listViewConfigs.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedConfig = configs.get(position);
            showDeletePopup();
            return true;
        });

        dialog.show();
    }

    private void loadConfigs() {
        configs = configManager.getConfigs();
        List<String> configNames = new ArrayList<>();
        for (ShapeConfigManager.ShapeConfig config : configs) {
            configNames.add(config.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, configNames);
        listViewConfigs.setAdapter(adapter);
    }

    private void showSaveConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Configuration");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String configName = input.getText().toString();
            configManager.saveConfig(configName, shapeViewsJson);
            Toast.makeText(this, "Configuration saved", Toast.LENGTH_SHORT).show();
            loadConfigs(); // Refresh the list
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeletePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Configuration")
                .setMessage("Are you sure you want to delete this configuration?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    configManager.deleteConfig(selectedConfig.getName());
                    Toast.makeText(this, "Configuration deleted", Toast.LENGTH_SHORT).show();
                    loadConfigs(); // Refresh the list
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void sendResultAndFinish() {
        if (selectedConfig != null) {
            Intent resultIntent = new Intent(ACTION_SETTINGS_RESULT);
            resultIntent.putExtra("loadedConfig", (Serializable) selectedConfig.getShapes());
            sendBroadcast(resultIntent);
            finish();
        } else {
            Toast.makeText(this, "No configuration selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
