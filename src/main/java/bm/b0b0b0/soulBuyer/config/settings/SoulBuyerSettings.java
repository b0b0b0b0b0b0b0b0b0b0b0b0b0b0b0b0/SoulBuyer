package bm.b0b0b0.soulBuyer.config.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.annotations.Serializer;
import net.elytrium.serializer.language.object.YamlSerializable;

public final class SoulBuyerSettings extends YamlSerializable {

    public SoulBuyerSettings() {
        super(SoulBuyerSerializerConfig.INSTANCE);
    }

    @Comment({
            @CommentValue("=== Update check ==="),
            @CommentValue("On startup (async): fetch latest version from https://b0b0b0.dev/pl/souls/soulbuyer.txt"),
            @CommentValue("and print result to console. No player or server data is sent."),
            @CommentValue("Set false to disable the remote version check entirely."),
    })
    public boolean checkForUpdates = true;

    @NewLine
    @Comment({
            @CommentValue("=== bStats (anonymous usage statistics) ==="),
            @CommentValue("Helps the author see how many servers run SoulBuyer: plugin version."),
            @CommentValue("Set enabled: false to opt out on this server."),
    })
    public BstatsSettings bstats = new BstatsSettings();

    @Comment(@CommentValue("Verbose console logs: bootstrap, commands, sales, storage. Use false in production."))
    public boolean debug = false;

    @Comment(@CommentValue("TOOLTIP-DEBUG: dump GUI icon stacks to console (/soulbuyer admin debug-tooltip, auto-dump smithing in /buyer). Set false in production."))
    public boolean debugTooltip = false;

    @NewLine
    @Comment({
            @CommentValue("=== DATA STORAGE MODE ==="),
            @CommentValue("Where the plugin stores player progress (points, categories) and market state."),
            @CommentValue("flat   — per-player YAML + market.yml (single server, no MySQL/Redis)."),
            @CommentValue("sqlite — data/data.db file (single server, no MySQL/Redis)."),
            @CommentValue("mysql  — MySQL + Redis for a multi-server network."),
            @CommentValue("Sellable items live in a separate items.yml file.")
    })
    public String storageType = "flat";

    @NewLine
    @Comment({
            @CommentValue("=== DATA PATHS (flat / sqlite) ==="),
            @CommentValue("When storage-type: mysql, this section is unused (data lives in MySQL)."),
            @CommentValue("When storage-type: flat, the folders/files below are created under plugins/SoulBuyer/.")
    })
    public StorageSettings storage = new StorageSettings();

    @NewLine
    @Comment({
            @CommentValue("=== SERVER NETWORK ==="),
            @CommentValue("Single server (flat or sqlite): leave single-server: true."),
            @CommentValue("Network (mysql): each server needs its own server-id, single-server: false, Redis enabled.")
    })
    public NetworkSettings network = new NetworkSettings();

    @NewLine
    @Comment({
            @CommentValue("=== MYSQL (storage-type: mysql only) ==="),
            @CommentValue("When using flat/sqlite, you can ignore this section — the plugin skips it."),
            @CommentValue("The database must exist beforehand; tables are created automatically on first start.")
    })
    public MysqlSettings mysql = new MysqlSettings();

    @NewLine
    @Comment({
            @CommentValue("=== REDIS (mysql + network only) ==="),
            @CommentValue("When flat/sqlite or single-server: true, Redis is not connected."),
            @CommentValue("Required for a shared market across servers (pub/sub price sync).")
    })
    public RedisSettings redis = new RedisSettings();

    @NewLine
    @Comment(@CommentValue("Language files and locale mode — see locale section below."))
    public LocaleSettings locale = new LocaleSettings();

    @NewLine
    @Comment({
            @CommentValue("=== PAYOUT ECONOMY ==="),
            @CommentValue("player-points-enabled — requires the PlayerPoints plugin."),
            @CommentValue("donate-buyer-enabled: true — two menus: regular (Vault) + donate (/donbuyer, PlayerPoints)."),
            @CommentValue("donate-buyer-enabled: false with PlayerPoints enabled — one menu, payouts only in points.")
    })
    public EconomySettings economy = new EconomySettings();

