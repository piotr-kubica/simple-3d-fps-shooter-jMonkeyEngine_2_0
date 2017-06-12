package ai;

public interface ICommand {
    public boolean isCompleted();
    public void process();
    public CommandType getCommandType();
}
