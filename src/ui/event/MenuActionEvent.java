package ui.event;

public class MenuActionEvent {
    private int position;

    public MenuActionEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