    @NewLine
    @Comment({
            @CommentValue("=== COMMANDS ==="),
            @CommentValue("main — primary command. open-aliases — extra commands that open the same menu."),
            @CommentValue("Read from config.yml at server startup. After changes — restart (reload does not re-register commands)."),
            @CommentValue("Example: main buyer + open-aliases seller → /buyer and /seller."),
    })
    public CommandsSettings commands = new CommandsSettings();

    @NewLine
    @Comment(@CommentValue("Permission nodes (rename to match your LuckPerms setup)"))
    public PermissionsSettings permissions = new PermissionsSettings();

    @NewLine
    @Comment({
            @CommentValue("=== DYNAMIC MARKET ==="),
            @CommentValue("The more players sell one resource, the lower its price coefficient."),
            @CommentValue("decay gradually restores the coefficient toward 1.0 when sales stop.")
    })
    public MarketSettings market = new MarketSettings();

    @NewLine
    @Comment({
            @CommentValue("=== PLAYER PROGRESSION ==="),
            @CommentValue("Points from sales, permission multipliers, bonus from the dominant category.")
    })
    public ProgressionSettings progression = new ProgressionSettings();

    @NewLine
    @Comment({
            @CommentValue("=== AUTOSELL ==="),
            @CommentValue("enabled — show the autosell button and enable autosell on this server."),
            @CommentValue("Permission soulbuyer.autosell — grant to donors (LuckPerms).")
    })
    public AutosellSettings autosell = new AutosellSettings();

    @NewLine
    @Comment({
            @CommentValue("=== BOOSTERS ==="),
            @CommentValue("Temporary boost shop in the buyer menu."),
            @CommentValue("currency: progression_points | vault | playerpoints")
    })
    public BoostersSettings boosters = new BoostersSettings();

    @NewLine
    @Comment({
            @CommentValue("=== SELL LIMITS ==="),
            @CommentValue("Per-player daily limit per item-id (anti-dumping)."),
            @CommentValue("permission-limits — LuckPerms node → items/day (highest granted value wins).")
    })
    public SellLimitsSettings sellLimits = new SellLimitsSettings();

    @NewLine
    @Comment({
            @CommentValue("=== RESOURCE CATEGORIES ==="),
            @CommentValue("category id → name key in lang/*.yml (categories.ores, etc.)."),
            @CommentValue("order — sort order in the catalog GUI (lower = higher).")
    })
    public Map<String, CategorySettings> categories = defaultCategories();

    public static final class StorageSettings {
        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/players/{uuid}.yml — points and category XP"))
        public String playersFolder = "data/players";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/autosell/{uuid}.yml — autosell settings"))
        public String autosellFolder = "data/autosell";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/boosters/{uuid}.yml — active boosters"))
        public String boostersFolder = "data/boosters";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/global-boosters.yml — server-wide boosters"))
        public String globalBoostersFile = "data/global-boosters.yml";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/sell-limits/{uuid}.yml — sales in the current period"))
        public String sellLimitsFolder = "data/sell-limits";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/market.yml — market coefficients"))
        public String marketFile = "data/market.yml";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/rotation.yml — current buyer catalog rotation"))
        public String rotationFile = "data/rotation.yml";

        @Comment(@CommentValue("flat/sqlite: plugins/SoulBuyer/data/sales.log — sales log"))
        public String salesLogFile = "data/sales.log";

        @Comment(@CommentValue("sqlite: plugins/SoulBuyer/data/data.db — SQLite database file"))
        public String sqliteFile = "data/data.db";

        @Comment(@CommentValue("sqlite: JDBC pool size (4 is usually enough)"))
        public int poolSize = 4;

        @Comment(@CommentValue("sqlite: database connection timeout, ms"))
        public long connectionTimeoutMs = 30000L;
    }

    public static final class NetworkSettings {
        @Comment(@CommentValue("Unique id of this Paper server (server-1, lobby, survival-1, …)"))
        public String serverId = "server-1";

        @Comment({
                @CommentValue("true  — single server: Redis disabled, local market."),
                @CommentValue("false — network: requires mysql + redis.enabled: true")
        })
        public boolean singleServer = true;
    }

