package collision;

import com.jme.math.Vector3f;

public interface ICollidable {
    public void addCollision(ICollidable c);
    public void clearCollisions();
    public void processCollisions();
    public void onCollision(ICollidable c);
    public float getSize();
    public Vector3f getPosition();
    public String getName();
    public void setName(String value);
}
