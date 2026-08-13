package ir.ac.pvz.model.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArmorDataRepository implements ArmorDefinitionRepository {
    private static final ArmorDataRepository INSTANCE = load();
    private final Map<String, Integer> healthByAlias;
    private final Map<String, Boolean> metallicByAlias;
    private ArmorDataRepository(Map<String, Integer> healthByAlias,
                                Map<String, Boolean> metallicByAlias) {
        this.healthByAlias = healthByAlias;
        this.metallicByAlias = metallicByAlias;
    }
    public static ArmorDataRepository getInstance() {
        return INSTANCE;
    }
    public int getHealth(String alias) {
        Integer health = healthByAlias.get(normalize(alias));
        if (health == null) {
            throw new IllegalArgumentException("Unknown armor data: " + alias);
        }
        return health;
    }
    public boolean isMetallic(String alias) {
        return metallicByAlias.getOrDefault(normalize(alias), false);
    }
    private static ArmorDataRepository load() {
        try (InputStream input = DataFileLocator.open("ArmorTypeData.json")) {
            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Integer> health = new LinkedHashMap<>();
            Map<String, Boolean> metallic = new LinkedHashMap<>();
            for (String object : splitTopLevelObjects(json)) {
                String alias = stringValue(object, "aliases");
                if (alias == null) {
                    continue;
                }
                health.put(normalize(alias), (int) numberValue(object,
                        "BaseHealth", 0d));
                metallic.put(normalize(alias), object.contains("\"metallic\""));
            }
            if (health.size() < 6) {
                throw new IOException("ArmorTypeData.json must contain all required records.");
            }
            validateOfficialHealth(health, "ConeDefault", 370);
            validateOfficialHealth(health, "BucketDefault", 1100);
            validateOfficialHealth(health, "BrickDefault", 2200);
            validateOfficialHealth(health, "ShoulderArmorDefault", 1600);
            validateOfficialHealth(health, "CrownDefault", 1600);
            return new ArmorDataRepository(health, metallic);
        }
        catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
    private static void validateOfficialHealth(Map<String, Integer> values,
                                               String alias, int expected)
            throws IOException {
        if (values.getOrDefault(normalize(alias), -1) != expected) {
            throw new IOException(alias + " must have health " + expected + ".");
        }
    }
    private static List<String> splitTopLevelObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean quoted = false;
        boolean escaped = false;
        for (int index = 0; index < json.length(); index++) {
            char character = json.charAt(index);
            if (quoted) {
                if (escaped) {
                    escaped = false;
                }
                else if (character == '\\') {
                    escaped = true;
                }
                else if (character == '"') {
                    quoted = false;
                }
                continue;
            }
            if (character == '"') {
                quoted = true;
            }
            else if (character == '{') {
                if (depth == 0) {
                    start = index;
                }
                depth++;
            }
            else if (character == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(json.substring(start, index + 1));
                }
            }
        }
        return objects;
    }
    private static String stringValue(String object, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key)
                + "\\\"\\s*:\\s*\\[\\s*\\\"([^\\\"]+)\\\"");
        Matcher matcher = pattern.matcher(object);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }
    private static double numberValue(String object, String key,
                                      double defaultValue) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key)
                + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(object);
        if (!matcher.find()) {
            return defaultValue;
        }
        return Double.parseDouble(matcher.group(1));
    }
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
