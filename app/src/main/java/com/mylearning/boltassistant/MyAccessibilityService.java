
package com.mylearning.boltassistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.mylearning.boltassistant.DataBase.TripDataManager;
import com.mylearning.boltassistant.TripSelector.AnalyzeText;
import com.mylearning.boltassistant.TripSelector.TripData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Add a field for the ExecutorService


public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private static MyAccessibilityService instance;
    private Handler handler;
    private boolean debugMode = false;

    private StringBuilder fullText = new StringBuilder(); // Store the full extracted text
    public Object lock = new Object();
    private StringBuilder allText = new StringBuilder();

    private  volatile boolean shouldAllCommandBeContinue = true;
    private  volatile boolean runningStatus = false;
    private static Thread commandThread; // The thread running the commands
    private  List<Command> commandList = new ArrayList<>();
    private List<String> importantTextData = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Map<Integer, List<String>> allDepthTextMap = new HashMap<>();
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


    public void addCommand(Command command) {
        commandList.add(command);
    }
    public void removeLastCommand(){
        if(!commandList.isEmpty()){
            commandList.remove(commandList.size()-1);
        }
    }
    public void clearCommandList(){
        if(commandList!=null) {
            commandList.clear();
        }
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
            TripData preTripData=new TripData();
            while (shouldAllCommandBeContinue && !Thread.currentThread().isInterrupted()) {
                TripData tripData = null;
                for (int i = 0; i < 7; i++) {
                    try {
                        if (!shouldAllCommandBeContinue) {
                            break;
                        }
                        Command command = commandList.get(i); // Take the next command
                        if (command.getTypeTag() == 0) {
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
                                command.execute(); // Execute the command and wait for it to complete
                                lock.wait(); // Wait for the command to finish
                            }

                                Boolean res= AnalyzeText.analyzeTextMap(this, allDepthTextMap, command.getRectangleData(), commandList);
                                tripData=AnalyzeText.getTripData();

                                if(!res)break;

                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Command processing interrupted", e);
                        shouldAllCommandBeContinue=false;
                        break; // Exit the loop if interrupted
                    }
                    //Log.d(TAG, "Command["+i+"]");
                }
                if(tripData != null &&  !tripData.equals(preTripData)) {
                    tripData.setQuality(4);
                    Context context = MyAccessibilityService.getInstance(); // Get the service instance
                    // Insert the trip data asynchronously
                    TripData finalTripData = tripData; // Ensure it's effectively final
                    executorService.submit(() -> {
                        Log.d(TAG, "A trip to be written in the data base "+ finalTripData.toString());
                        TripDataManager tripDataManager = new TripDataManager(context);
                        tripDataManager.insertTripData(finalTripData);
                        tripDataManager.close();

                    });
                }
                preTripData=tripData;

            }
            Log.d(TAG, "commandList size="+commandList.size());
            Log.d(TAG, "should be continue value= "+shouldAllCommandBeContinue);

        });
        commandThread.start();
        runningStatus = false;
    }
    public void extractAllTextInAllDepth(Runnable callback) {
        handler.post(() -> {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null");
                if (callback != null) {
                    callback.run();
                }
                return;
            }
            allDepthTextMap.clear(); // Clear previous data
            collectTextInAllDepths(rootNode, 0); // Start collecting data from depth 0
            if (callback != null) {
                callback.run();
            }
        });
    }
//this is a recursive function which collect the data in the nodes.
    private void collectTextInAllDepths(AccessibilityNodeInfo node, int currentDepth) {
        if (node == null) return;

        if (node.getText() != null) {
                allDepthTextMap.computeIfAbsent(currentDepth, k -> new ArrayList<>()).add(node.getText().toString());
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            if (childNode != null) {
                collectTextInAllDepths(childNode, currentDepth + 1);
            }
        }
    }



    public void stopAllCommands() {
        shouldAllCommandBeContinue = false;
        runningStatus=false;
        if (commandThread != null) {
            commandThread.interrupt();
        }
    }

    // Method to analyze the extracted text


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

    private void textInDepth(AccessibilityNodeInfo node, int targetDepth, List<String> result) {
        collectTextInDepth(node, 0, targetDepth, result);
    }
    private void collectTextInDepth(AccessibilityNodeInfo node, int currentDepth, int targetDepth, List<String> result) {
        if (node == null) return;

        if (currentDepth == targetDepth && node.getText() != null) {
            result.add(node.getText().toString());
            //Log.d(TAG, "Node text at depth " + targetDepth + ": " + node.getText().toString());
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            if (childNode != null) {
                collectTextInDepth(childNode, currentDepth + 1, targetDepth, result);
            }
        }
    }

    public void extractAllTextInDepth(Runnable callback) {
        handler.post(() -> {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null");
                if (callback != null) {
                    callback.run();
                }
                return;
            }
            importantTextData.clear(); // Clear previous data
            textInDepth(rootNode, 5, importantTextData); // Collect data at depth 5
            if (callback != null) {
                callback.run();
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
    public void simulateTouch(float x, float y, int duration) {
        //Log.d(TAG, Thread.currentThread().getName());
            Path path = new Path();
            path.moveTo(x, y);
            //Log.d(TAG, "Path moved to x=" + x + ", y=" + y);
            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, duration);
            GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();

            boolean result = dispatchGesture(gesture, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    //Log.d(TAG, "Touch gesture completed , x=" + x + ", y=" + y);
                    synchronized (lock) {
                        //Log.d(TAG, " is going to notified the "+Thread.currentThread().getName());
                        lock.notify();
                    }
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    //Log.d(TAG, "Touch gesture cancelled , x=" + x + ", y=" + y);
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }, null);

            if (!result) {
                Log.e(TAG, "Gesture dispatch failed");
                synchronized (lock) {
                    lock.notify();
                }
            }

    }



    private void turnOnAllCommand() {
        shouldAllCommandBeContinue = true;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        executorService.shutdownNow();
    }
}
