package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import java.util.LinkedHashSet;
import java.util.Set;

public final class InMemoryUnlockRepository implements UnlockRepository {
    private final Set<String> plants = new LinkedHashSet<>();
    private final Set<String> stages = new LinkedHashSet<>();
    @Override
    public boolean isPlantUnlocked(String plantType) {
        return plants.contains(normalize(plantType));
    }
    @Override
    public boolean isStageUnlocked(String stageId) {
        return stages.contains(normalize(stageId));
    }
    @Override
    public boolean unlockPlant(String plantType) {
        String key = normalize(plantType);
        return !key.isEmpty() && plants.add(key);
    }
    @Override
    public boolean unlockStage(String stageId) {
        String key = normalize(stageId);
        return !key.isEmpty() && stages.add(key);
    }
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
