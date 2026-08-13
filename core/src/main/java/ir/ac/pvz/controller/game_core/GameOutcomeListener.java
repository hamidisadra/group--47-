package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

public interface GameOutcomeListener {
    void onGameWon(GameSession session);
    void onGameLost(GameSession session);
}
