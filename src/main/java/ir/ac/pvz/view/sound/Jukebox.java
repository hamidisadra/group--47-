package ir.ac.pvz.view.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import ir.ac.pvz.controller.managers.GameSettings;

import java.util.HashMap;
import java.util.Map;

public class Jukebox implements Disposable {
    private final Map<String, Sound> sounds = new HashMap<>();
    private Music current;
    private String currentName;

    public void playMusic(String name) {
        if (name == null || name.equals(currentName)) {
            return;
        }
        stopMusic();
        FileHandle file = resolve(name);
        if (file == null) {
            return;
        }
        current = Gdx.audio.newMusic(file);
        current.setLooping(true);
        current.setVolume(GameSettings.getInstance().getMusicVolume());
        current.play();
        currentName = name;
    }

    private FileHandle resolve(String name) {
        for (String extension : new String[] { ".ogg", ".wav", ".mp3" }) {
            FileHandle file = Gdx.files.internal("assets/audio/" + name + extension);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public void updateVolume() {
        if (current != null) {
            current.setVolume(GameSettings.getInstance().getMusicVolume());
        }
    }

    public void stopMusic() {
        if (current != null) {
            current.stop();
            current.dispose();
            current = null;
            currentName = null;
        }
    }

    public void play(String name) {
        float volume = GameSettings.getInstance().getSoundVolume();
        if (volume <= 0f) {
            return;
        }
        Sound sound = sounds.get(name);
        if (sound == null) {
            if (sounds.containsKey(name)) {
                return;
            }
            FileHandle file = resolve(name);
            sound = file == null ? null : Gdx.audio.newSound(file);
            sounds.put(name, sound);
        }
        if (sound != null) {
            sound.play(volume);
        }
    }

    @Override
    public void dispose() {
        stopMusic();
        for (Sound sound : sounds.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        sounds.clear();
    }
}
