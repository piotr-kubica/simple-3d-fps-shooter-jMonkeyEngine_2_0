package game;

import collision.ICollidable;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import event.GameActionListener;

public class Missile extends PhysicsObject {
    
    public enum Event {
        HIT_MISSILE, MISSED
    };
    
    private Node mesh;
    private Terrain terrain;
    private float yMaxOffset = 1000;
    
    public Missile(Node mesh, Terrain terrain, boolean addToCollisionSystem) {
        super(addToCollisionSystem);
        this.mesh = mesh;
        this.terrain = terrain;
        this.attachChild(mesh);
    }
    
    public void setDirection(Vector3f direction) {
        this.getLocalRotation().lookAt(direction, Vector3f.UNIT_Y);
    }
    
    @Override
    protected void applyRotation() { } // do nothing with this.rotation
    
    @Override
    public void onCollision(ICollidable c) {
        if(c instanceof Enemy) {
            dispatchEvent(Event.HIT_MISSILE.name(), (GameObject)c);
        }
    }
    
    @Override
    protected void preUpdate(float timePerFrame) {
        super.preUpdate(timePerFrame);
        Vector3f max = terrain.maxBoundingWorldVertex();
        Vector3f min = terrain.minBoundingWorldVertex();
        Vector3f pos = this.getPosition();
        
        if(this.getParent() != null && this.getParent() == terrain) {
            if(pos.x > max.x || pos.x < min.x || pos.z > max.z || pos.z < min.z 
                    || (pos.y > terrain.getHeight(pos.x, pos.z) + yMaxOffset)) {
                dispatchEvent(Event.MISSED.name(), null);
            } else if(pos.y - 10 <= terrain.getHeight(pos.x, pos.z)) {
                dispatchEvent(Event.HIT_MISSILE.name(), null);
            }
            
        }
    }
    
    public boolean addObserver(Event event, GameActionListener observer) {
        return super.addObserver(event.name(), observer);
    }
    
    public boolean removeObserver(Event event, GameActionListener observer) {
        return super.removeObserver(event.name(), observer);
    }
    
    public boolean containsObserver(Event event, GameActionListener observer) {
        return super.containsObserver(event.name(), observer);
    }

    public float getyMaxOffset() {
        return yMaxOffset;
    }

    public void setyMaxOffset(float yMaxOffset) {
        this.yMaxOffset = yMaxOffset;
    }
}
