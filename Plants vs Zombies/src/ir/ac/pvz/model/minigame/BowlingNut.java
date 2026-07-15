package ir.ac.pvz.model.minigame;

public class BowlingNut {
    private int row;
    private double col;
    private double direction;
    private BowlingNutType type;
    private boolean alive;
    private int hitCount;

    public BowlingNut(int row, double col, BowlingNutType type) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.direction = 0;
        this.alive = true;
    }

    public int getRow() {
        return row;
    }

    public double getCol() {
        return col;
    }

    public BowlingNutType getType() {
        return type;
    }

    public boolean isAlive() {
        return alive;
    }

    public void move() {
        if (!alive) {
            return;
        }
        col += Math.cos(Math.toRadians(direction));
        row += (int) Math.round(Math.sin(Math.toRadians(direction)));
    }

    public void onHitZombie(String zombieType) {
        if (!alive) {
            return;
        }
        switch (type) {
            case EXPLODE_O_NUT:
                System.out.println("The nut explodes on " + zombieType + ", damaging a 3x3 area.");
                alive = false;
                break;
            case GIANT:
                System.out.println("The giant nut squashes " + zombieType + " and keeps rolling.");
                break;
            default:
                hitCount++;
                direction += hitCount == 1 ? 45 : 90;
                System.out.println("The nut hits " + zombieType + " and changes direction.");
                break;
        }
    }

    public void onHitWall() {
        direction = -direction;
    }
}
