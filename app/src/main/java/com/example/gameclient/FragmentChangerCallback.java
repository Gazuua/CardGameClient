package com.example.gameclient;

public interface FragmentChangerCallback {
    public abstract void onEnterRoom();
    public abstract void onExitRoom();
    public abstract void onStartGame();
    public abstract void onEndGame();
}
