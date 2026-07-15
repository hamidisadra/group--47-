package ir.ac.pvz.model.minigame;

public class PlantUpgrade {
    private String fromType;
    private String toType;
    private int sunCost;

    public PlantUpgrade(String fromType, String toType, int sunCost) {
        this.fromType = fromType;
        this.toType = toType;
        this.sunCost = sunCost;
    }

    public String getFromType() {
        return fromType;
    }

    public String getToType() {
        return toType;
    }

    public int getSunCost() {
        return sunCost;
    }

    public boolean canUpgrade(String plantType, int availableSun) {
        return fromType.equals(plantType) && availableSun >= sunCost;
    }
}
