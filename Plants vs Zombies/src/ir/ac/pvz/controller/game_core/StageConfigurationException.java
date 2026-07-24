package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

public class StageConfigurationException extends IllegalStateException {
    private static final long serialVersionUID = 1L;
    public StageConfigurationException(String message) {
        super(message);
    }
}