    public static final class MysqlSettings {
        @Comment(@CommentValue("MySQL host (127.0.0.1 or VPS IP)"))
        public String host = "127.0.0.1";

        public int port = 3306;

        @Comment(@CommentValue("Database name (create an empty DB before first startup)"))
        public String database = "soulbuyer";

        public String user = "root";
        public String password = "";

        @Comment(@CommentValue("HikariCP pool size"))
        public int poolSize = 10;

        public long connectionTimeoutMs = 30000L;
    }

    public static final class RedisSettings {
        @Comment(@CommentValue("false — force Redis off (even on a network; not recommended)"))
        public boolean enabled = true;

        public String host = "127.0.0.1";
        public int port = 6379;
        public String password = "";

        @Comment(@CommentValue("Redis DB index (0–15 on most hosts)"))
        public int database = 0;

        @Comment(@CommentValue("Pub/sub channel for price updates across servers"))
        public String marketChannel = "soulbuyer:market";

        @Comment(@CommentValue("Pub/sub channel for global boosters across servers"))
        public String globalBoostersChannel = "soulbuyer:global-boosters";

        @Comment(@CommentValue("Market coefficient cache TTL in Redis, seconds"))
        public long cacheTtlSeconds = 300L;
    }

    public static final class BstatsSettings {

        @Comment({@CommentValue("Send anonymous metrics to https://bstats.org")})
        public boolean enabled = true;
    }

    public static final class LocaleSettings {
        @Comment({
                @CommentValue("=== Plugin language (GUI, button lore, chat, command errors) ==="),
                @CommentValue("Strings live in plugins/SoulBuyer/lang/<code>.yml"),
                @CommentValue("The plugin does not translate for you — it picks the matching file."),
                @CommentValue("Edit YAML or copy en.yml to your own xx.yml."),
                @CommentValue("After lang changes: /soulbuyer admin reload (or restart the server)."),
                @CommentValue(""),
                @CommentValue("How to pick the language for players (locale-mode):"),
                @CommentValue(""),
                @CommentValue("CLIENT — default. Uses each player's Minecraft language setting:"),
                @CommentValue("  • lang/<code>.yml exists for the client language → that file"),
                @CommentValue("  • ru / ru_ru → ru.yml (when present)"),
                @CommentValue("  • otherwise → fallback-locale (usually en)"),
                @CommentValue("  Good when the server has mixed RU and EN players."),
                @CommentValue(""),
                @CommentValue("SERVER — one language for everyone:"),
                @CommentValue("  • set server-locale below (e.g. ru)"),
                @CommentValue("  • player client language is ignored"),
                @CommentValue("  • everyone sees the same strings from one YAML"),
                @CommentValue(""),
                @CommentValue("Allowed locale-mode: CLIENT or SERVER (case-insensitive)."),
                @CommentValue(""),
                @CommentValue("Server-wide locale (server-locale). Only used when locale-mode: SERVER."),
                @CommentValue("Code = file name: plugins/SoulBuyer/lang/<code>.yml"),
                @CommentValue("Bundled in JAR: en, ru, fi. Any *.yml in lang/ is loaded on startup and reload."),
                @CommentValue(""),
                @CommentValue("Example — Russian for everyone:"),
                @CommentValue("  locale-mode: SERVER"),
                @CommentValue("  server-locale: ru"),
                @CommentValue(""),
                @CommentValue("Example — English for everyone:"),
                @CommentValue("  locale-mode: SERVER"),
                @CommentValue("  server-locale: en"),
                @CommentValue(""),
                @CommentValue("Custom locale: add lang/de.yml and set server-locale: de"),
                @CommentValue("(server-locale is ignored when locale-mode is CLIENT)."),
                @CommentValue(""),
                @CommentValue("Missing keys in a locale file are taken from fallback-locale."),
        })
        public String localeMode = "CLIENT";

        public String serverLocale = "en";

        @Comment({
                @CommentValue("Fallback locale: missing keys in a file + CLIENT when no lang/<code>.yml matches."),
                @CommentValue("Usually en."),
        })
        public String fallbackLocale = "en";
    }

