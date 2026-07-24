package com.pvz.game;

import java.util.List;


public final class StageProgressionPolicy {
    private StageProgressionPolicy() {
    }
    public static void validateIncreasingDifficulty(List<StageConfig> stages) {
        if (stages == null || stages.isEmpty()) {
            throw new StageConfigurationException(
                    "A chapter must contain at least one stage.");
        }
        StageConfig previous = null;
        for (int index = 0; index < stages.size(); index++) {
            StageConfig current = stages.get(index);
            if (current == null) {
                throw new StageConfigurationException(
                        "Stage " + (index + 1) + " is missing.");
            }
            if (previous != null && !isHarderThan(current, previous)) {
                throw new StageConfigurationException("Stage " + (index + 1)
                        + " must increase wave count or wave costs.");
            }
            previous = current;
        }
    }
    public static boolean isHarderThan(StageConfig current,
                                       StageConfig previous) {
        if (current == null || previous == null) {
            return false;
        }
        List<Integer> currentCosts = current.calculateWaveCosts();
        List<Integer> previousCosts = previous.calculateWaveCosts();
        if (current.totalWaves > previous.totalWaves) {
            return true;
        }
        int sharedWaves = Math.min(currentCosts.size(), previousCosts.size());
        boolean strictlyHigher = false;
        for (int index = 0; index < sharedWaves; index++) {
            if (currentCosts.get(index) < previousCosts.get(index)) {
                return false;
            }
            strictlyHigher |= currentCosts.get(index) > previousCosts.get(index);
        }
        return current.totalWaves >= previous.totalWaves && strictlyHigher;
    }
}
