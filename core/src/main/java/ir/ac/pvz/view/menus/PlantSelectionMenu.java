package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.game_core.CommandLineGame;
import ir.ac.pvz.controller.game_core.GameOutcomeListener;
import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.others.StageConfig;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.PlantDataRepository;
import ir.ac.pvz.model.support.PlantDefinition;
import ir.ac.pvz.model.user.CollectionStatus;
import ir.ac.pvz.model.user.TransactionStatus;
import ir.ac.pvz.model.user.User;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlantSelectionMenu extends Menu{
    private String imitaterTargetType;

    public PlantSelectionMenu() {
        super("Plant Selection Menu");
        this.imitaterTargetType = null;
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String enterMenuRegex = "^menu\\s+enter\\s+(.+)$";
        String addImitaterRegex = "^add\\s+plant\\s+-t\\s+Imitater"
                + "\\s+-c\\s+(.+)$";

        String addPlantRegex = "^add\\s+plant\\s+-t\\s+(.+)$";
        String removePlantRegex = "^remove\\s+plant\\s+-t\\s+(.+)$";
        String boostRegex = "^boost\\s+plant\\s+-t\\s+(.+)$";

        Matcher enterMenuMatcher = Pattern.compile(enterMenuRegex).matcher(command);
        Matcher addImitaterMatcher = Pattern.compile(addImitaterRegex)
                .matcher(command);

        Matcher addPlantMatcher = Pattern.compile(addPlantRegex).matcher(command);
        Matcher removePlantMatcher = Pattern.compile(removePlantRegex).matcher(command);
        Matcher boostMatcher = Pattern.compile(boostRegex).matcher(command);

        if (addImitaterMatcher.matches()) {
            addImitater(addImitaterMatcher.group(1).trim());
        }

        else if (addPlantMatcher.matches()) {
            String plantType = addPlantMatcher.group(1);

            addPlant(plantType);
        }

        else if (removePlantMatcher.matches()) {
            String plantType = removePlantMatcher.group(1);

            removePlant(plantType);
        }

        else if (boostMatcher.matches()) {
            String plantType = boostMatcher.group(1);

            boostPlant(plantType);
        }

        else if (!handleSimpleCommands(command, enterMenuMatcher)) {
            System.out.println("Invalid command.");
        }
    }

    private boolean handleSimpleCommands(String command, Matcher enterMenuMatcher) {
        if (command.matches("^show\\s+all\\s+plants$")) {
            showAllPlants();
        }

        else if (command.matches("^show\\s+available\\s+plants$")) {
            showAvailablePlants();
        }

        else if (command.matches("^start\\s+game$")) {
            startGame();
        }

        else if (enterMenuMatcher.matches()) {
            String menuName = enterMenuMatcher.group(1).trim().toLowerCase();
            enterMenu(menuName);
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else if (command.matches("^menu\\s+exit$")) {
            exitMenu();
        }

        else {
            return false;
        }

        return true;
    }

    private void exitMenu() {
        User user = menuManager.getActiveUser();

        if (user != null) {
            user.getCollection().clearSelection();
        }

        imitaterTargetType = null;
        menuManager.popMenu();
    }

    private void startGame() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        if (user.getCollection().getSelectedPlants().isEmpty()) {
            System.out.println("Error: You must select at least one plant to start the game!");
            return;
        }

        Board board = GameplayManager.getInstance().getBoard();
        if (board == null) {
            System.out.println("Error: No chapter is loaded!");
            return;
        }

        StageConfig stageConfig = createStageConfig(board, user);
        if (stageConfig == null) {
            System.out.println("Error: No stage is selected!");
            return;
        }

        applyPlantLevels(stageConfig, user);
        stageConfig.setSelectedPlantTypes(user.getCollection()
                .getSelectedPlants().toArray(new String[0]));

        if (isSelected(user, "Imitater")) {
            stageConfig.setImitaterTargetType(imitaterTargetType);
        }

        List<String> boosted = user.getCollection().getEffectiveBoostedPlants().stream()
                .filter(type -> isSelected(user, type))
                .toList();

        if (!boosted.isEmpty()) {
            stageConfig.setBoostedPlantTypes(boosted.toArray(new String[0]));
        }

        Stage playedStage = GameplayManager.getInstance().getCurrentStage();
        GameSession gameSession = new GameSession(board, 50, stageConfig);
        installOutcomeListener(gameSession, user, playedStage);
        System.out.println("Starting the game...");
        runGame(gameSession);
    }

    private void installOutcomeListener(GameSession gameSession, User user,
                                        Stage playedStage) {

        gameSession.setOutcomeListener(new GameOutcomeListener() {
            @Override
            public void onGameWon(GameSession finishedSession) {
                finishAdventureGame(user, playedStage, finishedSession, true);
            }

            @Override
            public void onGameLost(GameSession finishedSession) {
                finishAdventureGame(user, playedStage, finishedSession, false);
            }
        });
    }

    private void finishAdventureGame(User user, Stage playedStage,
                                     GameSession finishedSession, boolean won) {

        transferSessionRewards(user, finishedSession);

        if (won) {
            int completedProgress = GameplayManager.getInstance()
                    .completeStage(playedStage);

            if (completedProgress > user.getGameProgress()) {
                user.setGameProgress(completedProgress);
            }
        }

        user.addGame();
        user.getCollection().consumeEarnedBoosts(
                user.getCollection().getSelectedPlants());

        user.getCollection().clearSelection();
        imitaterTargetType = null;
        UserManager.getInstance().saveAll();
        menuManager.popMenu();
    }

    private void transferSessionRewards(User user, GameSession session) {
        if (session.getCoins() > 0) {
            user.getWallet().addCoins(session.getCoins());
        }

        if (session.getDiamonds() > 0) {
            user.getWallet().addGems(session.getDiamonds());
        }

        for (int count = 0; count < session.getPots(); count++) {
            if (!user.getGreenHouse().unlockNextPot()) {
                break;
            }
        }
    }

    private void runGame(GameSession gameSession) {
        CommandLineGame commandLineGame = new CommandLineGame(gameSession);
        BufferedReader in = menuManager.getIn();
        PrintWriter out = menuManager.getOut();

        try {
            if (in != null && out != null) {
                commandLineGame.run(in, out);
            }
            else {
                commandLineGame.run(new InputStreamReader(System.in),
                        new OutputStreamWriter(System.out));
            }
        }
        catch (IOException | RuntimeException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private StageConfig createStageConfig(Board board, User user) {
        Stage stage = GameplayManager.getInstance().getCurrentStage();

        if (stage == null || !stage.isUnlocked()
                || stage.getWaves().isEmpty()) {
            return null;
        }

        int[] waveCosts = new int[stage.getWaves().size()];

        for (int index = 0; index < waveCosts.length; index++) {
            waveCosts[index] = stage.getWaves().get(index).waveCost;
        }

        SeasonType seasonType = board.getSeasonType();
        StageConfig config = StageConfig.of(seasonType,
                stage.getWaveCount(), waveCosts[0]);

        config.setExplicitWaveCosts(waveCosts);
        config.setDifficultyLevel(user.getDifficultyLevel());

        return config;
    }

    private void applyPlantLevels(StageConfig stageConfig, User user) {
        for (String plantType : user.getCollection().getUnlockedPlants()) {
            int level = user.getCollection().getPlantLevel(plantType);
            stageConfig.setPlantLevel(plantType, level);
        }
    }

    private void showAvailablePlants() {
        User user = menuManager.getActiveUser();

        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        List<String> plants = user.getCollection().getUnlockedPlants();

        if (plants.isEmpty()) {
            System.out.println("You do not have any plants.");
            return;
        }

        System.out.println("========== Your Plants ==========");
        for (String plant : plants) {
            System.out.println("- " + plant);
        }
    }

    private void showAllPlants() {
        System.out.println("========== All Plants ==========");

        String[] allPlants = Plant.getSpreadsheetTypes();

        if (allPlants != null) {
            for (String name : allPlants) {
                System.out.println("- " + name + " -");
            }
        }
    }

    private void boostPlant(String plantType) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        PlantDefinition definition = PlantDataRepository.getInstance()
                .get(plantType == null ? null : plantType.trim());

        if (definition == null) {
            System.out.println("Error: Plant not found.");
            return;
        }

        String canonicalName = definition.name;
        if (user.getCollection().getCanonicalUnlockedPlant(canonicalName) == null) {
            System.out.println("Error: You must unlock this plant first!");
            return;
        }

        if (user.getCollection().getCanonicalSelectedPlant(canonicalName) == null) {
            System.out.println("Error: Select this plant before boosting it!");
            return;
        }

        if (user.getCollection().getBoostedPlants().stream()
                .anyMatch(name -> normalize(name).equals(normalize(canonicalName)))) {
            System.out.println("Error: This plant is already boosted!");
            return;
        }

        TransactionStatus purchaseStatus = user.getWallet().spendGems(2);
        if (purchaseStatus != TransactionStatus.SUCCESS) {
            System.out.println("Error: You don't have enough gems to boost!");
            return;
        }

        CollectionStatus boostStatus = user.getCollection().boostPlant(canonicalName);
        if (boostStatus != CollectionStatus.SUCCESS) {
            user.getWallet().addGems(2);
            System.out.println("Error: This plant could not be boosted!");
            return;
        }

        UserManager.getInstance().saveAll();
        System.out.println("Plant " + canonicalName + " boosted successfully.");
    }

    private void removePlant(String plantType) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        String selectedName = user.getCollection().getCanonicalSelectedPlant(plantType);
        if (selectedName == null) {
            System.out.println("Error: You have not selected this plant!");
            return;
        }

        if (isActiveImitaterTarget(user, selectedName)) {
            System.out.println("Error: Remove Imitater before removing its copied plant.");
            return;
        }

        CollectionStatus removeStatus = user.getCollection().removePlant(selectedName);
        if (removeStatus != CollectionStatus.SUCCESS) {
            System.out.println("Error: You have not selected this plant!");
            return;
        }

        if (normalize(selectedName).equals("imitater")) {
            imitaterTargetType = null;
        }

        System.out.println("Plant " + selectedName + " removed successfully.");
    }

    private void addPlant(String plantType) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        PlantDefinition definition = PlantDataRepository.getInstance()
                .get(plantType == null ? null : plantType.trim());

        if (definition == null) {
            System.out.println("Error: Plant not found.");
            return;
        }

        if (normalize(definition.name).equals("imitater")) {
            System.out.println("Error: Imitater requires a copied plant.");
            return;
        }

        CollectionStatus addStatus = user.getCollection().selectPlant(definition.name);
        printAddResult(addStatus, definition.name);
    }

    private void addImitater(String copiedPlantType) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        PlantDefinition targetDefinition = PlantDataRepository.getInstance()
                .get(copiedPlantType == null ? null : copiedPlantType.trim());

        if (targetDefinition == null) {
            System.out.println("Error: Plant not found.");
            return;
        }

        String canonicalTarget = targetDefinition.name;
        if (normalize(canonicalTarget).equals("imitater")) {
            System.out.println("Error: Imitater cannot copy itself.");
            return;
        }

        if (user.getCollection().getCanonicalUnlockedPlant(canonicalTarget) == null) {
            System.out.println("Error: You must unlock the copied plant first!");
            return;
        }

        if (user.getCollection().getCanonicalSelectedPlant(canonicalTarget) == null) {
            System.out.println("Error: Select the copied plant first.");
            return;
        }

        CollectionStatus addStatus = user.getCollection().selectPlant("Imitater");
        if (addStatus == CollectionStatus.SUCCESS) {
            imitaterTargetType = canonicalTarget;
        }
        printAddResult(addStatus, "Imitater");
    }

    private void printAddResult(CollectionStatus addStatus,
                                String plantType) {
        switch (addStatus) {
            case PLANT_NOT_FOUND: {
                System.out.println("Error: Plant not found.");
                break;
            }

            case PLANT_NOT_UNLOCKED: {
                System.out.println("Error: You have not unlocked this plant!");
                break;
            }

            case PLANT_ALREADY_SELECTED: {
                System.out.println("Error: You already selected this plant!");
                break;
            }

            case CAPACITY_IS_FULL: {
                System.out.println("Error: Your selection capacity is full!");
                break;
            }

            case SUCCESS: {
                System.out.println("Plant " + plantType
                        + " selected successfully.");
                break;
            }

            default: {
                System.out.println("An unknown error occurred!");
                break;
            }
        }
    }

    private boolean isActiveImitaterTarget(User user, String plantType) {
        return imitaterTargetType != null
                && normalize(imitaterTargetType).equals(normalize(plantType))
                && isSelected(user, "Imitater");
    }

    private boolean isSelected(User user, String plantType) {
        String normalized = normalize(plantType);

        return user.getCollection().getSelectedPlants().stream()
                .anyMatch(selected -> normalize(selected).equals(normalized));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }

    private void enterMenu(String menuName) {
        switch (menuName) {
            case "game menu": {
                System.out.println("Entering Game Menu...");
                menuManager.pushMenu(new GameMenu());
                break;
            }

            case "network menu", "settings menu", "profile menu", "main menu", "news menu": {
                System.out.println("Error: You can't access this menu from Plant Selection Menu!");
                break;
            }

            case "plant selection menu": {
                System.out.println("You are already in plant selection menu.");
                break;
            }

            default: {
                System.out.println("Invalid menu name!");
                break;
            }
        }
    }
}