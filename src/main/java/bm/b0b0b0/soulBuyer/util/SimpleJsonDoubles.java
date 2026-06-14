package bm.b0b0b0.soulBuyer.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SimpleJsonDoubles {

    private static final Pattern ENTRY = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

    private SimpleJsonDoubles() {
    }

    public static String encode(Map<String, Double> values) {
        if (values.isEmpty()) {
            return "{}";
        }
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append('"')
                    .append(':').append(entry.getValue());
        }
        builder.append('}');
        return builder.toString();
    }

    public static Map<String, Double> decode(String json) {
        Map<String, Double> values = new HashMap<>();
        if (json == null || json.isBlank()) {
            return values;
        }
        Matcher matcher = ENTRY.matcher(json);
        while (matcher.find()) {
            values.put(matcher.group(1), Double.parseDouble(matcher.group(2)));
        }
        return values;
    }

    private static String escape(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
