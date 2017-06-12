package event;

/**
 *
 * @author dbow
 */
public abstract class GameActionListener {
    private static int Counter = 0;
    private String listenerId;
    
    public GameActionListener() {
        Counter++;
    }
    
    public abstract void gameActionPerformed(GameActionEvent e);
    
    public final String getListenerId() {
        return listenerId;
    }
    
    public final void setListenerId(String lid) {
        listenerId = lid + Counter;
    }
    
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GameActionListener)) {
            return false;
        }
        GameActionListener gal = (GameActionListener)o;
        return this.getListenerId().equals(gal.getListenerId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
