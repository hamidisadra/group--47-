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
        if (x < 1 || y < 1) {
            throw new IllegalArgumentException("Pot coordinates must be positive.");
        }
        this.x = x;
        this.y = y;
        this.locked = locked;
        resetPlantState();
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
        locked = false;
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
        if (locked) {
            throw new IllegalStateException("This pot is locked.");
        }
        if (!isEmpty()) {
            throw new IllegalStateException("This pot is already occupied.");
        }
        if (plantType == null || plantType.isBlank()) {
            throw new IllegalArgumentException("Plant type cannot be empty.");
        }
        this.plantType = plantType.trim();
        this.marigold = marigold;
        growthHours = marigold ? 2.0 : 8.0;
        plantedAt = System.currentTimeMillis();
    }

    public double getRemainingHours() {
        if (isEmpty()) {
            return 0;
        }
        double elapsedHours = (System.currentTimeMillis() - plantedAt) / 3600000.0;
        return Math.max(0, growthHours - elapsedHours);
    }

    public boolean isReady() {
        return !isEmpty() && getRemainingHours() <= 0;
    }

    public void growInstantly() {
        if (locked || isEmpty()) {
            throw new IllegalStateException("There is no growing plant here.");
        }
        if (isReady()) {
            return;
        }
        plantedAt = System.currentTimeMillis() - (long) Math.ceil(growthHours * 3600000.0);
    }

    public HarvestResult harvest() {
        if (locked || isEmpty()) {
            throw new IllegalStateException("There is nothing to collect here.");
        }
        if (!isReady()) {
            throw new IllegalStateException("This plant is not fully grown yet.");
        }
        HarvestResult result = marigold
                ? new HarvestResult(500, null)
                : new HarvestResult(0, plantType);
        resetPlantState();
        return result;
    }

    public PotStatus getStatus() {
        if (locked) {
            return PotStatus.LOCKED;
        }
        if (isEmpty()) {
            return PotStatus.EMPTY;
        }
        return isReady() ? PotStatus.READY : PotStatus.GROWING;
    }

    private void resetPlantState() {
        plantType = null;
        marigold = false;
        plantedAt = 0L;
        growthHours = 0.0;
    }
}
