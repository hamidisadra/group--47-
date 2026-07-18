package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.PlantStatusView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameCommandController {

    private static final Pattern ADVANCE_TIME = Pattern.compile(
            "^advance time -t (\\d+) ticks$");
    private static final Pattern LOCATION = Pattern.compile(
            "\\(?\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)?");
    private static final Pattern COLLECT_SUN = Pattern.compile(
            "^collect sun -l (.+)$");
    private static final Pattern CHEAT_SUN = Pattern.compile(
            "^cheat add -n (-?\\d+) suns$");
    private static final Pattern PLANT = Pattern.compile(
            "^plant plant -t (.+?) -l (.+)$");
    private static final Pattern PLUCK = Pattern.compile(
            "^pluck plant -l (.+)$");
    private static final Pattern FEED = Pattern.compile(
            "^feed plant -l (.+)$");
    private static final Pattern TILE_STATUS = Pattern.compile(
            "^show tile status -l (.+)$");
    private static final Pattern SPAWN_ZOMBIE = Pattern.compile(
            "^cheat spawn-zombie -t (.+?) -l <?(.+?)>?$");

    private final GameSession session;
    private final GamePrinter printer;

    public GameCommandController(GameSession session) {
        this.session = session;
        this.printer = new GamePrinter();
    }

    public void advanceTime(int count) {
        session.advanceTime(count);
    }

    public void collectSun(int x, int y) {
        GridPosition position = toInternalPosition(x, y);
        if (position != null) {
            session.collectSun(position);
        }
    }

    public int showSunAmount() {
        return session.getSunManager().showSunAmount();
    }

    public void plantPlant(String type, int x, int y) {
        GridPosition position = toInternalPosition(x, y);
        if (position != null) {
            session.plantPlant(type, position);
        }
    }

    public void pluckPlant(int x, int y) {
        GridPosition position = toInternalPosition(x, y);
        if (position != null) {
            session.pluckPlant(position);
        }
    }

    public void feedPlant(int x, int y) {
        GridPosition position = toInternalPosition(x, y);
        if (position != null) {
            session.feedPlant(position);
        }
    }

    public String showMap() {
        return printer.showMap(session);
    }

    public String showPlantsStatus() {
        StringBuilder builder = new StringBuilder();
        for (PlantStatusView status : printer.showPlantsStatus(session)) {
            builder.append(status.plantType).append(": sun=")
                    .append(status.requiredSun).append(", plantable=")
                    .append(status.plantableNow).append(", cooldown=")
                    .append(status.cooldownRemainingSeconds).append('s')
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    public String showTileStatus(int x, int y) {
        GridPosition position = toInternalPosition(x, y);
        return position == null ? "" : printer.showTileStatus(
                session.getBoard(), position);
    }

    public String zombiesInfo() {
        return printer.zombiesInfo(session.getBoard());
    }

    public void releaseNuke() {
        session.releaseNuke();
    }

    public void cheatAddSuns(int count) {
        session.getSunManager().addSuns(count);
        session.currentSunAmount = session.getSunManager().showSunAmount();
    }

    public void cheatAddPlantFood() {
        session.getPlantFoodInventory().cheatAddPlantFood();
        session.plantFoodCount = session.getPlantFoodInventory().count;
    }

    public void cheatRemoveCooldown() {
        session.cheatRemoveCooldown();
    }

    public void cheatSpawnZombie(String type, int x, int y) {
        GridPosition position = toInternalPosition(x, y);
        if (position != null) {
            session.cheatSpawnZombie(type, position.x, position.y);
        }
    }

    public String execute(String command) {
        if (command == null) {
            return "Invalid command.";
        }
        String trimmed = command.trim();
        Matcher matcher = ADVANCE_TIME.matcher(trimmed);
        if (matcher.matches()) {
            advanceTime(Integer.parseInt(matcher.group(1)));
            return "";
        }
        if (trimmed.equals("show sun amount")) {
            return String.valueOf(showSunAmount());
        }
        if (trimmed.equals("show map")) {
            return showMap();
        }
        if (trimmed.equals("show plants status")) {
            return showPlantsStatus();
        }
        if (trimmed.equals("zombies info")) {
            return zombiesInfo();
        }
        if (trimmed.equals("release the nuke")) {
            releaseNuke();
            return "";
        }
        if (trimmed.equals("cheat add-plant-food")) {
            return session.getPlantFoodInventory().cheatAddPlantFood()
                    ? "" : "Plant food inventory is full.";
        }
        if (trimmed.equals("cheat remove-cooldown")) {
            cheatRemoveCooldown();
            return "";
        }
        String result = executeSunCommands(trimmed);
        if (result != null) {
            return result;
        }
        result = executePlantCommands(trimmed);
        if (result != null) {
            return result;
        }
        result = executeStatusOrSpawnCommands(trimmed);
        return result == null ? "Invalid command." : result;
    }

    private String executeSunCommands(String command) {
        Matcher matcher = COLLECT_SUN.matcher(command);
        if (matcher.matches()) {
            GridPosition position = parsePosition(matcher.group(1));
            if (position == null) {
                return "Invalid location.";
            }
            return session.collectSun(position) ? "" : "No collectible sun at location.";
        }
        matcher = CHEAT_SUN.matcher(command);
        if (matcher.matches()) {
            int amount = Integer.parseInt(matcher.group(1));
            if (amount < 0) {
                return "Sun amount cannot be negative.";
            }
            cheatAddSuns(amount);
            return "";
        }
        return null;
    }

    private String executePlantCommands(String command) {
        Matcher matcher = PLANT.matcher(command);
        if (matcher.matches()) {
            GridPosition position = parsePosition(matcher.group(2));
            if (position == null) {
                return "Invalid location.";
            }
            String type = matcher.group(1).trim();
            String error = session.getPlantingError(type, position);
            if (error != null) {
                return error;
            }
            return session.plantPlant(type, position) ? ""
                    : "Plant cannot be planted on this tile.";
        }
        matcher = PLUCK.matcher(command);
        if (matcher.matches()) {
            GridPosition position = parsePosition(matcher.group(1));
            Plant plant = position == null ? null : session.pluckPlant(position);
            return plant == null ? "No plant at location." : "";
        }
        matcher = FEED.matcher(command);
        if (matcher.matches()) {
            GridPosition position = parsePosition(matcher.group(1));
            return position != null && session.feedPlant(position)
                    ? "" : "Plant cannot be fed.";
        }
        return null;
    }

    private String executeStatusOrSpawnCommands(String command) {
        Matcher matcher = TILE_STATUS.matcher(command);
        if (matcher.matches()) {
            GridPosition position = parsePosition(matcher.group(1));
            return position == null ? "Invalid location."
                    : printer.showTileStatus(session.getBoard(), position);
        }
        matcher = SPAWN_ZOMBIE.matcher(command);
        if (matcher.matches()) {
            GridPosition position = parsePosition(matcher.group(2));
            if (position == null || session.cheatSpawnZombie(
                    matcher.group(1).trim(), position.x, position.y) == null) {
                return "Zombie cannot be spawned.";
            }
            return "";
        }
        return null;
    }

    private GridPosition parsePosition(String text) {
        Matcher matcher = LOCATION.matcher(text.trim());
        if (!matcher.matches()) {
            return null;
        }
        return toInternalPosition(Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)));
    }

    private GridPosition toInternalPosition(int userX, int userY) {
        if (userX < 1 || userY < 1) {
            return null;
        }
        return new GridPosition(userX - 1, userY - 1);
    }
}
