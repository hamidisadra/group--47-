package ir.ac.pvz.controller.gui;

import ir.ac.pvz.controller.game_core.WaveController;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.others.Wave;

final class WaveNarration {
    private WaveNarration() {
    }

    static void announce(GameSession session, ChapterEffectsController effects,
                         int lastWaveSeen) {
        if (lastWaveSeen <= 0) {
            return;
        }

        WaveController waves = session.getWaveController();
        Wave wave = waves.getCurrentWave();

        if (wave == null) {
            return;
        }

        if (wave.isFinalWave) {
            effects.announce(waves.finalWaveMessage(), 2.6f);
            return;
        }

        effects.announce(waves.startWaveMessage(wave), 2.0f);
    }

    static float entryColumn(SessionController controller, int lane,
                             float spawnInset) {
        int tornadoColumn = controller.getChapterEffects().tornadoEntryColumn(
            controller.getChapter(), controller.board,
            isFinalWaveActive(controller.session), lane);

        if (tornadoColumn >= 0) {
            return tornadoColumn;
        }

        return controller.board.columns - spawnInset;
    }

    private static boolean isFinalWaveActive(GameSession session) {
        Wave wave = session.getWaveController().getCurrentWave();

        return wave != null && wave.isFinalWave;
    }

    static float progress(GameSession session) {
        int total = Math.max(1, session.getWaveController().getTotalWaves());
        int completed = Math.max(0, session.currentWaveNumber - 1);
        float withinWave = 0f;
        Wave wave = session.getWaveController().getCurrentWave();

        if (wave != null) {
            withinWave = Math.max(0f, Math.min(1f, wave.getHealthLostRatio()));
        }

        return Math.min(1f, (completed + withinWave) / total);
    }
}
