package game;

import collision.ICollidable;
import com.jme.input.InputHandler;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import event.GameActionListener;
import input.FPPInputHandler;

public class Player extends Figure implements IMovable {
    
    public enum CollisionEvent {
        COLLIDED_PLAYER // with enemy
    };
    
    FPPInputHandler inputHandler;

    public Player(float size, boolean addToCollisionSystem, float offBound, float offGround,
                  Camera cam, InputHandler input, float sensitivity, float pitchLimit) {
        super(size, addToCollisionSystem, offBound, offGround);
        input = inputHandler = new FPPInputHandler(cam, this, sensitivity, pitchLimit);
    }
    
    public FPPInputHandler getInputHandler() {
        return inputHandler;
    }
    
    public boolean addObserver(CollisionEvent event, GameActionListener observer) {
        return super.addObserver(event.name(), observer);
    }
    
    public boolean removeObserver(CollisionEvent event, GameActionListener observer) {
        return super.removeObserver(event.name(), observer);
    }
    
    public boolean containsObserver(CollisionEvent event, GameActionListener observer) {
        return super.containsObserver(event.name(), observer);
    }
    
    @Override
    public void onCollision(ICollidable c) {
        super.onCollision(c);
        
        if(c instanceof Enemy) {
            if(Math.abs(this.figureXZAngle((Enemy)c) * FastMath.RAD_TO_DEG) < 60) {
                position = inputHandler.getLastPosition();
                
            }
        }
    }
    
    @Override
    protected void applyPosition() {
        super.applyPosition();
        inputHandler.getCamera().getLocation().set(getPosition());
    }
    
    public Vector3f getHeadingDirection() {
        return inputHandler.getDirection();
    }
    
    @Override
    public float figureXZAngle(Figure f) {
        Vector3f rotA = this.getHeadingDirection();
        rotA.y = 0;
        rotA.normalizeLocal();
        Vector3f dirToFigure = this.getPosition().subtract(f.getLocalTranslation());
        dirToFigure.y = 0;
        dirToFigure.normalizeLocal();
        Vector3f orthoRotA = new Vector3f(-rotA.z, 0, rotA.x);
        return Math.abs(rotA.angleBetween(dirToFigure) - (float)Math.PI) * FastMath.sign(orthoRotA.dot(dirToFigure));
    }
}
