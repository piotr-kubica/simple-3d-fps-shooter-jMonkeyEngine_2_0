package ai;

import com.jme.math.Vector3f;
import game.Enemy;
import game.PhysicsObject;

public class MoveCommand implements ICommand {
    
    private PhysicsObject po;
    private float iterations;
    private float velocity;
    private boolean completed;
    
    public MoveCommand(PhysicsObject po, float iterations, float velocity) {
        this.po = po;
        this.iterations = iterations;
        this.velocity = velocity;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void process() {
        if(completed) {
            return;
        }
        if(((Enemy)po).getState() != Enemy.State.RUN) {
            ((Enemy)po).setState(Enemy.State.RUN);
        }
        
        if(iterations-- > 0) {
            po.setLinVel(velocity);
        } else {
            po.setLinVel(0);
            completed = true;
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.MOVE_COMMAND;
    }
}
