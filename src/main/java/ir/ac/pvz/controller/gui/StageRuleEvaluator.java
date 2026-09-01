package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.stage.DeadLineStage;
import ir.ac.pvz.model.stage.LoveYourPlantsStage;
import ir.ac.pvz.model.stage.SaveOurSeedsStage;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.stage.TimedWarStage;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;

import java.util.List;

final class StageRuleEvaluator {
    private StageRuleEvaluator() {
    }

    static void evaluate(Stage stage, Board board, GameSession session,
                         List<GridPosition> protectedTiles,
                         float elapsedSeconds) {
        if (stage instanceof TimedWarStage) {
            evaluateTimedWar((TimedWarStage) stage, session, elapsedSeconds);
        }

        if (stage instanceof LoveYourPlantsStage
            && ((LoveYourPlantsStage) stage).checkLoseCondition()) {
            session.lose();
            return;
        }

        if (stage instanceof DeadLineStage
            && crossedDeadLine((DeadLineStage) stage, board)) {
            session.lose();
            return;
        }

        if (stage instanceof SaveOurSeedsStage
            && lostProtectedPlant(board, protectedTiles)) {
            session.lose();
        }
    }

    private static void evaluateTimedWar(TimedWarStage timed,
                                         GameSession session,
                                         float elapsedSeconds) {
        if (((int) elapsedSeconds) != timed.getElapsedSeconds()) {
            timed.updateTimer(1);
        }

        if (timed.checkWinCondition()) {
            session.win();
        }

        else if (timed.checkLoseCondition()) {
            session.lose();
        }
    }

    private static boolean crossedDeadLine(DeadLineStage stage, Board board) {
        int column = stage.getDeadLineColumn();

        for (Zombie zombie : board.getAllAliveZombies()) {
            if (zombie.currentPosition.x <= column) {
                return true;
            }
        }

        return false;
    }

    private static boolean lostProtectedPlant(Board board,
                                              List<GridPosition> protectedTiles) {
        for (GridPosition position : protectedTiles) {
            if (board.getTile(position) != null
                && board.getTile(position).getPlant() == null) {
                return true;
            }
        }

        return false;
    }
}
