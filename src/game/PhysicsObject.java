package game;

import collision.ICollidable;
import com.jme.math.Vector3f;
import util.LimitHelper;

public class PhysicsObject extends GameObject {
    protected float linVel = 0;
    protected Vector3f rotVel = new Vector3f(0, 0, 0); // deree euler angles rotation
    protected Vector3f maxRotVel = new Vector3f(1, 1, 1); // deree euler angles rotation
    
    protected Vector3f position = new Vector3f(0, 0, 0);
    protected Vector3f rotation = new Vector3f(0, 0, 0); // deree euler angles rotation
    protected Vector3f calculatedVel = new Vector3f();
    
    public PhysicsObject() {
        super();
    }
    
    public PhysicsObject(boolean addToCollisionSystem) {
        super(addToCollisionSystem);
    }
    
    public PhysicsObject(float size, boolean addToCollisionSystem) {
        super(size, addToCollisionSystem);
    }
    
    public float getLinVel() {
        return linVel;
    }

    public void setLinVel(float linVel) {
        this.linVel = linVel;
    }

    public Vector3f getMaxRotVel() {
        return maxRotVel;
    }

    public void setMaxRotVel(Vector3f maxRotVel) {
        this.maxRotVel = maxRotVel;
    }

    public Vector3f getRotVel() {
        return rotVel;
    }

    public void setRotVel(Vector3f rotVel) {
        this.rotVel = rotVel;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }
    
    public void setPosition(Vector3f position) {
        this.position = position;
    }
    
    @Override
    public Vector3f getPosition() {
        return position;
    }
    
    @Override
    public void onCollision(ICollidable c) { }
    
    @Override
    protected void preUpdate(float timePerFrame) {
        updatePhysics(timePerFrame);
        processCollisions();
        applyRotation();
        applyPosition();
    }
    
    protected void updatePhysics(float timePerFrame) {
        // treat Z pointing vector as forward direction for velocity
        calculatedVel = this.getLocalRotation().getRotationColumn(2).mult(timePerFrame * linVel);
        position.addLocal(calculatedVel);
        rotation.addLocal(LimitHelper.LimitAbs(rotVel.mult(timePerFrame), maxRotVel));
    }
    
    protected void applyPosition() {
        this.setLocalTranslation(position);
    }
    
    protected void applyRotation() {
        this.setDegreeRotation(rotation);
    }

    protected void setDegreeRotation(float rx, float ry, float rz){
        this.getLocalRotation().fromAngles(rx, ry, rz);
    }
    
    protected void setDegreeRotation(Vector3f r){
        this.getLocalRotation().fromAngles(r.x, r.y, r.z);
    }
    
    protected Vector3f getDegreeRotation(){
        float angles[] = this.getLocalRotation().toAngles(null);
        return new Vector3f(angles[0], angles[1], angles[2]);
    }
    
    public Vector3f getCalculatedVel() {
        return calculatedVel;
    }

    public void setCalculatedVel(Vector3f calculatedVel) {
        this.calculatedVel = calculatedVel;
    }
}
