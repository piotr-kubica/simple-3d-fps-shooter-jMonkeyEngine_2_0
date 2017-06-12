package game;

import ai.ICommand;
import collision.ICollidable;
import com.jme.math.FastMath;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jmex.model.animation.KeyframeController;
import event.GameActionListener;
import java.util.EnumMap;
import java.util.LinkedList;

public class Enemy extends Figure {
    
    public enum State {
        STAND, RUN, DEATH
    };
    
    public enum AIEvent {
        SCARED, UNCARING, DEAD
    };
    
    public enum CollisionEvent {
        HIT_ENEMY, // by missile
        COLLIDED_ENEMY // with another enemy
    };
    
    private KeyframeController kc;
    private Node mesh;
    private EnumMap<State, AnimationTimeState> animationData;
    private State state;
    private AIEvent aiState = AIEvent.UNCARING;
    private Player player;
    private float scareDistanceThreshold = 250;
    private LinkedList<ICommand> commands = new LinkedList<ICommand>();
    
    public static float ENEMY_ROT_VEL = 0.05f;

    public Enemy(Node mesh, Player player, EnumMap<State, AnimationTimeState> animationData,
            boolean addToCollisionSystem, float offBound, float offGround) {
        super(addToCollisionSystem, offBound, offGround);
        this.mesh = mesh;
        this.player = player;
        this.kc = getKeyframeController(mesh);
        this.animationData = animationData;
        this.attachChild(mesh);
    }
    
    @Override
    public void onCollision(ICollidable c) {
        super.onCollision(c);
        
        if(c instanceof Enemy) {
            float angle = this.figureXZAngle((Enemy)c);
            
            if(Math.abs(angle) * FastMath.RAD_TO_DEG <= 60) {
                this.setPosition(this.getPosition().subtract(this.getCalculatedVel()));
            } 
        } else if(c instanceof Missile) {
            dispatchEvent(CollisionEvent.HIT_ENEMY.name(), (GameObject)c);
        }
    }
    
    public void update(float timePerFrame) {
        super.update(timePerFrame);
    }
    
    @Override
    protected void preUpdate(float timePerFrame) {
        super.preUpdate(timePerFrame);
        
        if(player == null) {
            return;
        }
        processAI();
        processCommands();
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
    
    public boolean addObserver(AIEvent event, GameActionListener observer) {
        return super.addObserver(event.name(), observer);
    }
    
    public boolean removeObserver(AIEvent event, GameActionListener observer) {
        return super.removeObserver(event.name(), observer);
    }
    
    public boolean containsObserver(AIEvent event, GameActionListener observer) {
        return super.containsObserver(event.name(), observer);
    }

    
    protected final KeyframeController getKeyframeController(Node animatedNode) {
        if(animatedNode == null) {
            System.out.println("node is null");
        }
        if(animatedNode.getChildren().isEmpty()) {
            System.out.println("ogre animation data is null");
            return null;
        }
        
        KeyframeController kc = (KeyframeController)animatedNode.getChild(0).getController(0);
        kc.setSpeed(10);
        kc.setRepeatType(Controller.RT_WRAP);
        kc.setActive(false);
        return kc;
    }
    
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        
        if(state == State.DEATH) {
            kc.setActive(false);
        } else {
            kc.setActive(true);
            AnimationTimeState ats = animationData.get(state);
            kc.setNewAnimationTimes(ats.getStartTime(), ats.getEndTime());
        }
    }
    
    public float getScareDistanceThreshold() {
        return scareDistanceThreshold;
    }

    public void setScareDistanceThreshold(float scareDistanceThreshold) {
        this.scareDistanceThreshold = scareDistanceThreshold;
    }
    
    public void addCommand(ICommand command) {
        commands.offer(command);
    }
    
    public void clearCommands() {
        commands.clear();
    }
    
    public int getCommandCnt() {
        return commands.size();
    }
    
    protected void processAI() {
        if(aiState == AIEvent.DEAD) {
            return;
        }
        boolean scareCondition = this.getPosition().distance(player.getPosition()) <= scareDistanceThreshold;
        
        // check scare condition
        if(scareCondition && (aiState != AIEvent.SCARED || commands.isEmpty()) ) {
            aiState = AIEvent.SCARED;
            dispatchEvent(AIEvent.SCARED.name(), player);
        } else if(commands.isEmpty()) {
            aiState = AIEvent.UNCARING;
            dispatchEvent(AIEvent.UNCARING.name(), player);
        }
    }
    
    protected void processCommands() {
        if(!commands.isEmpty()) {
            if(commands.peek().isCompleted()) {
                commands.poll();
            } else {
                commands.peek().process();
            }
        }
    }
    
    public void resetAI() {
        aiState = AIEvent.UNCARING;
    }
}