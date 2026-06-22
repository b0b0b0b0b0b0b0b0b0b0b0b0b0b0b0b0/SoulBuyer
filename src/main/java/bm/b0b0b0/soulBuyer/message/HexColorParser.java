package bm.b0b0b0.soulBuyer.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HexColorParser {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private HexColorParser() {
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
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
        return LEGACY.deserialize(legacy.toString());
    }

    public static String replacePlaceholders(String template, String... pairs) {
        String result = template;
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            result = result.replace("{" + pairs[index] + "}", pairs[index + 1]);
        }
        return result;
    }
}
