
package ai;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import game.Enemy;
import game.PhysicsObject;

public class RotateByAngleCommand implements ICommand {
    
    private PhysicsObject po;
    private float destAngle;
    private boolean positiveAngle;
    private Vector3f rotVel = new Vector3f();
    private boolean completed;
    
    public RotateByAngleCommand(PhysicsObject po, float angle, float rotationVelocity) {
        this.po = po;
        this.positiveAngle = angle >= 0;
        this.destAngle = po.getRotation().y + angle * FastMath.DEG_TO_RAD;
        this.rotVel.y = rotationVelocity;
        this.completed = false;
        
        if(positiveAngle) {
            this.rotVel.y = rotationVelocity;
            assert po.getRotation().y <= destAngle;
        } else {
            this.rotVel.y = -rotationVelocity;
            assert po.getRotation().y > destAngle;
        }
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
        if(((Enemy)po).getState() != Enemy.State.STAND) {
            ((Enemy)po).setState(Enemy.State.STAND);
        }
        
        if(positiveAngle) {
            if(po.getRotation().y < destAngle) {
                po.setRotVel(rotVel);
            } else {
                po.setRotVel(new Vector3f(0, 0, 0));
                completed = true;
            }
        } else {
            if(po.getRotation().y > destAngle) {
                po.setRotVel(rotVel);
            } else {
                po.setRotVel(new Vector3f(0, 0, 0));
                completed = true;
            }
        }
    }
    
    @Override
    public CommandType getCommandType() {
        return CommandType.ROTATE_COMMAND;
    }
}
