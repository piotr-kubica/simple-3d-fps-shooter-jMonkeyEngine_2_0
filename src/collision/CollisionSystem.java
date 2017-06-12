package collision;

import java.util.LinkedList;

public class CollisionSystem<T extends ICollidable> {

    private LinkedList<T> collidables;
    private static CollisionSystem instance;

    private CollisionSystem() {
        collidables = new LinkedList<T>();
    }

    public static CollisionSystem getInstance() {
        if (instance == null) {
            instance = new CollisionSystem();
        }
        return instance;
    }

    public boolean addCollidable(T c) {
        if (c == null) {
            throw new IllegalArgumentException("collidable cannot be null");
        }
        return collidables.add(c);
    }

    public boolean removeCollidable(T c) {
        if (c == null) {
            throw new IllegalArgumentException("parameter cannot be null");
        }
        return collidables.remove(c);
    }

    public boolean hasCollidable(T c) {
        if (c == null) {
            throw new IllegalArgumentException("parameter cannot be null");
        }
        return collidables.contains(c);
    }

    public void detectCollisions() {
        for (int i = 0; i < collidables.size(); i++) {
            T c1 = collidables.get(i);
            
            for (int j = i + 1; j < collidables.size(); j++) {
                T c2 = collidables.get(j);
                
                if(c1.getPosition().subtract(c2.getPosition()).length() 
                        <= c1.getSize() + c2.getSize()){
                    c1.addCollision(c2);
                    c2.addCollision(c1);
                }
            }
        }
    }
}
