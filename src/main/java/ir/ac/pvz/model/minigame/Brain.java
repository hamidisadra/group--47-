package ir.ac.pvz.model.minigame;

public class Brain {
    private int row;
    private boolean eaten;

    public Brain(int row) {
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public boolean isEaten() {
        return eaten;
    }

    public void eat() {
        eaten = true;
    }
}
