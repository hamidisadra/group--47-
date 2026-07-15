package ir.ac.pvz.model.board;

public class IceTile extends Tile {
    private String direction;

    public IceTile(Position position, String direction) {
        super(position);
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

    public void slideZombie(String zombieType) {
        System.out.println("Zombie " + zombieType + " slides towards " + direction + " on the ice.");
    }
}
