package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.autosell.AutosellSettingsCodec;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SqlPlayerAutosellRepository implements PlayerAutosellRepository {

    private final DataSource dataSource;
    private final PluginConfig config;
    private final Executor executor;
    private final boolean sqlite;

    public SqlPlayerAutosellRepository(
            DataSource dataSource,
            PluginConfig config,
            Executor executor,
            boolean sqlite
    ) {
        this.dataSource = dataSource;
        this.config = config;
        this.executor = executor;
        this.sqlite = sqlite;
    }

    @Override
    public CompletableFuture<PlayerAutosellSettings> find(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT settings FROM soulbuyer_autosell WHERE player_uuid = ?")) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return AutosellSettingsCodec.decode(
                                playerId,
                                resultSet.getString("settings"),
                                config.autosell()
                        );
                    }
                }
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to read autosell settings " + playerId, exception);
            }
            return PlayerAutosellSettings.defaults(playerId, config.autosell());
        }, executor);
    }

    @Override
    public CompletableFuture<Void> save(PlayerAutosellSettings settings) {
        return CompletableFuture.runAsync(() -> {
            String sql = sqlite
                    ? """
                    INSERT INTO soulbuyer_autosell (player_uuid, settings, updated_at)
                    VALUES (?, ?, ?)
                    ON CONFLICT(player_uuid) DO UPDATE SET
                        settings = excluded.settings,
                        updated_at = excluded.updated_at
                    """
                    : """
                    INSERT INTO soulbuyer_autosell (player_uuid, settings, updated_at)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE settings = VALUES(settings), updated_at = VALUES(updated_at)
                    """;
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, settings.playerId().toString());
                statement.setString(2, AutosellSettingsCodec.encode(settings));
                statement.setLong(3, System.currentTimeMillis());
                statement.executeUpdate();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to save autosell settings " + settings.playerId(), exception);
            }
        }, executor);
    }
}
