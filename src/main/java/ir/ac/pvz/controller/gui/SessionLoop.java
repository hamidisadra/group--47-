package ir.ac.pvz.controller.gui;

final class SessionLoop {
    void updateZomboss(SessionController controller, float scaled) {
        ZombossController zomboss = controller.getZomboss();

        if (zomboss == null) {
            return;
        }

        zomboss.update(scaled);

        if (zomboss.isDefeated()) {
            controller.session.win();
        }
    }

    void detectWaveChange(SessionController controller) {
        if (!controller.hasStartedWaves()
            || controller.session.currentWaveNumber == controller.lastWaveSeen) {
            return;
        }

        controller.lastWaveSeen = controller.session.currentWaveNumber;
        controller.getChapterEffects().onWaveStarted(controller.getChapter(),
            controller.board, controller.session, controller.lastWaveSeen);
        controller.announceWaveStart();
    }

    void advanceOneTick(SessionController controller) {
        if (controller.hasStartedWaves()) {
            advanceWaves(controller);
        }

        if (controller.usesConveyor()) {
            controller.conveyorTimer += SessionController.TICK_SECONDS;
            controller.refillConveyor();
        }

        updateMinigames(controller);
    }

    private void advanceWaves(SessionController controller) {
        WaveScheduler scheduler = controller.waveScheduler;

        controller.session.advanceTime(1);
        scheduler.harvestNewWave();
        scheduler.releaseIncoming(SessionController.TICK_SECONDS,
            controller.entryColumn(scheduler.nextLane()));
        scheduler.pushWaveOnTimer(SessionController.TICK_SECONDS);

        controller.updateStageRules();
        controller.recordSeenZombies();
        controller.scoreNotices.collect(controller.session);
    }

    private void updateMinigames(SessionController controller) {
        MinigameController minigames = controller.minigameController;

        if (minigames == null) {
            return;
        }

        minigames.expireHarvestedPlants(SessionController.TICK_SECONDS);
        minigames.advanceNuts(controller.board.rows, controller.board.columns);

        controller.minigameRuntime.resolveNutCollisions();
        controller.minigameRuntime.updateSunProducers(
            SessionController.TICK_SECONDS);
        controller.minigameRuntime.resolveBrains();

        if (minigames.isVasebreaker() && minigames.allVasesBroken()
            && controller.board.getAllAliveZombies().isEmpty()
            && !controller.waveScheduler.hasPending()) {
            controller.session.win();
        }
    }
}
