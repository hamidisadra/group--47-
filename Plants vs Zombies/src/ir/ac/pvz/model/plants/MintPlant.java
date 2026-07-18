package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.support.Board;

public class MintPlant extends Plant {

    public float durationSeconds;
    public PlantCategory familyCategory;

    public MintPlant(int id, String name, float rechargeTime, float durationSeconds) {
        this(id, name, rechargeTime, durationSeconds, PlantCategory.MINT);
    }

    public MintPlant(int id, String name, float rechargeTime, float durationSeconds,
                     PlantCategory familyCategory) {
        super(id, name, 0, 0, rechargeTime, 0f, 0, PlantCategory.MINT);
        this.durationSeconds = durationSeconds;
        this.familyCategory = familyCategory;
        lifeSpanSeconds = Math.max(0f, durationSeconds);
    }

    public void applyFamilyPlantFoodEffect(Board board) {
        if (board == null) {
            return;
        }
        for (int row = 0; row < board.rows; row++) {
            for (Plant plant : board.getPlantsInLane(row)) {
                if (plant != this && plant.category == familyCategory) {
                    plant.applyPlantFoodEffect();
                }
            }
        }
    }

    public void disappearAfterDuration() {
        die();
    }
}
