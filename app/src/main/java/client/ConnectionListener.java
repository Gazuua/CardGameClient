package client;

public interface ConnectionListener {
    public abstract void onSuccess();
    public abstract void onFail();
}
