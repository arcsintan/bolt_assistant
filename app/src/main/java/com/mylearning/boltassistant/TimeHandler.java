package com.mylearning.boltassistant;

public class TimeHandler {
    private long startTime;
    public TimeHandler() {
        startTime =  System.currentTimeMillis();;
    }
    public long pastTime() {
        return System.currentTimeMillis() - startTime;
    }
public long waitingTime(long timeUntilNextCommand){
        return timeUntilNextCommand-pastTime();
}


}
