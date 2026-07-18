package ir.ac.pvz.model.others;

import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.support.GridPosition;

public class TileConfiguration {

    public GridPosition position;
    public TileType type;
    public String containedPlantType;
    public String containedZombieType;

    public TileConfiguration(GridPosition position, TileType type) {
        this.position = position;
        this.type = type;
        this.containedPlantType = null;
        this.containedZombieType = null;
    }

    public boolean isAllowedFor(SeasonType season) {
        if (season == SeasonType.ANCIENT_EGYPT) {
            return type == TileType.EGYPT_GROUND || type == TileType.TOMBSTONE;
        }
        if (season == SeasonType.FROSTBITE_CAVES) {
            return type == TileType.FROSTBITE_GROUND
                    || type == TileType.SLIPPERY_UP
                    || type == TileType.SLIPPERY_DOWN
                    || type == TileType.FROZEN_TILE;
        }
        if (season == SeasonType.BIG_WAVE_BEACH) {
            return type == TileType.BEACH_GROUND || type == TileType.WATER
                    || type == TileType.LOW_TIDE;
        }
        return type == TileType.DARK_GROUND || type == TileType.TOMBSTONE
                || type == TileType.NECROMANCY;
    }
}