    public static final class EconomySettings {
        @Comment({
                @CommentValue("true — payouts via PlayerPoints; false — via Vault."),
                @CommentValue("On Folia without Vault: install PlayerPoints and set true, or the plugin auto-switches if Vault is missing but PP is present.")
        })
        public boolean playerPointsEnabled = false;

        @Comment(@CommentValue("true — separate donate menu paid in PlayerPoints + regular menu paid in Vault"))
        public boolean donateBuyerEnabled = false;
    }

    public static final class CommandsSettings {
        public String main = "soulbuyer";

        @Comment({
                @CommentValue("Common player-facing aliases (English):"),
                @CommentValue("buyer, sell, sb, rbuyer — open the buyer menu")
        })
        public List<String> openAliases = defaultOpenAliases();

        public DonateBuyerCommandsSettings donateBuyer = new DonateBuyerCommandsSettings();
    }

    public static final class DonateBuyerCommandsSettings {
        public String main = "donbuyer";

        @Comment(@CommentValue("Donate buyer aliases: /dbuyer, /ppbuyer, …"))
        public List<String> openAliases = defaultDonateOpenAliases();
    }

    public static final class PermissionsSettings {
        public String use = "soulbuyer.use";
        public String admin = "soulbuyer.admin";
        public String autosell = "soulbuyer.autosell";
        public String donate = "soulbuyer.donate";
        public String boosters = "soulbuyer.boosters";
    }

    public static final class MarketSettings {
        @Comment(@CommentValue("Lower bound of the coefficient (0.25 = price won't drop below 25% of base)"))
        public double minCoefficient = 0.25D;

        @Comment(@CommentValue("How much the coefficient drops per sold unit of an item-id"))
        public double dropPerUnit = 0.0005D;

        @Comment(@CommentValue("How much the coefficient rises toward 1.0 per decay tick"))
        public double decayPerInterval = 0.002D;

        @Comment(@CommentValue("How often to run decay, seconds"))
        public int decayIntervalSeconds = 300;

        @Comment(@CommentValue("How often to flush the sales buffer to disk/DB, ms"))
        public long saleFlushIntervalMs = 5000L;

        @Comment(@CommentValue("How many sale records to write per batch"))
        public int saleFlushBatchSize = 100;
    }

    @NewLine
    @Comment({
            @CommentValue("=== BUYER CATALOG ROTATION ==="),
            @CommentValue("Periodically changes which items are buyable and resets market coefficients,"),
            @CommentValue("so prices don't stay stuck too high/low from mass selling."),
            @CommentValue("Full item list is in items.yml; only rotation rules are configured here.")
    })
    public CatalogRotationSettings catalogRotation = new CatalogRotationSettings();

    public static final class CatalogRotationSettings {

        @Comment(@CommentValue("true — rotate the catalog on a timer; false — buy everything from items.yml"))
        public boolean enabled = true;

        @Comment(@CommentValue("Seconds between catalog changes (3600 = 1 hour)"))
        public int intervalSeconds = 3600;

        @Comment(@CommentValue("How many items are active at once (from the full items.yml pool)"))
        public int activeItemCount = 48;

        @Comment(@CommentValue("Minimum items from each category in one rotation (when enough exist in the pool)"))
        public int minItemsPerCategory = 4;

        @Comment(@CommentValue("Reset market coefficients to 1.0 for the new set on each rotation"))
        public boolean resetMarketOnRotation = true;

        @NewLine
        @Comment(@CommentValue("Notify players in chat when the catalog changes"))
        public NotifySettings notify = new NotifySettings();

        public static final class NotifySettings {

            @Comment(@CommentValue("true — chat message; false — silent rotation"))
            public boolean enabled = true;
        }
    }

    @NewLine
    @Comment({
            @CommentValue("=== CATEGORY ICON ANIMATION IN GUI ==="),
            @CommentValue("Category filter buttons cycle real items from the current buyer catalog on a timer."),
            @CommentValue("Button name and lore stay from lang; only the icon changes.")
    })
    public CategoryIconAnimationSettings categoryIconAnimation = new CategoryIconAnimationSettings();

