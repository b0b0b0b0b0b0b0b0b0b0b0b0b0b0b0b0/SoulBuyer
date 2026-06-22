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

    @Comment(@CommentValue("Подробные логи в консоль: bootstrap, команды, продажи, storage. На проде — false."))
    public boolean debug = false;

    @NewLine
    @Comment({
            @CommentValue("=== РЕЖИМ ХРАНЕНИЯ ДАННЫХ ==="),
            @CommentValue("Куда плагин сохраняет прогресс игроков (очки, категории) и состояние рынка."),
            @CommentValue("flat   — YAML на игрока + market.yml (один сервер, без MySQL/Redis)."),
            @CommentValue("sqlite — файл data/data.db (один сервер, без MySQL/Redis)."),
            @CommentValue("mysql  — MySQL + Redis для сети из нескольких серверов."),
            @CommentValue("Предметы скупки — в отдельном файле items.yml.")
    })
    public String storageType = "flat";

    @NewLine
    @Comment({
            @CommentValue("=== ПУТИ ДАННЫХ (flat / sqlite) ==="),
            @CommentValue("При storage-type: mysql эта секция не используется (данные в MySQL)."),
            @CommentValue("При storage-type: flat — создаются папки/файлы ниже в plugins/SoulBuyer/.")
    })
    public StorageSettings storage = new StorageSettings();

    @NewLine
    @Comment({
            @CommentValue("=== СЕТЬ СЕРВЕРОВ ==="),
            @CommentValue("Для одного сервера (flat или sqlite): оставь single-server: true."),
            @CommentValue("Для сети (mysql): у каждого сервера свой server-id, single-server: false, Redis включён.")
    })
    public NetworkSettings network = new NetworkSettings();

    @NewLine
    @Comment({
            @CommentValue("=== MYSQL (только storage-type: mysql) ==="),
            @CommentValue("При flat/sqlite эту секцию можно не трогать — плагин её игнорирует."),
            @CommentValue("База должна существовать заранее; таблицы создаются автоматически при первом старте.")
    })
    public MysqlSettings mysql = new MysqlSettings();

    @NewLine
    @Comment({
            @CommentValue("=== REDIS (только mysql + сеть) ==="),
            @CommentValue("При flat/sqlite или single-server: true Redis не подключается."),
            @CommentValue("Нужен для общего рынка между несколькими серверами (pub/sub синхронизация цен).")
    })
    public RedisSettings redis = new RedisSettings();

    @NewLine
    @Comment(@CommentValue("Язык сообщений и GUI: файлы lang/ru.yml, lang/en.yml"))
    public LocaleSettings locale = new LocaleSettings();

    @NewLine
    @Comment({
            @CommentValue("=== ЭКОНОМИКА ВЫПЛАТ ==="),
            @CommentValue("player-points-enabled — нужен плагин PlayerPoints."),
            @CommentValue("donate-buyer-enabled: true — два меню: обычный (Vault) + донатный (/donbuyer, PlayerPoints)."),
            @CommentValue("donate-buyer-enabled: false при включённом PlayerPoints — одно меню, выплата только в поинты.")
    })
    public EconomySettings economy = new EconomySettings();

    @NewLine
    @Comment({
            @CommentValue("=== КОМАНДЫ ==="),
            @CommentValue("main — основная команда (/soulbuyer). Менять только при конфликте с другим плагином."),
            @CommentValue("open-aliases — короткие команды, открывают GUI скупщика (как /buyer, /sell)."),
            @CommentValue("donate-buyer — команды донатного скупщика (только если economy.donate-buyer-enabled: true)."),
            @CommentValue("После изменения aliases нужен перезапуск сервера.")
    })
    public CommandsSettings commands = new CommandsSettings();

    @NewLine
    @Comment(@CommentValue("Permission-ноды (можно переименовать под свой LuckPerms)"))
    public PermissionsSettings permissions = new PermissionsSettings();

    @NewLine
    @Comment({
            @CommentValue("=== ДИНАМИЧЕСКИЙ РЫНОК ==="),
            @CommentValue("Чем больше игроки продают один ресурс — тем ниже коэффициент цены."),
            @CommentValue("decay постепенно возвращает коэффициент к 1.0, если ресурс перестают продавать.")
    })
    public MarketSettings market = new MarketSettings();

    @NewLine
    @Comment({
            @CommentValue("=== ПРОГРЕССИЯ ИГРОКА ==="),
            @CommentValue("Очки за продажи, множители по permission, бонус от главной категории.")
    })
    public ProgressionSettings progression = new ProgressionSettings();

    @NewLine
    @Comment({
            @CommentValue("=== АВТОПРОДАЖА ==="),
            @CommentValue("enabled — показывать кнопку и работу автопродажи на сервере."),
            @CommentValue("Право soulbuyer.autosell — выдавать донатерам (LuckPerms).")
    })
    public AutosellSettings autosell = new AutosellSettings();

    @NewLine
    @Comment({
            @CommentValue("=== БУСТЕРЫ ==="),
            @CommentValue("Магазин временных усилений в меню скупщика."),
            @CommentValue("currency: progression_points | vault | playerpoints")
    })
    public BoostersSettings boosters = new BoostersSettings();

    @NewLine
    @Comment({
            @CommentValue("=== ЛИМИТЫ ПРОДАЖ ==="),
            @CommentValue("Персональный дневной лимит на каждый item-id (анти-демпинг)."),
            @CommentValue("permission-limits — LuckPerms-нода → лимит шт./сутки (берётся максимальный).")
    })
    public SellLimitsSettings sellLimits = new SellLimitsSettings();

    @NewLine
    @Comment({
            @CommentValue("=== КАТЕГОРИИ РЕСУРСОВ ==="),
            @CommentValue("id категории → ключ названия в lang/*.yml (categories.ores и т.д.)."),
            @CommentValue("order — порядок в GUI каталога (меньше = выше).")
    })
    public Map<String, CategorySettings> categories = defaultCategories();

    public static final class StorageSettings {
        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/players/{uuid}.yml — очки и XP категорий"))
        public String playersFolder = "data/players";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/autosell/{uuid}.yml — настройки автопродажи"))
        public String autosellFolder = "data/autosell";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/boosters/{uuid}.yml — активные бустеры"))
        public String boostersFolder = "data/boosters";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/sell-limits/{uuid}.yml — продажи за период"))
        public String sellLimitsFolder = "data/sell-limits";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/market.yml — коэффициенты рынка"))
        public String marketFile = "data/market.yml";

        @Comment(@CommentValue("flat: plugins/SoulBuyer/data/rotation.yml — текущая ротация скупщика"))
        public String rotationFile = "data/rotation.yml";

        @Comment(@CommentValue("flat/sqlite: plugins/SoulBuyer/data/sales.log — журнал продаж"))
        public String salesLogFile = "data/sales.log";

        @Comment(@CommentValue("sqlite: plugins/SoulBuyer/data/data.db — файл SQLite"))
        public String sqliteFile = "data/data.db";

        @Comment(@CommentValue("sqlite: размер пула JDBC (обычно хватает 4)"))
        public int poolSize = 4;

        @Comment(@CommentValue("sqlite: таймаут подключения к БД, мс"))
        public long connectionTimeoutMs = 30000L;
    }

    public static final class NetworkSettings {
        @Comment(@CommentValue("Уникальный id этого Paper-сервера (server-1, lobby, survival-1, …)"))
        public String serverId = "server-1";

        @Comment({
                @CommentValue("true  — один сервер: Redis выключен, рынок локальный."),
                @CommentValue("false — сеть: нужны mysql + redis.enabled: true")
        })
        public boolean singleServer = true;
    }

    public static final class MysqlSettings {
        @Comment(@CommentValue("Хост MySQL (127.0.0.1 или IP VPS)"))
        public String host = "127.0.0.1";

        public int port = 3306;

        @Comment(@CommentValue("Имя базы (создай пустую БД до первого запуска)"))
        public String database = "soulbuyer";

        public String user = "root";
        public String password = "";

        @Comment(@CommentValue("Размер пула HikariCP"))
        public int poolSize = 10;

        public long connectionTimeoutMs = 30000L;
    }

    public static final class RedisSettings {
        @Comment(@CommentValue("false — принудительно без Redis (даже в сети, не рекомендуется)"))
        public boolean enabled = true;

        public String host = "127.0.0.1";
        public int port = 6379;
        public String password = "";

        @Comment(@CommentValue("Номер Redis DB (0–15 на большинстве хостингов)"))
        public int database = 0;

        @Comment(@CommentValue("Канал pub/sub для обновления цен между серверами"))
        public String marketChannel = "soulbuyer:market";

        @Comment(@CommentValue("TTL кэша коэффициентов в Redis, сек"))
        public long cacheTtlSeconds = 300L;
    }

    public static final class LocaleSettings {
        public String defaultLocale = "ru";
        public String fallbackLocale = "en";
    }

    public static final class EconomySettings {
        @Comment(@CommentValue("true — PlayerPoints на сервере; false — только Vault (как раньше)"))
        public boolean playerPointsEnabled = false;

        @Comment(@CommentValue("true — отдельное донатное меню за PlayerPoints + обычное за Vault"))
        public boolean donateBuyerEnabled = false;
    }

    public static final class CommandsSettings {
        public String main = "soulbuyer";

        @Comment({
                @CommentValue("Привычные алиасы для игроков (англ.):"),
                @CommentValue("buyer, sell, sb, rbuyer — открывают меню скупщика")
        })
        public List<String> openAliases = defaultOpenAliases();

        public DonateBuyerCommandsSettings donateBuyer = new DonateBuyerCommandsSettings();
    }

    public static final class DonateBuyerCommandsSettings {
        public String main = "donbuyer";

        @Comment(@CommentValue("Алиасы донатного скупщика: /dbuyer, /ppbuyer, …"))
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
        @Comment(@CommentValue("Нижняя граница коэффициента (0.25 = цена не упадёт ниже 25% от базовой)"))
        public double minCoefficient = 0.25D;

        @Comment(@CommentValue("На сколько падает коэффициент за каждую проданную единицу item-id"))
        public double dropPerUnit = 0.0005D;

        @Comment(@CommentValue("На сколько коэффициент растёт к 1.0 за один tick decay"))
        public double decayPerInterval = 0.002D;

        @Comment(@CommentValue("Как часто запускать decay, сек"))
        public int decayIntervalSeconds = 300;

        @Comment(@CommentValue("Как часто сбрасывать буфер продаж на диск/БД, мс"))
        public long saleFlushIntervalMs = 5000L;

        @Comment(@CommentValue("Сколько записей продаж писать одним batch"))
        public int saleFlushBatchSize = 100;
    }

    @NewLine
    @Comment({
            @CommentValue("=== РОТАЦИЯ АССОРТИМЕНТА СКУПЩИКА ==="),
            @CommentValue("Периодически меняет набор скупаемых предметов и сбрасывает рыночные коэффициенты,"),
            @CommentValue("чтобы цены не застревали слишком высокими/низкими от массовых продаж."),
            @CommentValue("Полный список предметов — в items.yml; здесь только правила ротации.")
    })
    public CatalogRotationSettings catalogRotation = new CatalogRotationSettings();

    public static final class CatalogRotationSettings {

        @Comment(@CommentValue("true — включить смену ассортимента по таймеру; false — скупается всё из items.yml"))
        public boolean enabled = true;

        @Comment(@CommentValue("Через сколько секунд менять набор предметов (3600 = 1 час)"))
        public int intervalSeconds = 3600;

        @Comment(@CommentValue("Сколько предметов одновременно в скупщике (из всего пула items.yml)"))
        public int activeItemCount = 48;

        @Comment(@CommentValue("Минимум предметов из каждой категории в одной ротации (если хватает в пуле)"))
        public int minItemsPerCategory = 4;

        @Comment(@CommentValue("Сбросить коэффициенты рынка на 1.0 для нового набора при каждой ротации"))
        public boolean resetMarketOnRotation = true;

        @NewLine
        @Comment(@CommentValue("Оповещение игроков в чат при смене ассортимента"))
        public NotifySettings notify = new NotifySettings();

        public static final class NotifySettings {

            @Comment(@CommentValue("true — писать в чат; false — тихая смена"))
            public boolean enabled = true;
        }
    }

    @NewLine
    @Comment({
            @CommentValue("=== АНИМАЦИЯ ИКОНОК КАТЕГОРИЙ В GUI ==="),
            @CommentValue("На кнопках фильтров по таймеру показываются реальные предметы из текущего ассортимента скупщика."),
            @CommentValue("Имя и lore кнопки остаются из lang; меняется только иконка.")
    })
    public CategoryIconAnimationSettings categoryIconAnimation = new CategoryIconAnimationSettings();

    public static final class CategoryIconAnimationSettings {

        @Comment(@CommentValue("true — крутить превью предметов на кнопках категорий; false — статичный material из gui/buyer.yml"))
        public boolean enabled = true;

        @Comment(@CommentValue("Как часто менять иконку, сек (минимум 1)"))
        public int intervalSeconds = 3;
    }

    public static final class ProgressionSettings {
        @Comment(@CommentValue("LuckPerms-нода → множитель к деньгам и очкам (берётся максимальный из выданных)"))
        public Map<String, Double> permissionMultipliers = defaultPermissionMultipliers();

        @Comment(@CommentValue("Доп. очки за каждую монету Vault-валюты от продажи"))
        public double pointsPerCurrency = 0.1D;

        @Comment(@CommentValue("+% к доходу за каждый уровень XP в доминирующей категории"))
        public double dominantCategoryBonusPerLevel = 0.5D;

        @Comment(@CommentValue("Сколько category-xp = 1 уровень для бонуса (не сырой XP)"))
        public double categoryXpPerLevel = 1000.0D;

        @Comment(@CommentValue("Потолок множителя от категории (1.0 = без бонуса, 2.5 = +150%)"))
        public double maxCategoryBonus = 2.5D;

        @Comment(@CommentValue("Макс. цена за 1 шт. в расчёте (защита от разгона)"))
        public double maxUnitPrice = 1_000_000.0D;

        @Comment(@CommentValue("Макс. выплата Vault/PlayerPoints за одну продажу"))
        public double maxPayoutPerSale = 50_000_000.0D;

        @Comment(@CommentValue("Сколько XP категории начислять за 1 заработанное очко"))
        public double categoryXpPerPoint = 1.0D;
    }

    public static final class AutosellSettings {
        @Comment(@CommentValue("false — кнопка автопродажи заменяется стеклом, подбор не продаёт"))
        public boolean enabled = true;

        @Comment(@CommentValue("Задержка в тиках после подбора перед продажей"))
        public int pickupDelayTicks = 1;

        @Comment(@CommentValue("Новый игрок: автопродажа выключена"))
        public boolean defaultEnabled = false;

        @Comment(@CommentValue("pickup — при подборе | buyer — при открытии меню | chest — продажа содержимого сундука при открытии"))
        public String defaultTrigger = "pickup";

        @Comment(@CommentValue("actionbar | chat | off"))
        public String defaultNotify = "actionbar";

        @Comment(@CommentValue("Не продавать предметы дешевле этой цены за шт. (после рынка и множителей)"))
        public double defaultMinUnitPrice = 0D;

        @Comment(@CommentValue("Категории по умолчанию для автопродажи"))
        public List<String> defaultCategories = List.of("ores", "mobs", "plants", "blocks", "misc");

        @Comment(@CommentValue("Шаги минимальной цены в GUI (цикл кнопки)"))
        public List<Double> minUnitPriceSteps = List.of(0D, 1D, 5D, 10D, 50D);

        @Comment(@CommentValue("Куда платить при dual buyer: vault | player-points (только если economy.donate-buyer-enabled)"))
        public String defaultPayout = "vault";
    }

    public static final class BoostersSettings {
        @Comment(@CommentValue("false — кнопка бустеров скрыта, покупка недоступна"))
        public boolean enabled = true;

        @Comment(@CommentValue("progression_points | vault | playerpoints"))
        public String currency = "progression_points";

        @Comment(@CommentValue("id предложения → параметры бустера"))
        public Map<String, BoosterOfferSettings> offers = defaultBoosterOffers();
    }

    public static final class BoosterOfferSettings {
        @Comment(@CommentValue("multiplier | money | limit"))
        public String type = "multiplier";

        @Comment(@CommentValue("multiplier: +к множителю; money/limit: множитель эффекта (1.25, 2.0)"))
        public double effect = 0.5D;

        @Comment(@CommentValue("Длительность бустера, сек"))
        public int durationSeconds = 3600;

        @Comment(@CommentValue("Цена в валюте boosters.currency"))
        public double price = 125D;

        @Comment(@CommentValue("Material иконки в GUI бустеров"))
        public String material = "EXPERIENCE_BOTTLE";

        @Comment(@CommentValue("Ключ названия в lang/*.yml"))
        public String nameKey = "gui.boosters.offer-multiplier";

        @Comment(@CommentValue("Ключи lore в lang/*.yml"))
        public List<String> loreKeys = List.of("gui.boosters.offer-lore");
    }

    public static final class SellLimitsSettings {
        @Comment(@CommentValue("false — лимиты не проверяются"))
        public boolean enabled = true;

        @Comment(@CommentValue("Лимит шт./сутки на item-id без спец. permission"))
        public int defaultPerItem = 64;

        @Comment(@CommentValue("LuckPerms-нода → лимит шт./сутки (максимум из выданных)"))
        public Map<String, Integer> permissionLimits = defaultSellLimitPermissions();
    }

    public static final class CategorySettings {
        @Comment(@CommentValue("Ключ в lang/*.yml, например categories.ores"))
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
