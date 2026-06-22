package bm.b0b0b0.soulBuyer.config.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

public final class GuiBuyerSettings extends YamlSerializable {

    public GuiBuyerSettings() {
        super(SoulBuyerSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("Ключ заголовка окна в lang/*.yml"))
    public String titleKey = "gui.buyer.title";

    @Comment(@CommentValue("Размер GUI (54 = 6 рядов). Не меняй без правки слотов ниже."))
    public int size = 54;

    @Comment({
            @CommentValue("Разделитель между категориями и сеткой продажи (стеклянные панели)."),
            @CommentValue("По умолчанию слоты 10–16 — второй ряд под фильтрами категорий.")
    })
    public List<Integer> separatorSlots = List.of(10, 11, 12, 13, 14, 15, 16);

    @Comment({
            @CommentValue("Рабочие слоты — иконки скупаемых предметов с ценами."),
            @CommentValue("Клик — мгновенная продажа всего такого ресурса из инвентаря.")
    })
    public List<Integer> contentSlots = List.of(
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    );

    @NewLine
    @Comment({
            @CommentValue("Кнопки GUI. slot: -1 = только шаблон (filler/separator)."),
            @CommentValue("action: SELL_ALL | DECORATION | CATEGORY_FILTER | SORT_CYCLE | PAGE_PREV | PAGE_NEXT | …"),
    })
    public Map<String, GuiGeneralSettings.GuiElementSettings> elements = defaultElements();

    private static Map<String, GuiGeneralSettings.GuiElementSettings> defaultElements() {
        Map<String, GuiGeneralSettings.GuiElementSettings> elements = new LinkedHashMap<>();

        GuiGeneralSettings.GuiElementSettings border = new GuiGeneralSettings.GuiElementSettings();
        border.slot = -1;
        border.material = "BLACK_STAINED_GLASS_PANE";
        border.nameKey = "gui.buyer.border";
        border.action = "DECORATION";
        elements.put("border", border);

        GuiGeneralSettings.GuiElementSettings separator = new GuiGeneralSettings.GuiElementSettings();
        separator.slot = -1;
        separator.material = "GRAY_STAINED_GLASS_PANE";
        separator.nameKey = "gui.buyer.separator";
        separator.action = "DECORATION";
        elements.put("separator", separator);

        GuiGeneralSettings.GuiElementSettings stats = new GuiGeneralSettings.GuiElementSettings();
        stats.slot = 4;
        stats.material = "EXPERIENCE_BOTTLE";
        stats.nameKey = "gui.buyer.stats";
        stats.loreKeys = List.of("gui.buyer.stats-lore");
        stats.action = "DECORATION";
        elements.put("stats", stats);

        elements.put("category-all", categoryButton(1, "VAULT", "gui.buyer.category-all", "gui.buyer.category-all-lore", ""));
        elements.put("category-ores", categoryButton(2, "DIAMOND_ORE", "gui.buyer.category-ores", "gui.buyer.category-ores-lore", "ores"));
        elements.put("category-mobs", categoryButton(3, "ROTTEN_FLESH", "gui.buyer.category-mobs", "gui.buyer.category-mobs-lore", "mobs"));
        elements.put("category-plants", categoryButton(5, "WHEAT", "gui.buyer.category-plants", "gui.buyer.category-plants-lore", "plants"));
        elements.put("category-blocks", categoryButton(6, "COBBLESTONE", "gui.buyer.category-blocks", "gui.buyer.category-blocks-lore", "blocks"));
        elements.put("category-misc", categoryButton(7, "EMERALD", "gui.buyer.category-misc", "gui.buyer.category-misc-lore", "misc"));

        elements.put("page-prev", pageButton(36, "LIGHT_GRAY_DYE", "gui.buyer.page-prev", "gui.buyer.page-prev-lore", "PAGE_PREV"));
        elements.put("page-next", pageButton(44, "GRAY_DYE", "gui.buyer.page-next", "gui.buyer.page-next-lore", "PAGE_NEXT"));

        GuiGeneralSettings.GuiElementSettings marketStats = new GuiGeneralSettings.GuiElementSettings();
        marketStats.slot = 49;
        marketStats.material = "CLOCK";
        marketStats.nameKey = "gui.buyer.market-stats";
        marketStats.loreKeys = List.of("gui.buyer.market-stats-lore");
        marketStats.action = "DECORATION";
        elements.put("market-stats", marketStats);

        GuiGeneralSettings.GuiElementSettings sortCycle = new GuiGeneralSettings.GuiElementSettings();
        sortCycle.slot = 45;
        sortCycle.material = "COMPARATOR";
        sortCycle.nameKey = "gui.buyer.sort";
        sortCycle.loreKeys = List.of("gui.buyer.sort-lore");
        sortCycle.action = "SORT_CYCLE";
        elements.put("sort-cycle", sortCycle);

        elements.put("filler-46", fillerPane(46));
        elements.put("filler-47", fillerPane(47));
        elements.put("filler-50", fillerPane(50));

        GuiGeneralSettings.GuiElementSettings autosell = new GuiGeneralSettings.GuiElementSettings();
        autosell.slot = 52;
        autosell.material = "EMERALD";
        autosell.nameKey = "gui.buyer.autosell";
        autosell.loreKeys = List.of("gui.buyer.autosell-lore");
        autosell.action = "AUTOSELL";
        elements.put("autosell", autosell);

        GuiGeneralSettings.GuiElementSettings boosters = new GuiGeneralSettings.GuiElementSettings();
        boosters.slot = 51;
        boosters.material = "NETHER_STAR";
        boosters.nameKey = "gui.buyer.boosters";
        boosters.loreKeys = List.of("gui.buyer.boosters-lore");
        boosters.action = "BOOSTERS";
        elements.put("boosters", boosters);

        GuiGeneralSettings.GuiElementSettings boostersDisabled = new GuiGeneralSettings.GuiElementSettings();
        boostersDisabled.slot = -1;
        boostersDisabled.material = "GRAY_STAINED_GLASS_PANE";
        boostersDisabled.nameKey = "gui.buyer.boosters-disabled";
        boostersDisabled.loreKeys = List.of("gui.buyer.boosters-disabled-lore");
        boostersDisabled.action = "NONE";
        elements.put("boosters-disabled", boostersDisabled);

        GuiGeneralSettings.GuiElementSettings autosellDisabled = new GuiGeneralSettings.GuiElementSettings();
        autosellDisabled.slot = -1;
        autosellDisabled.material = "GRAY_STAINED_GLASS_PANE";
        autosellDisabled.nameKey = "gui.buyer.autosell-disabled";
        autosellDisabled.loreKeys = List.of("gui.buyer.autosell-disabled-lore");
        autosellDisabled.action = "NONE";
        elements.put("autosell-disabled", autosellDisabled);

        GuiGeneralSettings.GuiElementSettings sellAll = new GuiGeneralSettings.GuiElementSettings();
        sellAll.slot = 48;
        sellAll.material = "HOPPER";
        sellAll.nameKey = "gui.buyer.sell-all";
        sellAll.loreKeys = List.of("gui.buyer.sell-all-lore");
        sellAll.action = "SELL_ALL";
        elements.put("sell-all", sellAll);

        return elements;
    }

    private static GuiGeneralSettings.GuiElementSettings fillerPane(int slot) {
        GuiGeneralSettings.GuiElementSettings element = new GuiGeneralSettings.GuiElementSettings();
        element.slot = slot;
        element.material = "GRAY_STAINED_GLASS_PANE";
        element.nameKey = "gui.buyer.separator";
        element.action = "DECORATION";
        return element;
    }

    private static GuiGeneralSettings.GuiElementSettings categoryButton(
            int slot,
            String material,
            String nameKey,
            String loreKey,
            String categoryFilter
    ) {
        GuiGeneralSettings.GuiElementSettings element = new GuiGeneralSettings.GuiElementSettings();
        element.slot = slot;
        element.material = material;
        element.nameKey = nameKey;
        element.loreKeys = List.of(loreKey);
        element.action = "CATEGORY_FILTER";
        element.categoryFilter = categoryFilter;
        return element;
    }

    private static GuiGeneralSettings.GuiElementSettings pageButton(
            int slot,
            String material,
            String nameKey,
            String loreKey,
            String action
    ) {
        GuiGeneralSettings.GuiElementSettings element = new GuiGeneralSettings.GuiElementSettings();
        element.slot = slot;
        element.material = material;
        element.nameKey = nameKey;
        element.loreKeys = List.of(loreKey);
        element.action = action;
        return element;
    }
}
