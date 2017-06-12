package game;

import collision.CollisionSystem;
import collision.ICollidable;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;
import event.GameActionEvent;
import event.GameActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class GameObject extends Node implements ICollidable {
    
    protected static DisplaySystem display;

    private static final int DEFAULT_SIZE = 15;
    private float size;
    private static int Counter = 0;
    private LinkedList<ICollidable> collisions = new LinkedList<ICollidable>();
    public static CollisionSystem collisionSystem = CollisionSystem.getInstance();
    
    // event name is an String 
    // event name is key for list of observers
    private HashMap<String, HashSet<GameActionListener>> events 
            = new HashMap<String, HashSet<GameActionListener>>();
    
    public static void SetDisplay(DisplaySystem d) {
        GameObject.display = d;
    }
    
    public GameObject() {
        setName("GameObject#" + (Counter++));
        this.size = DEFAULT_SIZE;
        collisionSystem.addCollidable(this);
    }
    
    public GameObject(boolean addToCollisionSystem) {
        setName("GameObject#" + (Counter++));
        this.size = DEFAULT_SIZE;

        if(addToCollisionSystem){
            collisionSystem.addCollidable(this);
        }
    }
    
    public GameObject(float size, boolean addToCollisionSystem) {
        setName("GameObject#" + (Counter++));
        this.size = size;
        
        if(addToCollisionSystem){
            collisionSystem.addCollidable(this);
        }
    }
    
    protected boolean addObserver(String event, GameActionListener observer) {
        if(!events.containsKey(event)) {
            events.put(event, new HashSet<GameActionListener>());
        }
        observer.setListenerId(event);
        return events.get(event).add(observer);
    }
    
    protected boolean removeObserver(String event, GameActionListener observer) {
        if(!events.containsKey(event)) {
            return false;
        }
        return events.get(event).remove(observer);
    }
    
    protected boolean containsObserver(String event, GameActionListener observer) {
        if(!events.containsKey(event)) {
            return false;
        }
        return events.get(event).contains(observer);
    }
    
    protected final void dispatchEvent(String event, GameObject param) {
        if(events.get(event) == null) {
            return;
        }
        for(GameActionListener o: events.get(event)) {
            o.gameActionPerformed(new GameActionEvent(this, param));
        }
    }

    @Override
    public void addCollision(ICollidable c) {
        collisions.push(c);
    }

    @Override
    public void clearCollisions() {
        collisions.clear();
    }

    @Override
    public void processCollisions() {
        while(collisions.size() > 0) {
            this.onCollision(collisions.pop());
        }
    }

    @Override
    public void onCollision(ICollidable c) {
//        System.out.println(this.getName() + " collided with " + c.getName());
    }
    
    public void update(float timePerFrame) {
        preUpdate(timePerFrame);
        List<Spatial> ls = this.getChildren();

        if(ls != null) {
            for (int i = 0; i < ls.size(); i++) {
                if(ls.get(i) instanceof GameObject) {
                    ((GameObject)ls.get(i)).update(timePerFrame);
                }
            }
        }
        postUpdate(timePerFrame);
    }
    
    // override
    protected void preUpdate(float timePerFrame) {
        processCollisions();
    }

    // override
    protected void postUpdate(float timePerFrame) { }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public Vector3f getPosition() {
        return this.getLocalTranslation();
    }
    
    @Override
    public boolean equals(Object o) {
        if(this == o){
            return true;
        }
        if(!(o instanceof GameObject)) {
            return false;
        }
        GameObject go = (GameObject)o;
        return this.getName().equals(go.getName());
    }
    
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
}
