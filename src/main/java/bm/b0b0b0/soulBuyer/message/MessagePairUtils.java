package bm.b0b0b0.soulBuyer.message;

public final class MessagePairUtils {

    private MessagePairUtils() {
    }

    public static String[] append(String[] base, String key, String value) {
        String[] merged = new String[base.length + 2];
        System.arraycopy(base, 0, merged, 0, base.length);
        merged[base.length] = key;
        merged[base.length + 1] = value;
        return merged;
    }

    public static String[] merge(String[] first, String[] second) {
        String[] merged = new String[first.length + second.length];
        System.arraycopy(first, 0, merged, 0, first.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
    }
}
