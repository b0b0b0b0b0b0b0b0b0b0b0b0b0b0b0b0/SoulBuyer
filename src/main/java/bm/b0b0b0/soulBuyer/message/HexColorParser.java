package bm.b0b0b0.soulBuyer.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class HexColorParser {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINIMESSAGE_TAG = Pattern.compile(
            "<(?!/?empty>|/?blank>)([!?#])?[a-z0-9_:/\\-]+",
            Pattern.CASE_INSENSITIVE
    );
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private HexColorParser() {
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        if (usesMiniMessage(input)) {
            return MINI_MESSAGE.deserialize(input);
        }
        return LEGACY.deserialize(applyHexColors(input).replace('&', '§'));
    }

    public static String replacePlaceholders(String template, String... pairs) {
        String result = template;
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            result = result.replace("{" + pairs[index] + "}", pairs[index + 1]);
        }
        return result;
    }

    private static boolean usesMiniMessage(String input) {
        return MINIMESSAGE_TAG.matcher(input).find();
    }

    private static String applyHexColors(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder legacy = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            legacy.append(input, last, matcher.start());
            legacy.append('§').append('x');
            String hex = matcher.group(1);
            for (char character : hex.toCharArray()) {
                legacy.append('§').append(character);
            }
            last = matcher.end();
        }
        legacy.append(input.substring(last));
        return legacy.toString();
    }
}
