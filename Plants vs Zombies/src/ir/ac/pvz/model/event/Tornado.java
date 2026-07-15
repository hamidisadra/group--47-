package ir.ac.pvz.model.event;

public class Tornado {
    private int lane;

    public Tornado(int lane) {
        this.lane = lane;
    }

    public int getLane() {
        return lane;
    }

    public void spawn() {
        System.out.println("A tornado is spawned in lane " + lane + ".");
    }

    public void moveZombie(String zombieType, int fromColumn) {
        int newColumn = Math.max(1, fromColumn - (1 + (int) (Math.random() * 4)));
        System.out.println("Tornado moved " + zombieType + " from column " + fromColumn + " to column " + newColumn + ".");
    }
}
