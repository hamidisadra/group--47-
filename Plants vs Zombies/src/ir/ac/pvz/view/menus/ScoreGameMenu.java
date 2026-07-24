package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.game_core.CommandLineGame;
import ir.ac.pvz.controller.game_core.GameOutcomeListener;
import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.others.StageConfig;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.travel.Leaderboard;
import ir.ac.pvz.model.travel.ScoreEvent;
import ir.ac.pvz.model.travel.ScoreTracker;
import ir.ac.pvz.model.travel.ScoredGame;
import ir.ac.pvz.model.user.User;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ScoreGameMenu extends Menu{
    private static final int STARTING_SUN = 150;
    private static final int WAVE_COUNT = 3;
    private static final int BASE_WAVE_COST = 200;

    private List<String> selectedPlants;
    private ScoredGame scoredGame;

    public ScoreGameMenu() {
        super("Score Game Menu");
        this.selectedPlants = new ArrayList<>();
        this.scoredGame = new ScoredGame(LocalDate.now().toEpochDay());
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        if (command.matches("^add\\s+plant\\s+-t\\s+.+$")) {
            addPlant(command.replaceFirst("^add\\s+plant\\s+-t\\s+", "").trim());
        }

        else if (command.matches("^show\\s+selected\\s+plants$")) {
            showSelected();
        }

        else if (command.matches("^start\\s+scored\\s+game$")) {
            startScoredGame();
        }

        else if (command.matches("^show\\s+high\\s+score$")) {
            showHighScore();
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

    private void showHighScore() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        System.out.println("Your best MU-Point: " + user.getMaxMuPoint());
    }

    private void startScoredGame() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        if (selectedPlants.isEmpty()) {
            System.out.println("Error: You must select at least one plant to start the game!");
            return;
        }

        System.out.println("Starting the scored game of " + scoredGame.getDate() + "...");

        Board board = new Board(5, 9, SeasonType.ANCIENT_EGYPT);
        StageConfig config = StageConfig.of(SeasonType.ANCIENT_EGYPT,
                WAVE_COUNT, BASE_WAVE_COST);
        config.setSelectedPlantTypes(selectedPlants.toArray(new String[0]));
        config.setRandomSeed(scoredGame.getSunSeed());

        GameSession session = new GameSession(board, STARTING_SUN, config);

        session.setOutcomeListener(new GameOutcomeListener() {
            @Override
            public void onGameWon(GameSession finished) {
                finishScoring(user, finished);
            }

            @Override
            public void onGameLost(GameSession finished) {
                finishScoring(user, finished);
            }
        });

        runGame(session);
    }

    private void finishScoring(User user, GameSession finished) {
        ScoreTracker tracker = new ScoreTracker();
        List<ScoreEvent> events = tracker.detect(finished.getStatistics());
        int score = scoredGame.calculateScore(events);

        System.out.println("========== MU-Point Report ==========");
        if (events.isEmpty()) {
            System.out.println("No scoring pattern was achieved.");
        }
        for (ScoreEvent event : events) {
            System.out.println("- " + event.getType() + " x" + event.getValue()
                    + " => " + event.getPoints() + " points");
        }
        System.out.println("Total MU-Point: " + score);

        if (score > user.getMaxMuPoint()) {
            user.setMaxMuPoint(score);
            System.out.println("New personal record!");
        }
        user.addGame();

        Leaderboard leaderboard = GameplayManager.getInstance().getLeaderboard();
        leaderboard.getOrCreateEntry(user.getUsername()).updateHighScore(score);

        UserManager.getInstance().saveAll();
    }

    private void runGame(GameSession session) {
        CommandLineGame game = new CommandLineGame(session);
        BufferedReader in = menuManager.getIn();
        PrintWriter out = menuManager.getOut();

        try {
            if (in != null && out != null) {
                game.run(in, out);
            }
            else {
                game.run(new InputStreamReader(System.in), new OutputStreamWriter(System.out));
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

}