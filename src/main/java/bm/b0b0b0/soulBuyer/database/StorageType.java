package bm.b0b0b0.soulBuyer.database;

public enum StorageType {

    FLAT("flat"),
    SQLITE("sqlite"),
    MYSQL("mysql");

    private final String id;

    StorageType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean sql() {
        return this == SQLITE || this == MYSQL;
    }

    public static StorageType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return FLAT;
        }
        return switch (raw.trim().toLowerCase()) {
            case "sqlite" -> SQLITE;
            case "mysql" -> MYSQL;
            default -> FLAT;
        };
    }
}
