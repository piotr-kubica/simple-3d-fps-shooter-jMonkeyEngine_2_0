package util;

import java.util.Date;

public class GameTimer {
    
    private Date startTime;
    private Date lastTime;
    
    public GameTimer() {
        startTime = lastTime = new Date();
    }
    
    public float getTimePerFrame() {
        Date currentTime = new Date();
        long diff = currentTime.getTime() - lastTime.getTime();
        lastTime = currentTime;
        return diff / 1000.0f + 0.0001f;
    }
    
    public long getTimeInSeconds() {
        return (new Date().getTime() - startTime.getTime()) / 1000;
    }
}
