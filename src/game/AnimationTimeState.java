package game;

/**
 * OGRE
 * stand: 0 - 39
 * run: 40 - 45
 * death: 178 - 183
 *
 * TRIS
 * stand: 0 - 39
 * run: 40 - 45
 * death: 178 - 183
 */
public class AnimationTimeState {
    
    private int startTime;
    private int endTime;
         
    public AnimationTimeState(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public int getEndTime() {
        return endTime;
    }

    public int getStartTime() {
        return startTime;
    }
}
