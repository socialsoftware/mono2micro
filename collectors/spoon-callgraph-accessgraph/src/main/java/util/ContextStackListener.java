package util;

public interface ContextStackListener {
    public void contextAdded(int index);
    public void contextClosed(int index);
}
