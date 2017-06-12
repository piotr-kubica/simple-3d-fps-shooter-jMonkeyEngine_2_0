package input;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.KeyInputAction;
import com.jme.input.action.MouseInputAction;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.system.JmeException;
import game.GameObject;
import game.IMovable;

public final class FPPInputHandler extends InputHandler {

    enum MovementDirection {
        FORWARD, BACK, LEFT, RIGHT
    };
    
    private float pitchLimitRad;
    private float sensitivity = 0.01f;
    private static final Matrix3f camTransform = new Matrix3f();
    private Camera cam;
    private IMovable movable;
    private float stepSpeed = 100f;
    protected Vector3f lastPosition = new Vector3f();

    public Vector3f getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(Vector3f lastPosition) {
        this.lastPosition = lastPosition;
    }

    public FPPInputHandler(Camera cam, IMovable movable, float sensitivity, float pitchLimit) {
        super();

        if (cam == null) {
            throw new JmeException(cam.getClass() + " cam parameter cannot be null");
        }
        this.cam = cam;
        this.sensitivity = sensitivity;
        this.pitchLimitRad = (float) (pitchLimit * Math.PI / 180.0);
        this.movable = movable;

        // init mouse handler
        InputHandler mouse = new InputHandler();
        MouseInputAction look = createMouseAction();
        mouse.addAction(look, InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_NONE, 0, true);
        this.addToAttachedHandlers(mouse);

        // init keyboard handler
        InputHandler keyboard = new InputHandler();
        KeyBindingManager keyManager = KeyBindingManager.getKeyBindingManager();
        keyManager.set(MovementDirection.FORWARD.name(), KeyInput.KEY_W);
        keyManager.set(MovementDirection.BACK.name(), KeyInput.KEY_S);
        keyManager.set(MovementDirection.LEFT.name(), KeyInput.KEY_A);
        keyManager.set(MovementDirection.RIGHT.name(), KeyInput.KEY_D);
        boolean repeat = true;
        keyboard.addAction(createMoveInDirectionKeyInputAction(), MovementDirection.FORWARD.name(), repeat);
        keyboard.addAction(createMoveInDirectionKeyInputAction(), MovementDirection.BACK.name(), repeat);
        keyboard.addAction(createMoveInDirectionKeyInputAction(), MovementDirection.LEFT.name(), repeat);
        keyboard.addAction(createMoveInDirectionKeyInputAction(), MovementDirection.RIGHT.name(), repeat);
        this.addToAttachedHandlers(keyboard);
    }

    public Camera getCamera() {
        return cam;
    }

    public Vector3f getDirection() {
        Vector3f dir = cam.getDirection().clone();
        dir.normalizeLocal();
        return dir;
    }

    protected Vector3f getY0Left() {
        Vector3f left = cam.getLeft().clone();
        left.y = 0;
        left.normalizeLocal();
        return left;
    }

    protected Vector3f getY0Dir() {
        Vector3f dir = cam.getDirection().clone();
        dir.y = 0;
        dir.normalizeLocal();
        return dir;
    }

    protected KeyInputAction createMoveInDirectionKeyInputAction() {

        KeyInputAction kia = new KeyInputAction() {

            public Vector3f getDirection(String triggerName) {
                Vector3f dir = null;

                if (triggerName.equals(MovementDirection.FORWARD.name())) {
                    dir = getY0Dir().mult(stepSpeed);
                } else if (triggerName.equals(MovementDirection.BACK.name())) {
                    dir = getY0Dir().mult(-stepSpeed);
                } else if (triggerName.equals(MovementDirection.LEFT.name())) {
                    dir = getY0Left().mult(stepSpeed);
                } else if (triggerName.equals(MovementDirection.RIGHT.name())) {
                    dir = getY0Left().mult(-stepSpeed);
                }
                return dir;
            }

            @Override
            public void performAction(InputActionEvent iae) {
                synchronized (GameObject.class) {
                    Vector3f dir = getDirection(iae.getTriggerName());

                    if (dir == null) {
                        return;
                    }
                    dir.multLocal(iae.getTime());
                    lastPosition = cam.getLocation();
                    movable.setPosition(cam.getLocation().add(dir));
                }
            }
        };
        return kia;
    }

    protected void updateCameraRotation(float pitchAngle, float yawAngle) {
        Vector3f left = cam.getLeft();
        Vector3f dir = cam.getDirection();
        Vector3f up = cam.getUp();

        // pitch (updates camera references up, left and dir)
        camTransform.fromAngleNormalAxis(pitchAngle, left);
        camTransform.mult(left, left);
        camTransform.mult(dir, dir);
        camTransform.mult(up, up);
//        cam.normalize();

        // yaw (updates camera references up, left and dir)
        camTransform.fromAngleNormalAxis(yawAngle, Vector3f.UNIT_Y);
        camTransform.mult(up, up);
        camTransform.mult(left, left);
        camTransform.mult(dir, dir);
        cam.normalize();
        cam.update();
    }

    protected MouseInputAction createMouseAction() {

        // private anonymous class
        MouseInputAction look = new MouseInputAction() {

            // delta rotation angles
            private float drx;
            private float dry;
            // rotation angles
            private float rx;
            private float ry;

            protected void updateRotation() {
                drx = MouseInput.get().getYDelta() * sensitivity;
                dry = MouseInput.get().getXDelta() * sensitivity;
                rx += drx;
                ry += dry;

                if (rx > pitchLimitRad) {
                    drx = 0;
                    rx = pitchLimitRad;
                } else if (rx < -pitchLimitRad) {
                    drx = 0;
                    rx = -pitchLimitRad;
                }
            }

            @Override
            public void performAction(InputActionEvent iae) {
                synchronized (GameObject.class) {
                    this.updateRotation();
                    updateCameraRotation(-drx, -dry);
                }
            }
        };
        return look;
    }
}
