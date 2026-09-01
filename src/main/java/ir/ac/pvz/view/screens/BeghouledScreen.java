package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.model.minigame.Beghouled;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Modal;
import ir.ac.pvz.view.ui.PamActor;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;
import ir.ac.pvz.view.assets.GameAssets;

public class BeghouledScreen extends BaseMenuScreen {
    private static final int TARGET_MATCHES = 12;

    private final Beghouled puzzle;
    private final Table grid = new Table();
    private Label statusLabel;
    private ProgressBar progress;
    private static final float CELL_SIZE = 84f;

    private int dragColumn = -1;
    private int dragRow = -1;
    private int selectedColumn = -1;
    private int selectedRow = -1;
    private boolean finished;

    public BeghouledScreen(PvzGame pvzGame) {
        super(pvzGame, "Beghouled", true);
        puzzle = new Beghouled(1, TARGET_MATCHES);
        puzzle.startGame();

        statusLabel = Ui.label("", "medium");
        progress = new ProgressBar(0f, 1f, 0.01f, false, Ui.skin(),
            Ui.skin().has("xp_green", ProgressBar.ProgressBarStyle.class)
                ? "xp_green" : "default-horizontal");

        Table header = new Table();
        header.add(statusLabel).padRight(20f);
        header.add(progress).width(320f);
        header.add(upgradeButton()).padLeft(20f);
        content.add(header).padBottom(10f).row();

        Table panel = new Table();
        panel.setBackground(Ui.panel());
        panel.pad(12f);
        panel.add(grid);
        content.add(panel).center().row();

        rebuild();
    }

    private Actor upgradeButton() {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button =
            Ui.button("Upgrade a plant (200 sun)", "purple");
        button.getLabel().setFontScale(0.72f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String[][] board = puzzle.getGrid();
                String type = board[0][0];
                if (puzzle.upgradePlant(type)) {
                    Toast.info(stage, type + " upgraded.");
                    rebuild();
                }

                else {
                    Toast.error(stage, "Not enough sun to upgrade.");
                }
            }
        });
        return button;
    }

    private void rebuild() {
        grid.clear();
        String[][] board = puzzle.getGrid();
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                grid.add(cell(column, row, board[row][column])).size(84f).pad(2f);
            }

            grid.row();
        }

        statusLabel.setText("Sun " + puzzle.getSunAmount() + "    Matches "
            + puzzle.getMatchCount() + " / " + puzzle.getTargetMatches()
            + "    Moves left " + puzzle.countPossibleMoves());
        progress.setValue(Math.min(1f,
            puzzle.getMatchCount() / (float) puzzle.getTargetMatches()));
        checkOutcome();
    }

    private Table cell(int column, int row, String plantType) {
        Table cell = new Table();
        boolean selected = column == selectedColumn && row == selectedRow;
        cell.setBackground(selected
            ? Ui.solid(new com.badlogic.gdx.graphics.Color(1f, 0.9f, 0.4f, 0.55f))
            : Ui.solid(new com.badlogic.gdx.graphics.Color(0f, 0f, 0f, 0.30f)));

        if (puzzle.isCrater(column + 1, row + 1)) {
            cell.add(Ui.label("x", "medium"));
            return cell;
        }

        if (plantType != null) {
            PamActor art = new PamActor(66f);
            art.setAnimation(GameAssets.get().plantPath(plantType), "idle");
            cell.add(art).size(66f);
        }

        cell.addListener(cellGestures(column, row));
        return cell;
    }

    private ClickListener cellGestures(int column, int row) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onCellClicked(column, row);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                dragColumn = column;
                dragRow = row;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                handleDrag(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                dragColumn = -1;
                dragRow = -1;
                super.touchUp(event, x, y, pointer, button);
            }
        };
    }

    private void handleDrag(float x, float y) {
        if (dragColumn < 0) {
            return;
        }

        int stepX = x > CELL_SIZE ? 1 : x < 0f ? -1 : 0;
        int stepY = y > CELL_SIZE ? -1 : y < 0f ? 1 : 0;

        if (stepX != 0 && stepY != 0 || stepX == 0 && stepY == 0) {
            return;
        }

        int startColumn = dragColumn;
        int startRow = dragRow;
        dragColumn = -1;
        dragRow = -1;
        trySwap(startColumn, startRow, startColumn + stepX, startRow + stepY);
    }

    private void onCellClicked(int column, int row) {
        if (finished) {
            return;
        }

        if (selectedColumn < 0) {
            selectedColumn = column;
            selectedRow = row;
            rebuild();
            return;
        }

        boolean adjacent = Math.abs(selectedColumn - column) + Math.abs(selectedRow - row) == 1;
        if (!adjacent) {
            selectedColumn = column;
            selectedRow = row;
            rebuild();
            return;
        }

        int fromColumn = selectedColumn;
        int fromRow = selectedRow;
        selectedColumn = -1;
        selectedRow = -1;
        trySwap(fromColumn, fromRow, column, row);
    }

    private void trySwap(int fromColumn, int fromRow, int toColumn, int toRow) {
        if (finished || toColumn < 0 || toRow < 0
            || toColumn >= puzzle.getGrid()[0].length
            || toRow >= puzzle.getGrid().length) {
            return;
        }

        boolean swapped = puzzle.swapPlants(fromColumn + 1, fromRow + 1,
            toColumn + 1, toRow + 1);

        if (!swapped) {
            Toast.error(stage, "That swap does not make a match.");
            rebuild();
            return;
        }

        puzzle.applyGravity();
        puzzle.fillEmpty();
        puzzle.reshuffleIfStuck();
        rebuild();
        animateBoard();
    }

    private void animateBoard() {
        for (com.badlogic.gdx.scenes.scene2d.Actor actor : grid.getChildren()) {
            actor.getColor().a = 0.25f;
            actor.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.22f));
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (!finished) {
            puzzle.advanceTime(1);
        }
    }

    private void checkOutcome() {
        if (finished) {
            return;
        }

        if (puzzle.checkWinCondition()) {
            finished = true;
            showOutcome(true);
        }

        else if (puzzle.isLost()) {
            finished = true;
            showOutcome(false);
        }
    }

    private void showOutcome(boolean won) {
        Modal modal = new Modal(won ? "You Win!" : "You Lost");
        modal.message(won
            ? "You made enough matches to hold the lawn."
            : "The zombies got through.");
        if (!won) {
            modal.action("Retry", "green", () -> {
                modal.close();
                game.replace(ScreenId.MINIGAMES);
            });
        }

        modal.action("Exit", "brown", () -> {
            modal.close();
            game.navigate(ScreenId.MINIGAMES);
        });
        modal.show(stage);
    }

}
