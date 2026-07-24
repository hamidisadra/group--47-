package ir.ac.pvz.model.support;

public class Tombstone extends TileObstacle {
    public int initialHealth;
    public Tombstone() {
        super(700, true, false);
        this.initialHealth = 700;
    }
    public void turnIntoNormalGround(Tile tile) {
        if (tile == null) {
            return;
        }
        tile.obstacle = null;
        tile.restoreNativeGround();
    }
}
