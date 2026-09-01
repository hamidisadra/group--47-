package ir.ac.pvz.model.support;

public class Tombstone extends TileObstacle {
    public static final String PLAIN = "PLAIN";
    public static final String SUN = "SUN";
    public static final String PLANT_FOOD = "PLANT_FOOD";
    public static final int STORED_SUN_AMOUNT = 50;

    public int initialHealth;
    private String variant;

    public Tombstone() {
        this(PLAIN);
    }

    public Tombstone(String variant) {
        super(700, true, false);
        this.initialHealth = 700;
        this.variant = variant == null ? PLAIN : variant;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        if (variant != null) {
            this.variant = variant;
        }
    }

    public int getStoredSun() {
        return SUN.equals(variant) ? STORED_SUN_AMOUNT : 0;
    }

    public boolean hasPlantFood() {
        return PLANT_FOOD.equals(variant);
    }

    public void turnIntoNormalGround(Tile tile) {
        if (tile == null) {
            return;
        }
        tile.obstacle = null;
        tile.restoreNativeGround();
    }
}
