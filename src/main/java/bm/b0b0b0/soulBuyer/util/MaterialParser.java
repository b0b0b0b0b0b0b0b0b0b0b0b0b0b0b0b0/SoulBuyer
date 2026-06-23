package bm.b0b0b0.soulBuyer.util;

import java.util.Locale;
import org.bukkit.Material;

public final class MaterialParser {

    private MaterialParser() {
    }

    public static Material parse(String name) {
        return parse(name, Material.STONE);
    }

    public static Material parse(String name, Material fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        try {
            return Material.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }
}
