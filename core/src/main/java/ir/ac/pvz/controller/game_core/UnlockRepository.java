package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

public interface UnlockRepository extends UnlockPolicy {
    boolean unlockPlant(String plantType);
    boolean unlockStage(String stageId);
}
