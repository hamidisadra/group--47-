package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.game_core.CommandLineGame;
import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.others.StageConfig;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ZombotanyMenu extends Menu {

    private static final String[] PLANT_ZOMBIES = {
            "PeashooterZombie", "WallnutZombie", "JalapenoZombie", "SquashZombie"
    };

    private static final int STARTING_SUN = 150;
    private static final int WAVE_COUNT = 3;
    private static final int BASE_WAVE_COST = 1000;

    private List<String> selectedPlants;
    private int stageNumber;

    public ZombotanyMenu(int stageNumber) {
        super("Zombotany Menu");
        this.stageNumber = stageNumber;
        this.selectedPlants = new ArrayList<>();
        System.out.println("Starting Zombotany stage " + stageNumber + ".");
        System.out.println("In this mini game the zombies borrow plant abilities.");
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        if (command.matches("^add\\s+plant\\s+-t\\s+.+$")) {
            addPlant(command.replaceFirst("^add\\s+plant\\s+-t\\s+", "").trim());
        }

        else if (command.matches("^remove\\s+plant\\s+-t\\s+.+$")) {
            removePlant(command.replaceFirst("^remove\\s+plant\\s+-t\\s+", "").trim());
        }

        else if (command.matches("^show\\s+selected\\s+plants$")) {
            showSelected();
        }

        else if (command.matches("^show\\s+plant\\s+zombies$")) {
            showPlantZombies();
        }

        else if (command.matches("^start\\s+game$")) {
            startGame();
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else if (command.matches("^menu\\s+exit$")) {
            menuManager.popMenu();
        }

        else {
            System.out.println("Invalid command.");
        }
    }

    private void addPlant(String plantType) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        if (!user.getCollection().getUnlockedPlants().contains(plantType)) {
            System.out.println("Error: You have not unlocked this plant!");
            return;
        }

        if (selectedPlants.contains(plantType)) {
            System.out.println("Error: This plant is already selected!");
            return;
        }

        selectedPlants.add(plantType);
        System.out.println("Plant " + plantType + " selected successfully.");
    }

    private void removePlant(String plantType) {
        if (!selectedPlants.remove(plantType)) {
            System.out.println("Error: This plant is not selected!");
            return;
        }
        System.out.println("Plant " + plantType + " removed successfully.");
    }

    private void showSelected() {
        if (selectedPlants.isEmpty()) {
            System.out.println("You have not selected any plants.");
            return;
        }

        System.out.println("========== Selected Plants ==========");

        for (String plant : selectedPlants) {
            System.out.println("- " + plant);
        }
    }

    private void showPlantZombies() {
        System.out.println("========== Plant Zombies ==========");
        System.out.println("- PeashooterZombie: shoots peas at your plants");
        System.out.println("- WallnutZombie: very tough, slow");
        System.out.println("- JalapenoZombie: burns its row after 10 seconds");
        System.out.println("- SquashZombie: fast, crushes the first plant it meets");
    }

    private void startGame() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        if (selectedPlants.isEmpty()) {
            System.out.println("Error: You must select at least one plant to start the game!");
            return;
        }

        System.out.println("Starting the game...");

        Board board = new Board(5, 9, SeasonType.ANCIENT_EGYPT);
        StageConfig config = StageConfig.of(SeasonType.ANCIENT_EGYPT,
                WAVE_COUNT, BASE_WAVE_COST * stageNumber, PLANT_ZOMBIES);
        config.setSelectedPlantTypes(selectedPlants.toArray(new String[0]));

        GameSession session = new GameSession(board, STARTING_SUN, config);
        runGame(session);

        user.addGame();
        if (session.status == ir.ac.pvz.model.enums.GameStatus.WON) {
            GameplayManager.getInstance().getLeaderboard()
                    .getOrCreateEntry(user.getUsername()).addMinigameCompleted();
        }

        menuManager.popMenu();
    }

    private void runGame(GameSession session) {
        CommandLineGame game = new CommandLineGame(session);
        BufferedReader input = menuManager.getIn();
        PrintWriter output = menuManager.getOut();

        try {
            if (input != null && output != null) {
                game.run(input, output);
            }

            else {
                game.run(new InputStreamReader(System.in),
                        new OutputStreamWriter(System.out));
            }
        } catch (IOException | RuntimeException exception) {
            System.out.println("The mini game could not run: " + exception.getMessage());
        }
    }
}