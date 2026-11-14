package bm.b0b0b0.soulBuyer.util;

import java.util.Locale;
import org.bukkit.Material;

public final class MaterialParser {

    private static final String[][] MATERIAL_ALIASES = {
            {"CHAIN", "IRON_CHAIN"},
    };

    private MaterialParser() {
    }

    public static Material parse(String name) {
        return parse(name, Material.STONE);
    }

    public static Material parse(String name, Material fallback) {
        Material resolved = resolve(name);
        return resolved != null ? resolved : fallback;
    }

    public static Material resolve(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = name.toUpperCase(Locale.ROOT);
        for (String candidate : candidates(normalized)) {
            Material material = tryValueOf(candidate);
            if (material != null) {
                return material;
            }
        }
        Material matched = Material.matchMaterial(normalized);
        if (matched != null) {
            return matched;
        }
        return Material.matchMaterial(normalized.toLowerCase(Locale.ROOT));
    }

    public static boolean isKnown(String name) {
        return resolve(name) != null;
    }

    private static String[] candidates(String normalized) {
        for (String[] alias : MATERIAL_ALIASES) {
            if (alias[0].equals(normalized)) {
                return alias;
            }
            if (alias[1].equals(normalized)) {
                return new String[]{alias[1], alias[0]};
            }
        }
        return new String[]{normalized};
    }

    private static Material tryValueOf(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