    public static final class CategoryIconAnimationSettings {

        @Comment(@CommentValue("true — cycle item previews on category buttons; false — static material from gui/buyer.yml"))
        public boolean enabled = true;

        @Comment(@CommentValue("How often to change the icon, seconds (minimum 1)"))
        public int intervalSeconds = 3;
    }

    public static final class ProgressionSettings {
        @Comment(@CommentValue("LuckPerms node → money and points multiplier (highest granted value wins)"))
        public Map<String, Double> permissionMultipliers = defaultPermissionMultipliers();

        @Comment(@CommentValue("false — don't award progression points and category XP from sales"))
        public boolean awardPoints = true;

        @Comment(@CommentValue("Extra points per Vault currency unit earned from a sale (if award-points: true)"))
        public double pointsPerCurrency = 0.1D;

        @Comment(@CommentValue("+% income per level of XP in the dominant category"))
        public double dominantCategoryBonusPerLevel = 0.5D;

        @Comment(@CommentValue("How much category XP equals 1 level for the bonus (not raw XP)"))
        public double categoryXpPerLevel = 1000.0D;

        @Comment(@CommentValue("Cap on the category income multiplier (1.0 = no bonus, 2.5 = +150%)"))
        public double maxCategoryBonus = 2.5D;

        @Comment(@CommentValue("Max price per unit in calculations (runaway protection)"))
        public double maxUnitPrice = 1_000_000.0D;

        @Comment(@CommentValue("Max Vault/PlayerPoints payout per single sale"))
        public double maxPayoutPerSale = 50_000_000.0D;

        @Comment(@CommentValue("How much category XP to award per progression point earned"))
        public double categoryXpPerPoint = 1.0D;
    }

    public static final class AutosellSettings {
        @Comment(@CommentValue("false — autosell button replaced with glass, pickup does not sell"))
        public boolean enabled = true;

        @Comment(@CommentValue("Delay in ticks after pickup before selling"))
        public int pickupDelayTicks = 1;

        @Comment(@CommentValue("New players: autosell disabled by default"))
        public boolean defaultEnabled = false;

        @Comment(@CommentValue("pickup — on pickup | buyer — when opening the menu | chest — sell chest contents on open"))
        public String defaultTrigger = "pickup";

        @Comment(@CommentValue("actionbar | chat | off"))
        public String defaultNotify = "actionbar";

        @Comment(@CommentValue("Don't sell items below this price per unit (after market and multipliers)"))
        public double defaultMinUnitPrice = 0D;

        @Comment(@CommentValue("Default categories for autosell"))
        public List<String> defaultCategories = List.of("ores", "mobs", "plants", "blocks", "misc");

        @Comment(@CommentValue("Minimum price steps in the GUI (button cycles through these)"))
        public List<Double> minUnitPriceSteps = List.of(0D, 1D, 5D, 10D, 50D);

        @Comment(@CommentValue("Where to pay when dual buyer is enabled: vault | player-points (only if economy.donate-buyer-enabled)"))
        public String defaultPayout = "vault";
    }

    public static final class BoostersSettings {
        @Comment(@CommentValue("false — boosters button hidden, purchases disabled"))
        public boolean enabled = true;

        @Comment(@CommentValue("false — admin global booster commands and their effect disabled"))
        public boolean globalEnabled = true;

        @Comment(@CommentValue("progression_points | vault | playerpoints"))
        public String currency = "progression_points";

        @Comment(@CommentValue("offer id → booster parameters"))
        public Map<String, BoosterOfferSettings> offers = defaultBoosterOffers();
    }

    public static final class BoosterOfferSettings {
        @Comment(@CommentValue("multiplier | money | limit"))
        public String type = "multiplier";

        @Comment(@CommentValue("multiplier: added to multiplier; money/limit: effect multiplier (1.25, 2.0)"))
        public double effect = 0.5D;

        @Comment(@CommentValue("Booster duration, seconds"))
        public int durationSeconds = 3600;

        @Comment(@CommentValue("Price in boosters.currency"))
        public double price = 125D;

