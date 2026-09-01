package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.enums.GameStatus;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.others.GameStatistics;
import ir.ac.pvz.model.plants.ExplosivePlant;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.DodoFlightAbility;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.LawnMower;
import ir.ac.pvz.model.support.ProjectileResolver;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.model.support.ZombieAbility;
import ir.ac.pvz.model.zombies.FishermanZombie;
import ir.ac.pvz.model.zombies.KingZombie;

final class ZombieMovementResolver {
    private final GameSession session;
    private final Board board;
    private final GameStatistics statistics;
    private final ProjectileResolver projectileResolver;

    ZombieMovementResolver(GameSession session, Board board,
                           GameStatistics statistics,
                           ProjectileResolver projectileResolver) {
        this.session = session;
        this.board = board;
        this.statistics = statistics;
        this.projectileResolver = projectileResolver;
    }

    void moveZombie(Zombie zombie, float tickDurationSeconds) {
        if (zombie instanceof KingZombie) {
            return;
        }

        if (zombie instanceof FishermanZombie fisherman
                && fisherman.staysInRightmostColumn) {
            return;
        }

        zombie.move(tickDurationSeconds);
        relocateZombie(zombie);
    }

    void handleLawnMower(Zombie zombie) {
        if (zombie.isDead() || zombie.currentPosition.x >= 0f) {
            return;
        }

        LawnMower mower = board.getLawnMower(zombie.lane);

        if (mower == null) {
            return;
        }

        int before = mower.destroyedZombies.size();
        mower.handleZombieAtEnd(zombie, session);
        statistics.recordLawnMowerKills(
                Math.max(0, mower.destroyedZombies.size() - before));

        if (!zombie.isDead() && session.status == GameStatus.RUNNING) {
            board.placeZombie(zombie, new ContinuousPosition(0f, zombie.lane));
        }
    }

    void relocateZombie(Zombie zombie) {
        board.removeZombieEverywhere(zombie);

        int x = (int) Math.floor(zombie.currentPosition.x);
        GridPosition position = new GridPosition(x, zombie.lane);

        if (!board.isInside(position)) {
            return;
        }

        Tile tile = board.getTile(position);
        tile.addZombie(zombie);

        ExplosivePlant.resolveTrapContact(tile, zombie, session,
                projectileResolver);
        applySlipperyTile(zombie, tile, x);
    }

    private void applySlipperyTile(Zombie zombie, Tile tile, int x) {
        if (tile.slipDeltaRow == 0 || escapesBySlipperyFlight(zombie, x)) {
            return;
        }

        int targetLane = zombie.lane + tile.slipDeltaRow;

        if (targetLane < 0 || targetLane >= board.rows) {
            return;
        }

        tile.moveZombieBySlip(zombie);
        board.removeZombieEverywhere(zombie);
        board.getTile(new GridPosition(x, zombie.lane)).addZombie(zombie);
    }

    private boolean escapesBySlipperyFlight(Zombie zombie, int x) {
        for (ZombieAbility ability : zombie.abilities) {
            if (ability instanceof DodoFlightAbility) {
                return ((DodoFlightAbility) ability)
                        .escapeSlipperyTile(zombie, session, x);
            }
        }

        return false;
    }
}
