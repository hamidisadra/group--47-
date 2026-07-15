package ir.ac.pvz.model.user;

public class Pot {
    private int x;
    private int y;
    private boolean locked;
    private String plantType;
    private boolean marigold;
    private long plantedAt;
    private double growthHours;

    public Pot(int x, int y, boolean locked) {
        this.x = x;
        this.y = y;
        this.locked = locked;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean isEmpty() {
        return plantType == null;
    }

    public String getPlantType() {
        return plantType;
    }

    public boolean isMarigold() {
        return marigold;
    }

    public void plantSeed(String plantType, boolean marigold) {
        this.plantType = plantType;
        this.marigold = marigold;
        this.growthHours = marigold ? 2.0 : 8.0;
        this.plantedAt = System.currentTimeMillis();
    }

    public double getRemainingHours() {
        if (isEmpty()) {
            return 0;
        }
        double elapsedHours = (System.currentTimeMillis() - plantedAt) / 3600000.0;
        double remaining = growthHours - elapsedHours;
        return Math.max(0, remaining);
    }

    public boolean isReady() {
        return !isEmpty() && getRemainingHours() <= 0;
    }

    public void growInstantly() {
        this.plantedAt = System.currentTimeMillis() - (long) (growthHours * 3600000);
    }

    public HarvestResult harvest() {
        if (marigold) {
            plantType = null;
            return new HarvestResult(500, null);
        }
        String harvestedPlant = plantType;
        plantType = null;
        return new HarvestResult(0, harvestedPlant);
    }

    public PotStatus getStatus() {
        if (locked) {
            return PotStatus.LOCKED;
        }
        if (isEmpty()) {
            return PotStatus.EMPTY;
        }
        if (isReady()) {
            return PotStatus.READY;
        }
        return PotStatus.GROWING;
    }
}
