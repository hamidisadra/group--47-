package ir.ac.pvz.model.minigame;

public class BowlingNut {
    private int row;
    private double col;
    private double direction;
    private BowlingNutType type;
    private boolean alive;
    private boolean exploded;
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

    public boolean hasExploded() {
        return exploded;
    }

    public void move(int rows) {
        if (!alive) {
            return;
        }

        col += Math.cos(Math.toRadians(direction));
        row += (int) Math.round(Math.sin(Math.toRadians(direction)));

        if (row < 0) {
            row = 0;
            onHitWall();
        }

        else if (row >= rows) {
            row = rows - 1;
            onHitWall();
        }
    }

    public void move() {
        move(Integer.MAX_VALUE);
    }

    public boolean isOffBoard(int columns) {
        return col < -1d || col > columns;
    }

    public void onHitZombie(String zombieType) {
        if (!alive) {
            return;
        }

        switch (type) {
            case EXPLODE_O_NUT:
                System.out.println("The nut explodes on " + zombieType
                        + ", damaging a 3x3 area.");
                exploded = true;
                alive = false;
                break;

            case GIANT:
                System.out.println("The giant nut squashes " + zombieType
                        + " and keeps rolling.");
                break;

            default:
                hitCount++;
                direction += hitCount == 1 ? 45 : 90;
                normalizeDirection();
                System.out.println("The nut hits " + zombieType
                        + " and changes direction.");
                break;
        }
    }

    public void onHitWall() {
        direction = -direction;
        normalizeDirection();
    }

    private void normalizeDirection() {
        direction %= 360d;

        if (direction < 0d) {
            direction += 360d;
        }
    }

    public void expire() {
        alive = false;
    }
}
