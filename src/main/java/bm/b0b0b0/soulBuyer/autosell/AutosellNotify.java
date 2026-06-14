package bm.b0b0b0.soulBuyer.autosell;

import java.util.List;

public final class AutosellNotify {

    public static final String ACTIONBAR = "actionbar";
    public static final String CHAT = "chat";
    public static final String OFF = "off";

    private static final List<String> CYCLE = List.of(ACTIONBAR, CHAT, OFF);

    private AutosellNotify() {
    }

    public static String normalize(String notify) {
        if (CHAT.equals(notify) || OFF.equals(notify)) {
            return notify;
        }
        return ACTIONBAR;
    }

    public static String next(String notify) {
        String current = normalize(notify);
        int index = CYCLE.indexOf(current);
        if (index < 0) {
            return ACTIONBAR;
        }
        return CYCLE.get((index + 1) % CYCLE.size());
    }
}
