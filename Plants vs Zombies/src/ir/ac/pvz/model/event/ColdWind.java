package ir.ac.pvz.model.event;

public class ColdWind {
    private int[] affectedRows;

    public ColdWind(int[] affectedRows) {
        this.affectedRows = affectedRows;
    }

    public int[] getAffectedRows() {
        return affectedRows;
    }

    public void apply() {
        StringBuilder rows = new StringBuilder();
        for (int row : affectedRows) {
            rows.append(row).append(" ");
        }
        System.out.println("A cold wind blows through rows " + rows.toString().trim() + ", increasing the ice level of the tiles.");
    }
}
