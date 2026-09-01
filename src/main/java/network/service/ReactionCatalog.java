package network.service;

import java.util.Arrays;
import java.util.List;

public final class ReactionCatalog {
    public static final List<String> TEXTS = List.of(
        "Good luck!",
        "Nice move.",
        "You will not get past this lane."
    );

    public static final List<String> EMOJIS = List.of("\uD83D\uDE04",
        "\uD83D\uDE31", "\uD83E\uDDDF");

    public static final List<String> STICKERS = List.of(
        "sticker_dancing_sunflower",
        "sticker_chomping_zombie",
        "sticker_exploding_cherry"
    );

    private ReactionCatalog() {
    }

    public static boolean isValid(String kind, int index) {
        List<String> options = optionsFor(kind);

        return options != null && index >= 0 && index < options.size();
    }

    public static String valueOf(String kind, int index) {
        List<String> options = optionsFor(kind);

        if (options == null || index < 0 || index >= options.size()) {
            return null;
        }

        return options.get(index);
    }

    private static List<String> optionsFor(String kind) {
        if (kind == null) {
            return null;
        }

        switch (kind.toLowerCase()) {
            case "text":
                return TEXTS;

            case "emoji":
                return EMOJIS;

            case "sticker":
                return STICKERS;

            default:
                return null;
        }
    }

    public static List<String> kinds() {
        return Arrays.asList("text", "emoji", "sticker");
    }
}
