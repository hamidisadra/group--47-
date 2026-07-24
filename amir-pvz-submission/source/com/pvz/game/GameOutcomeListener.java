package com.pvz.game;

public interface GameOutcomeListener {
    void onGameWon(GameSession session);
    void onGameLost(GameSession session);
}
