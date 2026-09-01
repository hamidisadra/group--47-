package ir.ac.pvz.controller.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class GameSettings {
    private static final String FILE_NAME = "settings.properties";
    private static GameSettings instance;

    private int difficulty;
    private int gameSpeed;
    private boolean showGrid;
    private boolean debugMode;
    private float musicVolume;
    private float soundVolume;

    private GameSettings() {
        difficulty = 3;
        gameSpeed = 1;
        showGrid = false;
        debugMode = false;
        musicVolume = 0.5f;
        soundVolume = 0.7f;
        load();
    }

    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }

        return instance;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty));
        save();
    }

    public int getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = Math.max(1, Math.min(3, gameSpeed));
        save();
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        save();
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        save();
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = clamp(musicVolume);
        save();
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(float soundVolume) {
        this.soundVolume = clamp(soundVolume);
        save();
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            difficulty = Integer.parseInt(properties.getProperty("difficulty", "3"));
            gameSpeed = Integer.parseInt(properties.getProperty("gameSpeed", "1"));
            showGrid = Boolean.parseBoolean(properties.getProperty("showGrid", "false"));
            debugMode = Boolean.parseBoolean(properties.getProperty("debugMode", "false"));
            musicVolume = Float.parseFloat(properties.getProperty("musicVolume", "0.5"));
            soundVolume = Float.parseFloat(properties.getProperty("soundVolume", "0.7"));
        }
        catch (IOException | NumberFormatException exception) {
            System.err.println("Could not read settings: " + exception.getMessage());
        }
    }

    private void save() {
        Properties properties = new Properties();
        properties.setProperty("difficulty", String.valueOf(difficulty));
        properties.setProperty("gameSpeed", String.valueOf(gameSpeed));
        properties.setProperty("showGrid", String.valueOf(showGrid));
        properties.setProperty("debugMode", String.valueOf(debugMode));
        properties.setProperty("musicVolume", String.valueOf(musicVolume));
        properties.setProperty("soundVolume", String.valueOf(soundVolume));

        try (FileOutputStream output = new FileOutputStream(FILE_NAME)) {
            properties.store(output, "Plants vs Zombies settings");
        }
        catch (IOException exception) {
            System.err.println("Could not save settings: " + exception.getMessage());
        }
    }
}
