package com.pvz.game;

public interface UnlockRepository extends UnlockPolicy {
    boolean unlockPlant(String plantType);
    boolean unlockStage(String stageId);
}
