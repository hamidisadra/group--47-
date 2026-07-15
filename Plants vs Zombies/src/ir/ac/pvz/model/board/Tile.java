package ir.ac.pvz.model.board;

import java.util.ArrayList;
import java.util.List;

public abstract class Tile {
    protected Position position;
    protected boolean plantable;
    protected boolean passable;
    protected String plant;
    protected List<String> zombies;

    public Tile(Position position) {
        this.position = position;
        this.plantable = true;
        this.passable = true;
        this.plant = null;
        this.zombies = new ArrayList<>();
    }

    public Position getPosition() {
        return position;
    }

    public boolean isPassable() {
        return passable;
    }

    public boolean canPlant() {
        return plantable && plant == null;
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public void removePlant() {
        this.plant = null;
    }

    public List<String> getZombies() {
        return zombies;
    }

    public void addZombie(String zombieType) {
        zombies.add(zombieType);
    }

    public void removeZombie(String zombieType) {
        zombies.remove(zombieType);
    }

    public void showStatus() {
        System.out.println(getStatus());
    }

    public String getStatus() {
        String plantStatus = plant == null ? "empty" : plant;
        return "(" + (int) position.getX() + "," + (int) position.getY() + ") plant: " + plantStatus + ", zombies: " + zombies.size();
    }
}
