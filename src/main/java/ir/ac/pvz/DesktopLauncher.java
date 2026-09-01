package ir.ac.pvz;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import ir.ac.pvz.view.PvzGame;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Plants vs. Zombies 2");
        configuration.setWindowedMode(1280, 720);
        configuration.setWindowSizeLimits(960, 540, -1, -1);
        configuration.useVsync(true);
        configuration.setForegroundFPS(60);
        new Lwjgl3Application(new PvzGame(), configuration);
    }
}
