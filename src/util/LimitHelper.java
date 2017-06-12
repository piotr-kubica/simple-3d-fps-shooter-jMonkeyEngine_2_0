package util;

import com.jme.math.Vector3f;

public class LimitHelper {

    public static Vector3f LimitAbs(Vector3f v, Vector3f limiter) {
        if (limiter.x < 0 || limiter.y < 0 || limiter.z < 0) {
            throw new IllegalArgumentException("limiter values must not be negative");
        }

        if (v.x > limiter.x) {
            v.x = limiter.x;
        } else if (v.x < -limiter.x) {
            v.x = -limiter.x;
        }

        if (v.y > limiter.y) {
            v.y = limiter.y;
        } else if (v.y < -limiter.y) {
            v.y = -limiter.y;
        }

        if (v.z > limiter.z) {
            v.z = limiter.z;
        } else if (v.z < -limiter.z) {
            v.z = -limiter.z;
        }

        return v;
    }

    public static Vector3f LimitMin(Vector3f v, Vector3f limiter) {
        if (v.x < limiter.x) {
            v.x = limiter.x;
        }

        if (v.y < limiter.y) {
            v.y = limiter.y;
        }

        if (v.z < limiter.z) {
            v.z = limiter.z;
        }

        return v;
    }

    public static Vector3f LimitMax(Vector3f v, Vector3f limiter) {
        if (v.x > limiter.x) {
            v.x = limiter.x;
        }

        if (v.y > limiter.y) {
            v.y = limiter.y;
        }

        if (v.z > limiter.z) {
            v.z = limiter.z;
        }

        return v;
    }
}
