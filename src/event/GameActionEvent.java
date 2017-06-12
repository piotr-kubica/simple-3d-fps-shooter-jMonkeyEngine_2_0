package event;

import game.GameObject;

/**
 *
 * @author dbow
 */
public class GameActionEvent {
    
    private GameObject eventSource;
    private GameObject paramObj;
//    private String optionalMessage;
    
    public GameActionEvent(GameObject eventSource, GameObject param) {
        this.eventSource = eventSource;
        this.paramObj = param;
    }

    public GameObject getEventSource() {
        return eventSource;
    }

    public GameObject getParamObj() {
        return paramObj;
    }
}
