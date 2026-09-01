package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.support.GridPosition;

import java.util.List;

public final class CouchPlay {
    private final SessionController controller;

    private int cursorRow;
    private int cursorColumn;
    private int selectedCard;
    private String lastMessage;

    public CouchPlay(SessionController controller) {
        this.controller = controller;
        this.cursorColumn = controller.getBoard().columns - 1;
    }

    public int getCursorRow() {
        return cursorRow;
    }

    public int getCursorColumn() {
        return cursorColumn;
    }

    public int getSelectedCard() {
        return selectedCard;
    }

    public String pollMessage() {
        String message = lastMessage;
        lastMessage = null;

        return message;
    }

    public List<String> cards() {
        return controller.zombieCardTypes();
    }

    public String selectedCardType() {
        List<String> cards = cards();

        if (cards.isEmpty()) {
            return null;
        }

        return cards.get(Math.floorMod(selectedCard, cards.size()));
    }

    public void moveCursor(int deltaColumn, int deltaRow) {
        int columns = controller.getBoard().columns;
        int rows = controller.getBoard().rows;
        int minimumColumn = SessionController.IZOMBIE_PLANT_COLUMNS;

        cursorColumn = clamp(cursorColumn + deltaColumn, minimumColumn,
            columns - 1);
        cursorRow = clamp(cursorRow + deltaRow, 0, rows - 1);
    }

    public void selectCard(int index) {
        List<String> cards = cards();

        if (!cards.isEmpty() && index >= 0 && index < cards.size()) {
            selectedCard = index;
        }
    }

    public void cycleCard(int delta) {
        List<String> cards = cards();

        if (!cards.isEmpty()) {
            selectedCard = Math.floorMod(selectedCard + delta, cards.size());
        }
    }

    public boolean placeSelected() {
        String type = selectedCardType();

        if (type == null) {
            return false;
        }

        boolean placed = controller.placeZombieCard(type,
            new GridPosition(cursorColumn, cursorRow));

        lastMessage = placed ? null : "Not enough sun for " + type + ".";

        return placed;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
