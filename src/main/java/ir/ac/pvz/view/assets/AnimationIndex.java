package ir.ac.pvz.view.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AnimationIndex {
    public static final class Entry {
        public final String name;
        public final String path;
        public final float canvasWidth;
        public final float canvasHeight;
        public final Set<String> clips;

        Entry(String name, String path, float canvasWidth, float canvasHeight, Set<String> clips) {
            this.name = name;
            this.path = path;
            this.canvasWidth = canvasWidth;
            this.canvasHeight = canvasHeight;
            this.clips = clips;
        }

        public boolean hasClip(String clip) {
            return clips.contains(clip);
        }
    }

    private final Map<String, Entry> byPath = new HashMap<>();
    private final Map<String, List<Entry>> byName = new HashMap<>();
    private final Map<String, List<Entry>> byPlainName = new HashMap<>();

    public AnimationIndex(FileHandle indexFile) {
        JsonValue root = new JsonReader().parse(indexFile);
        JsonValue list = root.get("animations");
        for (JsonValue node = list == null ? null : list.child; node != null; node = node.next) {
            String name = node.getString("name", null);
            String path = node.getString("path", null);
            if (name == null || path == null) {
                continue;
            }
            JsonValue canvas = node.get("canvas");
            float width = canvas != null && canvas.size > 0 ? canvas.getFloat(0) : 0f;
            float height = canvas != null && canvas.size > 1 ? canvas.getFloat(1) : 0f;
            Set<String> clips = new HashSet<>();
            JsonValue clipNode = node.get("clips");
            for (JsonValue clip = clipNode == null ? null : clipNode.child; clip != null; clip = clip.next) {
                clips.add(clip.name);
            }
            Entry entry = new Entry(name, path, Math.abs(width), Math.abs(height), clips);
            byPath.put(path, entry);
            byName.computeIfAbsent(name, key -> new ArrayList<>()).add(entry);
            byPlainName.computeIfAbsent(plain(name), key -> new ArrayList<>()).add(entry);
        }
    }

    private static String plain(String value) {
        StringBuilder builder = new StringBuilder();
        for (char character : value.toCharArray()) {
            if (Character.isLetterOrDigit(character)) {
                builder.append(Character.toUpperCase(character));
            }
        }
        return builder.toString();
    }

    public Entry byPath(String path) {
        return byPath.get(path);
    }

    public Entry find(String name, String preferredFolder) {
        List<Entry> matches = byName.get(name);
        if (matches == null || matches.isEmpty()) {
            matches = byPlainName.get(plain(name));
        }
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        if (preferredFolder != null) {
            for (Entry entry : matches) {
                if (entry.path.contains("/" + preferredFolder + "/")) {
                    return entry;
                }
            }
        }
        return matches.get(0);
    }

    public boolean contains(String name) {
        return byName.containsKey(name);
    }

    public int size() {
        return byPath.size();
    }
}
