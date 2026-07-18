package ir.ac.pvz.model.others;

import ir.ac.pvz.controller.game_core.StageConfigurationException;
import ir.ac.pvz.controller.game_core.ZombieSpawner;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.plants.PlantFactory;
import ir.ac.pvz.model.support.Board;

import java.util.ArrayList;
import java.util.List;

public class GameBootstrapConfig {

    public int rows;
    public int columns;
    public int startingSun;
    public StageConfig stageConfig;
    public List<TileConfiguration> tileConfigurations;

    public GameBootstrapConfig(int rows, int columns, int startingSun,
                               StageConfig stageConfig) {
        this(rows, columns, startingSun, stageConfig, new ArrayList<>());
    }

    public GameBootstrapConfig(int rows, int columns, int startingSun,
                               StageConfig stageConfig,
                               List<TileConfiguration> tileConfigurations) {
        this.rows = rows;
        this.columns = columns;
        this.startingSun = startingSun;
        this.stageConfig = stageConfig;
        this.tileConfigurations = tileConfigurations == null
                ? new ArrayList<>() : new ArrayList<>(tileConfigurations);
    }

    public Board createBoard() {
        Board board = new Board(rows, columns, stageConfig.seasonType);
        for (TileConfiguration configuration : tileConfigurations) {
            configureTerrain(board, configuration);
        }
        configureFrozenContents(board);
        return board;
    }

    private void configureTerrain(Board board,
                                  TileConfiguration configuration) {
        if (!configuration.isAllowedFor(stageConfig.seasonType)) {
            throw new StageConfigurationException("Tile "
                    + configuration.type + " is not valid for "
                    + stageConfig.seasonType + ".");
        }
        if (!board.configureTile(configuration.position, configuration.type)) {
            throw new StageConfigurationException(
                    "Invalid tile configuration at " + configuration.position + ".");
        }
    }

    private void configureFrozenContents(Board board) {
        ZombieSpawner spawner = new ZombieSpawner(board, stageConfig);
        int plantId = -1;
        for (TileConfiguration configuration : tileConfigurations) {
            if (configuration.containedPlantType != null) {
                Plant plant = PlantFactory.create(plantId--,
                        configuration.containedPlantType);
                if (plant == null || !board.configureFrozenPlant(
                        configuration.position, plant)) {
                    throw new StageConfigurationException(
                            "Unknown frozen plant: "
                                    + configuration.containedPlantType);
                }
            }
            if (configuration.containedZombieType != null) {
                Zombie zombie = spawner.createZombie(
                        configuration.containedZombieType);
                if (zombie == null || !board.configureFrozenZombie(
                        configuration.position, zombie)) {
                    throw new StageConfigurationException(
                            "Unknown frozen zombie: "
                                    + configuration.containedZombieType);
                }
            }
        }
    }
}
