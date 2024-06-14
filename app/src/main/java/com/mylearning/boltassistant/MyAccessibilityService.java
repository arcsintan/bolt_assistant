package com.mylearning.boltassistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.mylearning.boltassistant.TripSelector.AbstractSelector;
import com.mylearning.boltassistant.TripSelector.BoltNormal;

import java.util.ArrayList;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private static MyAccessibilityService instance;
    private Handler handler;
    private List<Command> commandList = new ArrayList<>();
    private boolean debugMode = false;

    private StringBuilder fullText = new StringBuilder(); // Store the full extracted text
    private final Object lock = new Object();
    private StringBuilder allText = new StringBuilder();

    private volatile boolean shouldBeContinue = true;
    private volatile boolean runningStatus = false;
    private Thread commandThread; // The thread running the commands

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        handler = new Handler(Looper.getMainLooper());
    }

    public static MyAccessibilityService getInstance() {
        return instance;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Handle accessibility events if necessary
    }

    @Override
    public void onInterrupt() {
        // Handle service interruption
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra("action");
            if ("simulate_touch".equals(action)) {
                float x = intent.getFloatExtra("x", 0);
                float y = intent.getFloatExtra("y", 0);
                int duration = intent.getIntExtra("duration", 100);
                int timeUntilNextCommand = intent.getIntExtra("timeUntilNextCommand", 1000);

                Log.d(TAG, "A touch message received to click on " + x + ", " + y);
                addCommand(new SimulateTouchCommand(this, x, y, duration, timeUntilNextCommand));
            } else if ("extract_text".equals(action)) {
                Rect targetRect = intent.getParcelableExtra("targetRect");
                if (targetRect != null) {
                    Log.d(TAG, "Text extraction command received for rect: " + targetRect.toString());
                    addCommand(new ReadTextCommand(this, targetRect));
                }
            } else if ("extract_all_text".equals(action)) {
                Log.d(TAG, "Text extraction command received to read all text");
                addCommand(new ReadAllTextCommand(this));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void addCommand(Command command) {
        commandList.add(command);
    }

    public void executeAllCommands() {
        turnOnAllCommand();
        System.out.println("Size of the Queue = " + commandList.size());
        if (runningStatus) {
            Log.d(TAG, "executeAllCommand is already running a task");
            return; // Exit if already running
        }
        runningStatus = true;
        commandThread = new Thread(() -> {
            while (shouldBeContinue) {
                for (int i = 0; i < commandList.size(); i++) {
                    try {
                        if (!shouldBeContinue) {
                            break;
                        }
                        Command command = commandList.get(i); // Take the next command
                        if (command.getType() == 0) {
                            long startTime = System.currentTimeMillis();
                            synchronized (lock) {
                                command.execute(); // Execute the command
                                lock.wait(); // Wait for the command to finish
                            }
                            long executionTime = System.currentTimeMillis() - startTime;
                            long remainingTime = command.getTimeUntilNextCommand() - executionTime;
                            if (remainingTime > 0) {
                                Thread.sleep(remainingTime); // Wait if necessary
                            }
                        } else {
                            synchronized (lock) {
                                command.execute(); // Execute the command
                                // here I need to do text analysis
                                AbstractSelector tripSelector = new BoltNormal(getFullText());
                                Boolean res = tripSelector.selectInput();

                                lock.wait();
                                // Wait for the command to finish
                            }
                        }

                    } catch (InterruptedException e) {
                        Log.e(TAG, "Command processing interrupted", e);
                        shouldBeContinue = false;
                        break; // Exit the loop if interrupted
                    }
                }
            }
        });
        commandThread.start();
        runningStatus = false;
    }

    public void stopAllCommands() {
        shouldBeContinue = false;
        runningStatus=false;
        if (commandThread != null) {
            commandThread.interrupt();
        }
    }

    // Method to analyze the extracted text
    public boolean analyzeExtractedText(String text) {
        MyLog.d(TAG, Thread.currentThread().getName());
        // Implement your analysis logic here
        // Return true if analysis succeeds, false if it fails
        MyLog.d(TAG, getFullText());
        return !text.isEmpty();  // Example: fail if text is empty
    }

    public void simulateTouch(float x, float y, int duration, int timeUntilNextCommand, Runnable callback) {
        MyLog.d(TAG, Thread.currentThread().getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);
            Log.d(TAG, "Path moved to x=" + x + ", y=" + y);
            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, duration);
            GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();

            boolean result = dispatchGesture(gesture, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.d(TAG, "Touch gesture completed");
                    if (callback != null) {
                        callback.run();
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.d(TAG, "Touch gesture cancelled");
                    if (callback != null) {
                        callback.run();
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }, null);

            if (!result) {
                Log.e(TAG, "Gesture dispatch failed");
                if (callback != null) {
                    callback.run();
                }
                synchronized (lock) {
                    lock.notify();
                }
            }
        } else {
            Log.e(TAG, "simulateTouch requires API level 24 or higher");
            if (callback != null) {
                callback.run();
            }
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    public void extractTextFromRect(Rect targetRect, Runnable callback) {
        MyLog.d(TAG, Thread.currentThread().getName());
        handler.post(() -> {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null");
                if (callback != null) {
                    callback.run();
                }
                synchronized (lock) {
                    lock.notify();
                }
                return;
            }
            // Debug view hierarchy
            debugViewHierarchy(rootNode, 0, allText);
            String extractedText = traverseNodeForText(rootNode, targetRect);
            Log.d(TAG, "Extracted text: " + extractedText);
            if (callback != null) {
                callback.run();
            }
            synchronized (lock) {
                lock.notify();
            }
        });
    }

    public void extractAllText(Runnable callback) {
        handler.post(() -> {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null");
                if (callback != null) {
                    callback.run();
                }
                synchronized (lock) {
                    lock.notify();
                }
                return;
            }
            fullText.setLength(0); // Clear previous text
            logViewHierarchy();
            traverseNodeForText(rootNode);
            String extractedText = fullText.toString();
            MyLog.d(TAG, "Extracted text: " + extractedText);
            if (callback != null) {
                callback.run();
            }
            synchronized (lock) {
                lock.notify();
            }
        });
    }

    private String traverseNodeForText(AccessibilityNodeInfo node, Rect targetRect) {
        if (node == null) return "";

        Rect nodeRect = new Rect();
        node.getBoundsInScreen(nodeRect);

        StringBuilder textBuilder = new StringBuilder();
        Log.d(TAG, "Node bounds: " + nodeRect.toShortString() + " Target bounds: " + targetRect.toShortString());
        if (Rect.intersects(targetRect, nodeRect)) {
            Log.d(TAG, "Node intersects with target rect");
            if (node.getText() != null) {
                textBuilder.append(node.getText().toString()).append(" ");
                Log.d(TAG, "Node text within target rect: " + node.getText().toString());
            } else {
                Log.d(TAG, "Node within target rect has no text");
            }

            if (node.getChildCount() > 0) {
                Log.d(TAG, "Node has children, traversing them");
            } else {
                Log.d(TAG, "Node has no children");
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            if (childNode != null) {
                textBuilder.append(traverseNodeForText(childNode, targetRect));
            }
        }

        return textBuilder.toString().trim();
    }

    private void traverseNodeForText(AccessibilityNodeInfo node) {
        if (node == null) return;

        if (node.getText() != null) {
            fullText.append(node.getText().toString()).append(" ");
            Log.d(TAG, "Node text: " + node.getText().toString());
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            if (childNode != null) {
                traverseNodeForText(childNode);
            }
        }
    }

    public String getFullText() {
        return fullText.toString();
    }

    // Method to traverse and log the view hierarchy
    private void debugViewHierarchy(AccessibilityNodeInfo node, int depth, StringBuilder result) {
        if (node == null) return;

        // Create an indentation string based on the current depth
        String indent = new String(new char[depth]).replace("\0", "-");

        // Append the class name and text of the current node to the result
        result.append(indent)
                .append(" Class Name: ").append(node.getClassName())
                .append(", Text: ").append(node.getText())
                .append("\n");

        // Recursively call this method for each child node, increasing the depth by 1
        for (int i = 0; i < node.getChildCount(); i++) {
            debugViewHierarchy(node.getChild(i), depth + 1, result);
        }
    }

    // Method to retrieve the view hierarchy text
    public String getDebugViewHierarchyText(AccessibilityNodeInfo rootNode) {
        StringBuilder result = new StringBuilder();
        debugViewHierarchy(rootNode, 0, result); // Start with depth 0
        return result.toString();
    }

    // Example method to demonstrate usage (can be triggered by an event)
    public void logViewHierarchy() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            String viewHierarchy = getDebugViewHierarchyText(rootNode);
            Log.d(TAG, viewHierarchy);
        }
    }



    private void turnOnAllCommand() {
        shouldBeContinue = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
