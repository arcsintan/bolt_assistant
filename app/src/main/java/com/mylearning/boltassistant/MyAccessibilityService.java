package com.mylearning.boltassistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private static MyAccessibilityService instance;
    private Handler handler;
    private BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();
    private boolean debugMode = false;  // Set to true to enable debug overlay

    private WindowManager windowManager;
    private View debugOverlay;
    private static final int DEBUG_OVERLAY_DISPLAY_DURATION = 3000; // Duration to show overlay in milliseconds
    private StringBuilder fullText = new StringBuilder(); // Store the full extracted text
    private final Object lock = new Object();
    private StringBuilder allText=new StringBuilder();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        handler = new Handler(Looper.getMainLooper());
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        removeDebugOverlay();
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
                enqueueCommand(new SimulateTouchCommand(this, x, y, duration, timeUntilNextCommand));
            } else if ("extract_text".equals(action)) {
                Rect targetRect = intent.getParcelableExtra("targetRect");
                if (targetRect != null) {
                    Log.d(TAG, "Text extraction command received for rect: " + targetRect.toString());
                    enqueueCommand(new ReadTextCommand(this, targetRect));
                }
            } else if ("extract_all_text".equals(action)) {
                Log.d(TAG, "Text extraction command received to read all text");
                enqueueCommand(new ReadAllTextCommand(this));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void enqueueCommand(Command command) {
        try {
            commandQueue.put(command);
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to enqueue command", e);
        }
    }

    public void executeAllCommands() {
        MyLog.d(TAG, Thread.currentThread().getName());
        new Thread(() -> {
            while (!commandQueue.isEmpty()) {
                try {
                    Command command = commandQueue.take();
                    synchronized (lock) {
                        command.execute();
                        lock.wait();

                        // Check the result of text extraction commands
                        if (command instanceof ReadTextCommand) {
                            if (!((ReadTextCommand) command).getResult()) {
                                Log.d(TAG, "Text extraction failed, stopping execution.");
                                break;
                            }
                        } else if (command instanceof ReadAllTextCommand) {
                            if (!((ReadAllTextCommand) command).getResult()) {
                                Log.d(TAG, "Text extraction failed, stopping execution.");
                                break;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Command processing interrupted", e);
                    break;
                }
            }
        }).start();
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
                    handler.postDelayed(() -> {
                        if (callback != null) {
                            callback.run();
                        }
                        synchronized (lock) {
                            lock.notify();
                        }
                    }, timeUntilNextCommand);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.d(TAG, "Touch gesture cancelled");
                    handler.postDelayed(() -> {
                        if (callback != null) {
                            callback.run();
                        }
                        synchronized (lock) {
                            lock.notify();
                        }
                    }, timeUntilNextCommand);
                }
            }, null);

            if (!result) {
                Log.e(TAG, "Gesture dispatch failed");
                handler.postDelayed(() -> {
                    if (callback != null) {
                        callback.run();
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                }, timeUntilNextCommand);
            }
        } else {
            Log.e(TAG, "simulateTouch requires API level 24 or higher");
            handler.postDelayed(() -> {
                if (callback != null) {
                    callback.run();
                }
                synchronized (lock) {
                    lock.notify();
                }
            }, timeUntilNextCommand);
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
            if (debugMode) {
                showDebugOverlay(targetRect, extractedText);
            }
            handler.postDelayed(() -> {
                if (callback != null) {
                    callback.run();
                }
                synchronized (lock) {
                    lock.notify();
                }
            }, DEBUG_OVERLAY_DISPLAY_DURATION);
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
            //debugViewHierarchy(rootNode, 0, allText);

            logViewHierarchy();
            traverseNodeForText(rootNode);
            String extractedText = fullText.toString();
            MyLog.d(TAG, "Extracted text: " + extractedText);
            if (debugMode) {
                // this block is for debugging purposes.
                showDebugOverlay(extractedText);

            handler.postDelayed(() -> {
                if (callback != null) {
                    callback.run();
                }
                synchronized (lock) {
                    lock.notify();
                }
            }, DEBUG_OVERLAY_DISPLAY_DURATION);
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

    private void showDebugOverlay(Rect rect, String text) {

        handler.post(() -> {
            if (debugOverlay != null) {
                removeDebugOverlay();
            }

            TextView textView = new TextView(this);
            textView.setBackgroundColor(Color.argb(128, 255, 0, 0)); // Semi-transparent red
            textView.setTextColor(Color.WHITE);
            textView.setText(text);
            textView.setGravity(Gravity.CENTER);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    rect.width(),
                    rect.height(),
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = rect.left;
            params.y = rect.top;

            debugOverlay = textView;
            windowManager.addView(debugOverlay, params);

            // Blink effect
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (debugOverlay != null) {
                        debugOverlay.setVisibility(View.GONE);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (debugOverlay != null) {
                                    debugOverlay.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 500);
                    }
                }
            }, 500);

            // Remove overlay after a set duration
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeDebugOverlay();
                }
            }, DEBUG_OVERLAY_DISPLAY_DURATION);
        });
    }

    private void showDebugOverlay(String text) {

        handler.post(() -> {
            if (debugOverlay != null) {
                removeDebugOverlay();
            }

            TextView textView = new TextView(this);
            textView.setBackgroundColor(Color.argb(128, 255, 0, 0)); // Semi-transparent red
            textView.setTextColor(Color.WHITE);
            textView.setText(text);
            textView.setGravity(Gravity.CENTER);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 0;
            params.y = 0;

            debugOverlay = textView;
            windowManager.addView(debugOverlay, params);

            // Blink effect
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (debugOverlay != null) {
                        debugOverlay.setVisibility(View.GONE);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (debugOverlay != null) {
                                    debugOverlay.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 500);
                    }
                }
            }, 500);

            // Remove overlay after a set duration
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeDebugOverlay();
                }
            }, DEBUG_OVERLAY_DISPLAY_DURATION);
        });
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
    private void removeDebugOverlay() {
        if (debugOverlay != null) {
            windowManager.removeView(debugOverlay);
            debugOverlay = null;
        }
    }
}

