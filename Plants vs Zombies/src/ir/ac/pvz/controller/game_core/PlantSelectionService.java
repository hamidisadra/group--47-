package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.support.PlantDataRepository;
import ir.ac.pvz.model.support.PlantDefinition;
import ir.ac.pvz.model.support.PlantDefinitionRepository;
import java.util.ArrayList;
import java.util.List;

public final class PlantSelectionService {
    private final StageConfig stage;
    private final UnlockPolicy unlocks;
    private final PlantDefinitionRepository plantRepository;
    public PlantSelectionService(StageConfig stage, UnlockPolicy unlocks) {
        this(stage, unlocks, PlantDataRepository.getInstance());
    }
    public PlantSelectionService(
            StageConfig stage, UnlockPolicy unlocks,
            PlantDefinitionRepository plantRepository) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage configuration is required.");
        }
        if (plantRepository == null) {
            throw new IllegalArgumentException("Plant repository is required.");
        }
        this.stage = stage;
        this.plantRepository = plantRepository;
        if (unlocks == null) {
            this.unlocks = UnlockPolicy.allUnlocked();
        } else {
            this.unlocks = unlocks;
        }
    }
    public List<String> availablePlants() {
        List<String> result = new ArrayList<>();
        for (String type : Plant.getSpreadsheetTypes()) {
            if (unlocks.isPlantUnlocked(type)) {
                result.add(type);
            }
        }
        return result;
    }
    public boolean select(String plantType) {
        String canonical = canonicalType(plantType);
        if (canonical == null || !unlocks.isPlantUnlocked(canonical)
                || stage.isPlantSelected(canonical)) {
            return false;
        }
        stage.selectedPlantTypes.add(canonical);
        return true;
    }
    public boolean deselect(String plantType) {
        String canonical = canonicalType(plantType);
        if (canonical == null) {
            return false;
        }
        stage.boostedPlantTypes.removeIf(type -> sameType(type, canonical));
        return stage.selectedPlantTypes.removeIf(type -> sameType(type, canonical));
    }
    public boolean setBoosted(String plantType, boolean boosted) {
        String canonical = canonicalType(plantType);
        if (canonical == null || !stage.isPlantSelected(canonical)) {
            return false;
        }
        stage.boostedPlantTypes.removeIf(type -> sameType(type, canonical));
        if (boosted) {
            stage.boostedPlantTypes.add(canonical);
        }
        return true;
    }
    public List<String> selectedPlants() {
        return new ArrayList<>(stage.selectedPlantTypes);
    }
    private String canonicalType(String requested) {
        PlantDefinition definition = plantRepository.get(requested);
        if (definition == null) {
            return null;
        }
        return definition.name;
    }
    private boolean sameType(String first, String second) {
        return normalize(first).equals(normalize(second));
    }
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
