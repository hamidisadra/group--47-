package ir.ac.pvz.model.support;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.enums.FallingSunType;

public class Sun extends GameObject {
    public FallingSunType type;
    public int amount;
    public GridPosition groundPosition;
    public float remainingFallSeconds;
    public boolean isFalling;
    public float height;
    public float verticalVelocity;
    public int zombieExplosionDamage;
    public int plantExplosionDamage;
    public int zombieExplosionRadius;
    public int plantExplosionRadius;
    public Sun(FallingSunType type, int amount, GridPosition groundPosition,
               float remainingFallSeconds, boolean isFalling) {
        super(groundPosition.x, groundPosition.y, 1);
        this.type = type;
        this.amount = amount;
        this.groundPosition = new GridPosition(groundPosition.x, groundPosition.y);
        this.remainingFallSeconds = remainingFallSeconds;
        this.isFalling = isFalling;
        this.height = 0f;
        this.verticalVelocity = 0f;
        if (isFalling) {
            this.height = 1f;
            if (remainingFallSeconds > 0f) {
                this.verticalVelocity = -height / remainingFallSeconds;
            }
        }
        this.zombieExplosionDamage = 150;
        this.plantExplosionDamage = 80;
        this.zombieExplosionRadius = 2;
        this.plantExplosionRadius = 1;
    }

    public void collect(GameSession session) {
        if (!isAlive || session == null) {
            return;
        }
        if (type == FallingSunType.RADIOACTIVE && isFalling) {
            explode(session.getBoard());
        }
        else {
            session.getSunManager().addSuns(amount);
        }
        die();
    }
    public void reachGround() {
        if (!isFalling) {
            return;
        }
        isFalling = false;
        remainingFallSeconds = 0f;
        height = 0f;
        verticalVelocity = 0f;
        if (type == FallingSunType.RADIOACTIVE) {
            convertToNormalAfterGround();
        }
        System.out.println("Sun reached the ground at position "
                + groundPosition.toUserString());
    }
    public void explode(Board board) {
        if (board == null) {
            return;
        }
        for (int y = groundPosition.y - zombieExplosionRadius;
             y <= groundPosition.y + zombieExplosionRadius; y++) {
            for (int x = groundPosition.x - zombieExplosionRadius;
                 x <= groundPosition.x + zombieExplosionRadius; x++) {
                Tile tile = board.getTile(new GridPosition(x, y));
                if (tile != null) {
                    tile.getZombies().forEach(zombie -> zombie.takeDamage(zombieExplosionDamage));
                }
            }
        }
        for (int y = groundPosition.y - plantExplosionRadius;
             y <= groundPosition.y + plantExplosionRadius; y++) {
            for (int x = groundPosition.x - plantExplosionRadius;
                 x <= groundPosition.x + plantExplosionRadius; x++) {
                Tile tile = board.getTile(new GridPosition(x, y));
                if (tile != null) {
                    tile.getPlants().forEach(plant -> plant.takeDamage(plantExplosionDamage));
                }
            }
        }
    }
    public void convertToNormalAfterGround() {
        type = FallingSunType.NORMAL;
        amount = 25;
    }
    @Override
    public void update(int tickCount) {
        if (!isFalling || !isAlive) {
            return;
        }
        float elapsed = tickCount / 10f;
        remainingFallSeconds -= elapsed;
        height = Math.max(0f, height + verticalVelocity * elapsed);
        if (remainingFallSeconds <= 0f) {
            reachGround();
        }
    }
    public boolean isCollected() {
        return !isAlive;
    }
    public FallingSunType getType() {
        return type;
    }
    public int getAmount() {
        return amount;
    }
    public GridPosition getGroundPosition() {
        return groundPosition;
    }
}
