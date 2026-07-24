package com.pvz.game;

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
