package bm.b0b0b0.soulBuyer.config.settings;

import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.custom.ClassSerializer;

public final class SoulBuyerSerializerConfig {

    public static final SerializerConfig INSTANCE = new SerializerConfig.Builder().build();

    private SoulBuyerSerializerConfig() {
    }

    public static final class NullableCmdSerializer extends ClassSerializer<Integer, Object> {

        public NullableCmdSerializer() {
            super(Integer.class, Object.class);
        }

        @Override
        public Object serialize(Integer value) {
            if (value == null || value < 0) {
                return -1;
            }
            return value;
        }

        @Override
        public Integer deserialize(Object value) {
            if (value == null) {
                return -1;
            }
            if (value instanceof Number number) {
                return number.intValue();
            }
            return -1;
        }
    }
}
