package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.stage.LoveYourPlantsStage;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.stage.TimedWarStage;

final class StageCounters {
    private int recordedKills;
    private int recordedSun;

    void sync(Stage stage, GameSession session) {
        int kills = session.getStatistics().getKilledZombies();
        int sun = session.getStatistics().getCollectedSun();
        int lost = session.getStatistics().getLostPlants();

        if (stage instanceof TimedWarStage) {
            syncTimedWar((TimedWarStage) stage, kills, sun);
        }

        if (stage instanceof LoveYourPlantsStage) {
            syncLostPlants((LoveYourPlantsStage) stage, lost);
        }

        recordedKills = kills;
        recordedSun = sun;
    }

    private void syncTimedWar(TimedWarStage timed, int kills, int sun) {
        for (int index = recordedKills; index < kills; index++) {
            timed.addKill();
        }

        if (sun > recordedSun) {
            timed.addSun(sun - recordedSun);
        }
    }

    private void syncLostPlants(LoveYourPlantsStage love, int lost) {
        for (int index = love.getLostCount(); index < lost; index++) {
            love.onPlantDestroyed();
        }
    }
}
