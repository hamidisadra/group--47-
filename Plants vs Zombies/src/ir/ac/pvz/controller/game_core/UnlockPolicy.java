package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

public interface UnlockPolicy {
    boolean isPlantUnlocked(String plantType);
    boolean isStageUnlocked(String stageId);
    static UnlockPolicy allUnlocked() {
        return new UnlockPolicy() {
            @Override
            public boolean isPlantUnlocked(String plantType) {
                return true;
            }
            @Override
            public boolean isStageUnlocked(String stageId) {
                return true;
            }
        };
    }
}