        @Comment(@CommentValue("Icon material in the boosters GUI"))
        public String material = "EXPERIENCE_BOTTLE";

        @Comment(@CommentValue("Name key in lang/*.yml"))
        public String nameKey = "gui.boosters.offer-multiplier";

        @Comment(@CommentValue("Lore keys in lang/*.yml"))
        public List<String> loreKeys = List.of("gui.boosters.offer-lore");
    }

    public static final class SellLimitsSettings {
        @Comment(@CommentValue("false — limits are not checked"))
        public boolean enabled = true;

        @Comment(@CommentValue("Default items/day per item-id without a special permission"))
        public int defaultPerItem = 64;

        @Comment(@CommentValue("LuckPerms node → items/day (highest granted value wins)"))
        public Map<String, Integer> permissionLimits = defaultSellLimitPermissions();
    }

    public static final class CategorySettings {
        @Comment(@CommentValue("Key in lang/*.yml, e.g. categories.ores"))
        public String langKey = "";

        public int order = 0;
    }

    public static final class SellableItemSettings {
        public String material = "STONE";
        public String category = "misc";
        public double basePrice = 1.0D;
        public double basePoints = 0.1D;

        @Serializer(SoulBuyerSerializerConfig.NullableCmdSerializer.class)
        public Integer customModelData = -1;

        @Comment(@CommentValue("GUI icon material (empty = auto: PAPER+model or trim preview on chestplate for smithing templates)"))
        public String displayMaterial = "";
    }

    private static Map<String, CategorySettings> defaultCategories() {
        Map<String, CategorySettings> categories = new LinkedHashMap<>();
        categories.put("ores", category("categories.ores", 1));
        categories.put("mobs", category("categories.mobs", 2));
        categories.put("plants", category("categories.plants", 3));
        categories.put("blocks", category("categories.blocks", 4));
        categories.put("misc", category("categories.misc", 5));
        return categories;
    }

    private static CategorySettings category(String langKey, int order) {
        CategorySettings settings = new CategorySettings();
        settings.langKey = langKey;
        settings.order = order;
        return settings;
    }

    private static Map<String, Double> defaultPermissionMultipliers() {
        Map<String, Double> multipliers = new LinkedHashMap<>();
        multipliers.put("soulbuyer.multiplier.vip", 1.1D);
        multipliers.put("soulbuyer.multiplier.premium", 1.25D);
        return multipliers;
    }

    private static List<String> defaultOpenAliases() {
        return List.of("buyer", "sell", "sb", "rbuyer", "bs");
    }

    private static List<String> defaultDonateOpenAliases() {
        return List.of("dbuyer", "ppbuyer", "donatesell");
    }

    private static Map<String, BoosterOfferSettings> defaultBoosterOffers() {
        Map<String, BoosterOfferSettings> offers = new LinkedHashMap<>();

        BoosterOfferSettings multiplier = new BoosterOfferSettings();
        multiplier.type = "multiplier";
        multiplier.effect = 0.5D;
        multiplier.durationSeconds = 3600;
        multiplier.price = 125D;
        multiplier.material = "EXPERIENCE_BOTTLE";
        multiplier.nameKey = "gui.boosters.offer-multiplier";
        offers.put("multiplier", multiplier);

        BoosterOfferSettings money = new BoosterOfferSettings();
        money.type = "money";
        money.effect = 1.25D;
        money.durationSeconds = 3600;
        money.price = 175D;
        money.material = "GOLD_INGOT";
        money.nameKey = "gui.boosters.offer-money";
        offers.put("money", money);

        BoosterOfferSettings limit = new BoosterOfferSettings();
        limit.type = "limit";
        limit.effect = 2.0D;
        limit.durationSeconds = 1800;
        limit.price = 75D;
        limit.material = "CHEST";
        limit.nameKey = "gui.boosters.offer-limit";
        offers.put("limit", limit);

        return offers;
    }

    private static Map<String, Integer> defaultSellLimitPermissions() {
        Map<String, Integer> limits = new LinkedHashMap<>();
        limits.put("soulbuyer.limit.vip", 128);
        limits.put("soulbuyer.limit.premium", 256);
        return limits;
    }
}
