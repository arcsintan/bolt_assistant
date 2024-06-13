package com.mylearning.boltassistant;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ShapeConfigManager {
    private static ShapeConfigManager instance;
    private Context context;

    private ShapeConfigManager(Context context) {
        this.context = context;
    }

    public static synchronized ShapeConfigManager getInstance(Context context) {
        if (instance == null) {
            instance = new ShapeConfigManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveConfig(String name, ArrayList<String> shapesJson) {
        SharedPreferences prefs = context.getSharedPreferences("ShapeConfigs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(shapesJson);
        editor.putString(name, json);
        editor.apply();
    }

    public List<ShapeConfig> getConfigs() {
        SharedPreferences prefs = context.getSharedPreferences("ShapeConfigs", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        List<ShapeConfig> configs = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> shapesJson = gson.fromJson((String) entry.getValue(), type);
            configs.add(new ShapeConfig(entry.getKey(), shapesJson));
        }
        return configs;
    }

    public void deleteConfig(String name) {
        SharedPreferences prefs = context.getSharedPreferences("ShapeConfigs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(name);
        editor.apply();
    }

    public static class ShapeConfig {
        private String name;
        private ArrayList<String> shapes;

        public ShapeConfig(String name, ArrayList<String> shapes) {
            this.name = name;
            this.shapes = shapes;
        }

        public String getName() {
            return name;
        }

        public ArrayList<String> getShapes() {
            return shapes;
        }
    }
}
