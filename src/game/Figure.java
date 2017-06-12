package game;

import collision.ICollidable;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import util.LimitHelper;

public class Figure extends PhysicsObject {
    protected float offBound = 0; // margin to x,z positive and negative bounds
    protected float offGround = 0; // level over ground (value on y axis)
    
    public Figure() {
        super();
    }
    
    public Figure(boolean addToCollisionSystem, float offBound, float offGround) {
        super(addToCollisionSystem);
        this.offBound = offBound;
        this.offGround = offGround;
    }
    
    public Figure(float size, boolean addToCollisionSystem, float offBound, float offGround) {
        super(size, addToCollisionSystem);
        this.offBound = offBound;
        this.offGround = offGround;
    }
    
    @Override
    public void onCollision(ICollidable c) {
        if(c instanceof Terrain) {
            
            Terrain t = (Terrain)c;
            Vector3f max = new Vector3f(t.maxBoundingWorldVertex().x - offBound, 0,
                                        t.maxBoundingWorldVertex().z - offBound);
            Vector3f min = new Vector3f(t.minBoundingWorldVertex().x + offBound, 0, 
                                        t.minBoundingWorldVertex().z + offBound);
            position = LimitHelper.LimitMax(position, max);
            position = LimitHelper.LimitMin(position, min);
            position.y = t.getHeight(position.x, position.z) + offGround;
        }
    }
    
    // angle between facing direction and direction to another figure
    public float figureXZAngle(Figure f) {
        Vector3f rotA = this.getWorldRotation().getRotationColumn(2);
        Vector3f dirToFigure = this.getLocalTranslation().subtract(f.getLocalTranslation());
        dirToFigure.normalizeLocal();
        rotA.y = 0;
        rotA.normalizeLocal();
        Vector3f orthoRotA = new Vector3f(rotA.z, 0, rotA.x);
        return Math.abs(rotA.angleBetween(dirToFigure) - (float)Math.PI) * FastMath.sign(orthoRotA.dot(dirToFigure));
    }
}
