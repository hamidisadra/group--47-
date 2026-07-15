package ir.ac.pvz.model.board;

public class WaterTile extends Tile {
    private boolean flooded;
    private boolean lilyPad;

    public WaterTile(Position position) {
        super(position);
        this.flooded = true;
        this.lilyPad = false;
        this.plantable = false;
    }

    public boolean isFlooded() {
        return flooded;
    }

    public void setFlooded(boolean flooded) {
        this.flooded = flooded;
    }

    public boolean hasLilyPad() {
        return lilyPad;
    }

    public void placeLilyPad() {
        this.lilyPad = true;
        this.plantable = true;
    }

    @Override
    public boolean canPlant() {
        return lilyPad && plant == null;
    }
}
