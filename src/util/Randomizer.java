package util;

import com.jme.math.FastMath;
import java.util.Random;

/**
 *
 * @author dbow
 */
public class Randomizer {
    
    private static Random r = new Random();
    
    public static float GetRandom(float from, float to) {
        if(from >= to) {
            throw new IllegalArgumentException("Parameter to can not be less or equal than from");
        }
        float range = to - from;
        return r.nextFloat() * range + from;
    }
}
