package bm.b0b0b0.soulBuyer.config.settings;

import bm.b0b0b0.soulBuyer.util.MaterialParser;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SoulBuyerItemDefaults {

    private SoulBuyerItemDefaults() {
    }

    public static Map<String, SoulBuyerSettings.SellableItemSettings> create() {
        Map<String, SoulBuyerSettings.SellableItemSettings> items = new LinkedHashMap<>();
        addBlocks(items);
        addOres(items);
        addPlants(items);
        addMobs(items);
        addMisc(items);
        validateMaterials(items);
        return items;
    }

    private static void validateMaterials(Map<String, SoulBuyerSettings.SellableItemSettings> items) {
        for (Map.Entry<String, SoulBuyerSettings.SellableItemSettings> entry : items.entrySet()) {
            String materialName = entry.getValue().material;
            if (!MaterialParser.isKnown(materialName)) {
                throw new IllegalStateException(
                        "Default item '" + entry.getKey() + "' uses unknown material '" + materialName
                                + "' on this server version"
                );
            }
        }
    }

    private static void addBlocks(Map<String, SoulBuyerSettings.SellableItemSettings> items) {
        item(items, "cobblestone", "COBBLESTONE", "blocks", 0.45D, 0.04D);
        item(items, "stone", "STONE", "blocks", 0.45D, 0.04D);
        item(items, "deepslate", "DEEPSLATE", "blocks", 0.55D, 0.05D);
        item(items, "cobbled_deepslate", "COBBLED_DEEPSLATE", "blocks", 0.5D, 0.05D);
        item(items, "stone_bricks", "STONE_BRICKS", "blocks", 0.65D, 0.06D);
        item(items, "mossy_cobblestone", "MOSSY_COBBLESTONE", "blocks", 0.55D, 0.05D);
        item(items, "mossy_stone_bricks", "MOSSY_STONE_BRICKS", "blocks", 0.7D, 0.07D);
        item(items, "andesite", "ANDESITE", "blocks", 0.4D, 0.04D);
        item(items, "polished_andesite", "POLISHED_ANDESITE", "blocks", 0.45D, 0.04D);
        item(items, "diorite", "DIORITE", "blocks", 0.4D, 0.04D);
        item(items, "polished_diorite", "POLISHED_DIORITE", "blocks", 0.45D, 0.04D);
        item(items, "granite", "GRANITE", "blocks", 0.4D, 0.04D);
        item(items, "polished_granite", "POLISHED_GRANITE", "blocks", 0.45D, 0.04D);
        item(items, "tuff", "TUFF", "blocks", 0.35D, 0.03D);
        item(items, "calcite", "CALCITE", "blocks", 0.5D, 0.05D);
        item(items, "dripstone_block", "DRIPSTONE_BLOCK", "blocks", 0.55D, 0.05D);
        item(items, "dirt", "DIRT", "blocks", 0.25D, 0.02D);
        item(items, "coarse_dirt", "COARSE_DIRT", "blocks", 0.28D, 0.03D);
        item(items, "rooted_dirt", "ROOTED_DIRT", "blocks", 0.35D, 0.03D);
        item(items, "mud", "MUD", "blocks", 0.3D, 0.03D);
        item(items, "clay_ball", "CLAY_BALL", "blocks", 0.6D, 0.06D);
        item(items, "sand", "SAND", "blocks", 0.35D, 0.03D);
        item(items, "red_sand", "RED_SAND", "blocks", 0.38D, 0.04D);
        item(items, "gravel", "GRAVEL", "blocks", 0.35D, 0.03D);
        item(items, "soul_sand", "SOUL_SAND", "blocks", 0.5D, 0.05D);
        item(items, "soul_soil", "SOUL_SOIL", "blocks", 0.45D, 0.04D);
        item(items, "netherrack", "NETHERRACK", "blocks", 0.2D, 0.02D);
        item(items, "basalt", "BASALT", "blocks", 0.35D, 0.03D);
        item(items, "blackstone", "BLACKSTONE", "blocks", 0.4D, 0.04D);
        item(items, "polished_blackstone", "POLISHED_BLACKSTONE", "blocks", 0.48D, 0.05D);
        item(items, "end_stone", "END_STONE", "blocks", 0.55D, 0.05D);
        item(items, "obsidian", "OBSIDIAN", "blocks", 8.0D, 0.8D);
        item(items, "crying_obsidian", "CRYING_OBSIDIAN", "blocks", 10.0D, 1.0D);
        item(items, "bricks", "BRICKS", "blocks", 0.75D, 0.07D);
        item(items, "nether_bricks", "NETHER_BRICKS", "blocks", 0.6D, 0.06D);
        item(items, "red_nether_bricks", "RED_NETHER_BRICKS", "blocks", 0.65D, 0.06D);
        item(items, "sandstone", "SANDSTONE", "blocks", 0.45D, 0.04D);
        item(items, "red_sandstone", "RED_SANDSTONE", "blocks", 0.48D, 0.05D);
        item(items, "smooth_stone", "SMOOTH_STONE", "blocks", 0.5D, 0.05D);
        item(items, "glass", "GLASS", "blocks", 0.55D, 0.05D);
        item(items, "tinted_glass", "TINTED_GLASS", "blocks", 1.2D, 0.12D);
        item(items, "white_terracotta", "WHITE_TERRACOTTA", "blocks", 0.7D, 0.07D);
        item(items, "orange_terracotta", "ORANGE_TERRACOTTA", "blocks", 0.7D, 0.07D);
        item(items, "red_terracotta", "RED_TERRACOTTA", "blocks", 0.7D, 0.07D);
        item(items, "yellow_terracotta", "YELLOW_TERRACOTTA", "blocks", 0.7D, 0.07D);
        item(items, "lime_terracotta", "LIME_TERRACOTTA", "blocks", 0.7D, 0.07D);
        item(items, "light_blue_terracotta", "LIGHT_BLUE_TERRACOTTA", "blocks", 0.7D, 0.07D);
        item(items, "blue_terracotta", "BLUE_TERRACOTTA", "blocks", 0.7D, 0.07D);
        item(items, "purple_terracotta", "PURPLE_TERRACOTTA", "blocks", 0.7D, 0.07D);
        item(items, "white_concrete_powder", "WHITE_CONCRETE_POWDER", "blocks", 0.65D, 0.06D);
        item(items, "gray_concrete_powder", "GRAY_CONCRETE_POWDER", "blocks", 0.65D, 0.06D);
        item(items, "black_concrete_powder", "BLACK_CONCRETE_POWDER", "blocks", 0.65D, 0.06D);
        item(items, "white_concrete", "WHITE_CONCRETE", "blocks", 0.8D, 0.08D);
        item(items, "gray_concrete", "GRAY_CONCRETE", "blocks", 0.8D, 0.08D);
        item(items, "black_concrete", "BLACK_CONCRETE", "blocks", 0.8D, 0.08D);
        item(items, "oak_planks", "OAK_PLANKS", "blocks", 0.55D, 0.05D);
        item(items, "spruce_planks", "SPRUCE_PLANKS", "blocks", 0.55D, 0.05D);
        item(items, "birch_planks", "BIRCH_PLANKS", "blocks", 0.55D, 0.05D);
        item(items, "jungle_planks", "JUNGLE_PLANKS", "blocks", 0.55D, 0.05D);
        item(items, "acacia_planks", "ACACIA_PLANKS", "blocks", 0.55D, 0.05D);
        item(items, "dark_oak_planks", "DARK_OAK_PLANKS", "blocks", 0.55D, 0.05D);
        item(items, "mangrove_planks", "MANGROVE_PLANKS", "blocks", 0.58D, 0.06D);
        item(items, "cherry_planks", "CHERRY_PLANKS", "blocks", 0.6D, 0.06D);
        item(items, "bamboo_planks", "BAMBOO_PLANKS", "blocks", 0.52D, 0.05D);
        item(items, "crimson_planks", "CRIMSON_PLANKS", "blocks", 0.65D, 0.06D);
        item(items, "warped_planks", "WARPED_PLANKS", "blocks", 0.65D, 0.06D);
        item(items, "bookshelf", "BOOKSHELF", "blocks", 2.5D, 0.25D);
        item(items, "chiseled_bookshelf", "CHISELED_BOOKSHELF", "blocks", 3.0D, 0.3D);
        item(items, "glowstone", "GLOWSTONE", "blocks", 2.0D, 0.2D);
        item(items, "sea_lantern", "SEA_LANTERN", "blocks", 4.5D, 0.45D);
        item(items, "prismarine", "PRISMARINE", "blocks", 1.5D, 0.15D);
        item(items, "prismarine_bricks", "PRISMARINE_BRICKS", "blocks", 1.8D, 0.18D);
        item(items, "dark_prismarine", "DARK_PRISMARINE", "blocks", 2.0D, 0.2D);
        item(items, "moss_block", "MOSS_BLOCK", "blocks", 0.65D, 0.06D);
        item(items, "sculk", "SCULK", "blocks", 3.5D, 0.35D);
        item(items, "sculk_catalyst", "SCULK_CATALYST", "blocks", 12.0D, 1.2D);
        item(items, "sculk_sensor", "SCULK_SENSOR", "blocks", 5.0D, 0.5D);
        item(items, "sculk_shrieker", "SCULK_SHRIEKER", "blocks", 8.0D, 0.8D);
        item(items, "amethyst_block", "AMETHYST_BLOCK", "blocks", 4.0D, 0.4D);
        item(items, "hay_block", "HAY_BLOCK", "blocks", 2.5D, 0.25D);
        item(items, "dried_kelp_block", "DRIED_KELP_BLOCK", "blocks", 1.8D, 0.18D);
        item(items, "honey_block", "HONEY_BLOCK", "blocks", 6.0D, 0.6D);
        item(items, "slime_block", "SLIME_BLOCK", "blocks", 5.0D, 0.5D);
        item(items, "packed_ice", "PACKED_ICE", "blocks", 1.2D, 0.12D);
        item(items, "blue_ice", "BLUE_ICE", "blocks", 2.5D, 0.25D);
        item(items, "snow_block", "SNOW_BLOCK", "blocks", 0.4D, 0.04D);
        item(items, "mycelium", "MYCELIUM", "blocks", 1.5D, 0.15D);
        item(items, "podzol", "PODZOL", "blocks", 0.45D, 0.04D);
    }

    private static void addOres(Map<String, SoulBuyerSettings.SellableItemSettings> items) {
        item(items, "coal", "COAL", "ores", 2.0D, 0.2D);
        item(items, "charcoal", "CHARCOAL", "ores", 1.8D, 0.18D);
        item(items, "raw_iron", "RAW_IRON", "ores", 5.0D, 0.5D);
        item(items, "raw_gold", "RAW_GOLD", "ores", 8.0D, 0.8D);
        item(items, "raw_copper", "RAW_COPPER", "ores", 3.5D, 0.35D);
        item(items, "iron_ingot", "IRON_INGOT", "ores", 10.0D, 1.0D);
        item(items, "gold_ingot", "GOLD_INGOT", "ores", 15.0D, 1.5D);
        item(items, "copper_ingot", "COPPER_INGOT", "ores", 6.0D, 0.6D);
        item(items, "netherite_scrap", "NETHERITE_SCRAP", "ores", 85.0D, 8.5D);
        item(items, "netherite_ingot", "NETHERITE_INGOT", "ores", 350.0D, 35.0D);
        item(items, "iron_nugget", "IRON_NUGGET", "ores", 1.1D, 0.11D);
        item(items, "gold_nugget", "GOLD_NUGGET", "ores", 1.7D, 0.17D);
        item(items, "diamond", "DIAMOND", "ores", 50.0D, 5.0D);
        item(items, "emerald", "EMERALD", "ores", 40.0D, 4.0D);
        item(items, "lapis_lazuli", "LAPIS_LAZULI", "ores", 3.0D, 0.3D);
        item(items, "redstone", "REDSTONE", "ores", 2.5D, 0.25D);
        item(items, "quartz", "QUARTZ", "ores", 4.0D, 0.4D);
        item(items, "amethyst_shard", "AMETHYST_SHARD", "ores", 5.5D, 0.55D);
        item(items, "coal_block", "COAL_BLOCK", "ores", 18.0D, 1.8D);
        item(items, "iron_block", "IRON_BLOCK", "ores", 90.0D, 9.0D);
        item(items, "gold_block", "GOLD_BLOCK", "ores", 135.0D, 13.5D);
        item(items, "copper_block", "COPPER_BLOCK", "ores", 54.0D, 5.4D);
        item(items, "diamond_block", "DIAMOND_BLOCK", "ores", 450.0D, 45.0D);
        item(items, "emerald_block", "EMERALD_BLOCK", "ores", 360.0D, 36.0D);
        item(items, "lapis_block", "LAPIS_BLOCK", "ores", 27.0D, 2.7D);
        item(items, "redstone_block", "REDSTONE_BLOCK", "ores", 22.5D, 2.25D);
        item(items, "raw_iron_block", "RAW_IRON_BLOCK", "ores", 45.0D, 4.5D);
        item(items, "raw_gold_block", "RAW_GOLD_BLOCK", "ores", 72.0D, 7.2D);
        item(items, "raw_copper_block", "RAW_COPPER_BLOCK", "ores", 31.5D, 3.15D);
        item(items, "ancient_debris", "ANCIENT_DEBRIS", "ores", 120.0D, 12.0D);
        item(items, "glowstone_dust", "GLOWSTONE_DUST", "ores", 1.5D, 0.15D);
        item(items, "blaze_powder", "BLAZE_POWDER", "ores", 6.0D, 0.6D);
        item(items, "magma_cream", "MAGMA_CREAM", "ores", 5.5D, 0.55D);
        item(items, "ender_eye", "ENDER_EYE", "ores", 18.0D, 1.8D);
        item(items, "shulker_shell", "SHULKER_SHELL", "ores", 45.0D, 4.5D);
        item(items, "echo_shard", "ECHO_SHARD", "ores", 25.0D, 2.5D);
        item(items, "disc_fragment_5", "DISC_FRAGMENT_5", "ores", 35.0D, 3.5D);
        item(items, "nautilus_shell", "NAUTILUS_SHELL", "ores", 8.0D, 0.8D);
        item(items, "heart_of_the_sea", "HEART_OF_THE_SEA", "ores", 55.0D, 5.5D);
        item(items, "prismarine_shard", "PRISMARINE_SHARD", "ores", 2.5D, 0.25D);
        item(items, "prismarine_crystals", "PRISMARINE_CRYSTALS", "ores", 3.5D, 0.35D);
        item(items, "clay", "CLAY", "ores", 2.4D, 0.24D);
        item(items, "brick", "BRICK", "ores", 0.9D, 0.09D);
        item(items, "experience_bottle", "EXPERIENCE_BOTTLE", "ores", 12.0D, 1.2D);
    }

    private static void addPlants(Map<String, SoulBuyerSettings.SellableItemSettings> items) {
        item(items, "wheat", "WHEAT", "plants", 1.5D, 0.15D);
        item(items, "wheat_seeds", "WHEAT_SEEDS", "plants", 0.4D, 0.04D);
        item(items, "carrot", "CARROT", "plants", 1.2D, 0.12D);
        item(items, "potato", "POTATO", "plants", 1.2D, 0.12D);
        item(items, "baked_potato", "BAKED_POTATO", "plants", 2.0D, 0.2D);
        item(items, "beetroot", "BEETROOT", "plants", 1.0D, 0.1D);
        item(items, "beetroot_seeds", "BEETROOT_SEEDS", "plants", 0.4D, 0.04D);
        item(items, "melon_slice", "MELON_SLICE", "plants", 0.8D, 0.08D);
        item(items, "pumpkin", "PUMPKIN", "plants", 2.5D, 0.25D);
        item(items, "carved_pumpkin", "CARVED_PUMPKIN", "plants", 3.0D, 0.3D);
        item(items, "melon", "MELON", "plants", 4.5D, 0.45D);
        item(items, "sweet_berries", "SWEET_BERRIES", "plants", 1.1D, 0.11D);
        item(items, "glow_berries", "GLOW_BERRIES", "plants", 2.2D, 0.22D);
        item(items, "apple", "APPLE", "plants", 1.8D, 0.18D);
        item(items, "golden_apple", "GOLDEN_APPLE", "plants", 45.0D, 4.5D);
        item(items, "enchanted_golden_apple", "ENCHANTED_GOLDEN_APPLE", "plants", 500.0D, 50.0D);
        item(items, "chorus_fruit", "CHORUS_FRUIT", "plants", 3.5D, 0.35D);
        item(items, "popped_chorus_fruit", "POPPED_CHORUS_FRUIT", "plants", 4.0D, 0.4D);
        item(items, "sugar_cane", "SUGAR_CANE", "plants", 0.9D, 0.09D);
        item(items, "bamboo", "BAMBOO", "plants", 0.35D, 0.03D);
        item(items, "cactus", "CACTUS", "plants", 0.8D, 0.08D);
        item(items, "kelp", "KELP", "plants", 0.5D, 0.05D);
        item(items, "dried_kelp", "DRIED_KELP", "plants", 0.7D, 0.07D);
        item(items, "sea_pickle", "SEA_PICKLE", "plants", 1.5D, 0.15D);
        item(items, "cocoa_beans", "COCOA_BEANS", "plants", 1.4D, 0.14D);
        item(items, "nether_wart", "NETHER_WART", "plants", 2.0D, 0.2D);
        item(items, "brown_mushroom", "BROWN_MUSHROOM", "plants", 1.0D, 0.1D);
        item(items, "red_mushroom", "RED_MUSHROOM", "plants", 1.0D, 0.1D);
        item(items, "crimson_fungus", "CRIMSON_FUNGUS", "plants", 1.5D, 0.15D);
        item(items, "warped_fungus", "WARPED_FUNGUS", "plants", 1.5D, 0.15D);
        item(items, "crimson_roots", "CRIMSON_ROOTS", "plants", 0.8D, 0.08D);
        item(items, "warped_roots", "WARPED_ROOTS", "plants", 0.8D, 0.08D);
        item(items, "nether_sprouts", "NETHER_SPROUTS", "plants", 0.7D, 0.07D);
        item(items, "twisting_vines", "TWISTING_VINES", "plants", 0.9D, 0.09D);
        item(items, "weeping_vines", "WEEPING_VINES", "plants", 0.9D, 0.09D);
        item(items, "vine", "VINE", "plants", 0.6D, 0.06D);
        item(items, "lily_pad", "LILY_PAD", "plants", 0.8D, 0.08D);
        item(items, "big_dripleaf", "BIG_DRIPLEAF", "plants", 1.2D, 0.12D);
        item(items, "spore_blossom", "SPORE_BLOSSOM", "plants", 2.5D, 0.25D);
        item(items, "pitcher_pod", "PITCHER_POD", "plants", 2.0D, 0.2D);
        item(items, "torchflower_seeds", "TORCHFLOWER_SEEDS", "plants", 1.8D, 0.18D);
        item(items, "pitcher_plant", "PITCHER_PLANT", "plants", 2.5D, 0.25D);
        item(items, "torchflower", "TORCHFLOWER", "plants", 2.2D, 0.22D);
        item(items, "oak_sapling", "OAK_SAPLING", "plants", 0.6D, 0.06D);
        item(items, "spruce_sapling", "SPRUCE_SAPLING", "plants", 0.6D, 0.06D);
        item(items, "birch_sapling", "BIRCH_SAPLING", "plants", 0.6D, 0.06D);
        item(items, "jungle_sapling", "JUNGLE_SAPLING", "plants", 0.65D, 0.06D);
        item(items, "acacia_sapling", "ACACIA_SAPLING", "plants", 0.65D, 0.06D);
        item(items, "dark_oak_sapling", "DARK_OAK_SAPLING", "plants", 0.65D, 0.06D);
        item(items, "cherry_sapling", "CHERRY_SAPLING", "plants", 0.7D, 0.07D);
        item(items, "mangrove_propagule", "MANGROVE_PROPAGULE", "plants", 0.75D, 0.07D);
        item(items, "oak_log", "OAK_LOG", "plants", 1.2D, 0.12D);
        item(items, "spruce_log", "SPRUCE_LOG", "plants", 1.2D, 0.12D);
        item(items, "birch_log", "BIRCH_LOG", "plants", 1.2D, 0.12D);
        item(items, "jungle_log", "JUNGLE_LOG", "plants", 1.25D, 0.12D);
        item(items, "acacia_log", "ACACIA_LOG", "plants", 1.25D, 0.12D);
        item(items, "dark_oak_log", "DARK_OAK_LOG", "plants", 1.25D, 0.12D);
        item(items, "mangrove_log", "MANGROVE_LOG", "plants", 1.3D, 0.13D);
        item(items, "cherry_log", "CHERRY_LOG", "plants", 1.35D, 0.13D);
        item(items, "crimson_stem", "CRIMSON_STEM", "plants", 1.4D, 0.14D);
        item(items, "warped_stem", "WARPED_STEM", "plants", 1.4D, 0.14D);
        item(items, "stripped_oak_log", "STRIPPED_OAK_LOG", "plants", 1.35D, 0.13D);
        item(items, "stripped_spruce_log", "STRIPPED_SPRUCE_LOG", "plants", 1.35D, 0.13D);
        item(items, "oak_leaves", "OAK_LEAVES", "plants", 0.3D, 0.03D);
        item(items, "azalea", "AZALEA", "plants", 1.0D, 0.1D);
        item(items, "flowering_azalea", "FLOWERING_AZALEA", "plants", 1.5D, 0.15D);
        item(items, "dandelion", "DANDELION", "plants", 0.5D, 0.05D);
        item(items, "poppy", "POPPY", "plants", 0.5D, 0.05D);
        item(items, "blue_orchid", "BLUE_ORCHID", "plants", 0.8D, 0.08D);
        item(items, "allium", "ALLIUM", "plants", 0.6D, 0.06D);
        item(items, "azure_bluet", "AZURE_BLUET", "plants", 0.55D, 0.05D);
        item(items, "red_tulip", "RED_TULIP", "plants", 0.6D, 0.06D);
        item(items, "orange_tulip", "ORANGE_TULIP", "plants", 0.6D, 0.06D);
        item(items, "white_tulip", "WHITE_TULIP", "plants", 0.6D, 0.06D);
        item(items, "pink_tulip", "PINK_TULIP", "plants", 0.6D, 0.06D);
        item(items, "oxeye_daisy", "OXEYE_DAISY", "plants", 0.55D, 0.05D);
        item(items, "cornflower", "CORNFLOWER", "plants", 0.6D, 0.06D);
        item(items, "lily_of_the_valley", "LILY_OF_THE_VALLEY", "plants", 0.7D, 0.07D);
        item(items, "sunflower", "SUNFLOWER", "plants", 0.9D, 0.09D);
        item(items, "rose_bush", "ROSE_BUSH", "plants", 0.85D, 0.08D);
        item(items, "peony", "PEONY", "plants", 0.85D, 0.08D);
        item(items, "lilac", "LILAC", "plants", 0.85D, 0.08D);
        item(items, "pink_petals", "PINK_PETALS", "plants", 0.5D, 0.05D);
    }

    private static void addMobs(Map<String, SoulBuyerSettings.SellableItemSettings> items) {
        item(items, "rotten_flesh", "ROTTEN_FLESH", "mobs", 0.8D, 0.08D);
        item(items, "bone", "BONE", "mobs", 1.0D, 0.1D);
        item(items, "bone_meal", "BONE_MEAL", "mobs", 0.35D, 0.03D);
        item(items, "string", "STRING", "mobs", 1.5D, 0.15D);
        item(items, "spider_eye", "SPIDER_EYE", "mobs", 2.0D, 0.2D);
        item(items, "fermented_spider_eye", "FERMENTED_SPIDER_EYE", "mobs", 4.0D, 0.4D);
        item(items, "gunpowder", "GUNPOWDER", "mobs", 3.0D, 0.3D);
        item(items, "slime_ball", "SLIME_BALL", "mobs", 2.5D, 0.25D);
        item(items, "ender_pearl", "ENDER_PEARL", "mobs", 12.0D, 1.2D);
        item(items, "blaze_rod", "BLAZE_ROD", "mobs", 8.0D, 0.8D);
        item(items, "ghast_tear", "GHAST_TEAR", "mobs", 15.0D, 1.5D);
        item(items, "phantom_membrane", "PHANTOM_MEMBRANE", "mobs", 6.0D, 0.6D);
        item(items, "leather", "LEATHER", "mobs", 2.0D, 0.2D);
        item(items, "rabbit_hide", "RABBIT_HIDE", "mobs", 1.5D, 0.15D);
        item(items, "rabbit_foot", "RABBIT_FOOT", "mobs", 5.0D, 0.5D);
        item(items, "feather", "FEATHER", "mobs", 0.8D, 0.08D);
        item(items, "egg", "EGG", "mobs", 0.5D, 0.05D);
        item(items, "white_wool", "WHITE_WOOL", "mobs", 1.5D, 0.15D);
        item(items, "black_wool", "BLACK_WOOL", "mobs", 1.5D, 0.15D);
        item(items, "gray_wool", "GRAY_WOOL", "mobs", 1.5D, 0.15D);
        item(items, "red_wool", "RED_WOOL", "mobs", 1.5D, 0.15D);
        item(items, "blue_wool", "BLUE_WOOL", "mobs", 1.5D, 0.15D);
        item(items, "beef", "BEEF", "mobs", 2.0D, 0.2D);
        item(items, "cooked_beef", "COOKED_BEEF", "mobs", 3.5D, 0.35D);
        item(items, "porkchop", "PORKCHOP", "mobs", 1.8D, 0.18D);
        item(items, "cooked_porkchop", "COOKED_PORKCHOP", "mobs", 3.2D, 0.32D);
        item(items, "mutton", "MUTTON", "mobs", 1.7D, 0.17D);
        item(items, "cooked_mutton", "COOKED_MUTTON", "mobs", 3.0D, 0.3D);
        item(items, "chicken", "CHICKEN", "mobs", 1.5D, 0.15D);
        item(items, "cooked_chicken", "COOKED_CHICKEN", "mobs", 2.8D, 0.28D);
        item(items, "rabbit", "RABBIT", "mobs", 1.6D, 0.16D);
        item(items, "cooked_rabbit", "COOKED_RABBIT", "mobs", 2.9D, 0.29D);
        item(items, "cod", "COD", "mobs", 1.2D, 0.12D);
        item(items, "cooked_cod", "COOKED_COD", "mobs", 2.2D, 0.22D);
        item(items, "salmon", "SALMON", "mobs", 1.4D, 0.14D);
        item(items, "cooked_salmon", "COOKED_SALMON", "mobs", 2.5D, 0.25D);
        item(items, "tropical_fish", "TROPICAL_FISH", "mobs", 2.0D, 0.2D);
        item(items, "pufferfish", "PUFFERFISH", "mobs", 3.0D, 0.3D);
        item(items, "ink_sac", "INK_SAC", "mobs", 1.8D, 0.18D);
        item(items, "glow_ink_sac", "GLOW_INK_SAC", "mobs", 4.0D, 0.4D);
        item(items, "honeycomb", "HONEYCOMB", "mobs", 3.5D, 0.35D);
        item(items, "honey_bottle", "HONEY_BOTTLE", "mobs", 4.5D, 0.45D);
        item(items, "turtle_scute", "TURTLE_SCUTE", "mobs", 8.0D, 0.8D);
        item(items, "armadillo_scute", "ARMADILLO_SCUTE", "mobs", 7.0D, 0.7D);
        item(items, "sniffer_egg", "SNIFFER_EGG", "mobs", 25.0D, 2.5D);
        item(items, "frogspawn", "FROGSPAWN", "mobs", 2.0D, 0.2D);
        item(items, "arrow_drop", "ARROW", "mobs", 0.6D, 0.06D);
        item(items, "bow_drop", "BOW", "mobs", 4.0D, 0.4D);
        item(items, "crossbow_drop", "CROSSBOW", "mobs", 8.0D, 0.8D);
        item(items, "trident_drop", "TRIDENT", "mobs", 35.0D, 3.5D);
        item(items, "totem_of_undying", "TOTEM_OF_UNDYING", "mobs", 250.0D, 25.0D);
        item(items, "wither_skeleton_skull", "WITHER_SKELETON_SKULL", "mobs", 80.0D, 8.0D);
        item(items, "skeleton_skull", "SKELETON_SKULL", "mobs", 40.0D, 4.0D);
        item(items, "zombie_head", "ZOMBIE_HEAD", "mobs", 35.0D, 3.5D);
        item(items, "creeper_head", "CREEPER_HEAD", "mobs", 45.0D, 4.5D);
        item(items, "dragon_breath", "DRAGON_BREATH", "mobs", 20.0D, 2.0D);
        item(items, "dragon_head", "DRAGON_HEAD", "mobs", 120.0D, 12.0D);
        item(items, "breeze_rod", "BREEZE_ROD", "mobs", 18.0D, 1.8D);
        item(items, "wind_charge", "WIND_CHARGE", "mobs", 5.0D, 0.5D);
        item(items, "mace_drop", "MACE", "mobs", 60.0D, 6.0D);
    }

    private static void addMisc(Map<String, SoulBuyerSettings.SellableItemSettings> items) {
        item(items, "paper", "PAPER", "misc", 0.8D, 0.08D);
        item(items, "book", "BOOK", "misc", 2.5D, 0.25D);
        item(items, "writable_book", "WRITABLE_BOOK", "misc", 3.5D, 0.35D);
        item(items, "name_tag", "NAME_TAG", "misc", 15.0D, 1.5D);
        item(items, "saddle", "SADDLE", "misc", 20.0D, 2.0D);
        item(items, "lead", "LEAD", "misc", 4.0D, 0.4D);
        item(items, "flint", "FLINT", "misc", 0.6D, 0.06D);
        item(items, "stick", "STICK", "misc", 0.25D, 0.02D);
        item(items, "bowl", "BOWL", "misc", 0.4D, 0.04D);
        item(items, "bucket", "BUCKET", "misc", 5.0D, 0.5D);
        item(items, "water_bucket", "WATER_BUCKET", "misc", 6.0D, 0.6D);
        item(items, "lava_bucket", "LAVA_BUCKET", "misc", 12.0D, 1.2D);
        item(items, "powder_snow_bucket", "POWDER_SNOW_BUCKET", "misc", 4.0D, 0.4D);
        item(items, "glass_bottle", "GLASS_BOTTLE", "misc", 0.7D, 0.07D);
        item(items, "sugar", "SUGAR", "misc", 0.9D, 0.09D);
        item(items, "cookie", "COOKIE", "misc", 1.2D, 0.12D);
        item(items, "bread", "BREAD", "misc", 2.0D, 0.2D);
        item(items, "cake", "CAKE", "misc", 8.0D, 0.8D);
        item(items, "pumpkin_pie", "PUMPKIN_PIE", "misc", 3.5D, 0.35D);
        item(items, "rabbit_stew", "RABBIT_STEW", "misc", 5.0D, 0.5D);
        item(items, "beetroot_soup", "BEETROOT_SOUP", "misc", 2.5D, 0.25D);
        item(items, "mushroom_stew", "MUSHROOM_STEW", "misc", 2.5D, 0.25D);
        item(items, "suspicious_stew", "SUSPICIOUS_STEW", "misc", 4.0D, 0.4D);
        item(items, "torch", "TORCH", "misc", 0.35D, 0.03D);
        item(items, "lantern", "LANTERN", "misc", 2.0D, 0.2D);
        item(items, "soul_lantern", "SOUL_LANTERN", "misc", 2.5D, 0.25D);
        item(items, "campfire", "CAMPFIRE", "misc", 2.5D, 0.25D);
        item(items, "soul_campfire", "SOUL_CAMPFIRE", "misc", 3.0D, 0.3D);
        item(items, "fishing_rod", "FISHING_ROD", "misc", 3.5D, 0.35D);
        item(items, "shears", "SHEARS", "misc", 4.0D, 0.4D);
        item(items, "flint_and_steel", "FLINT_AND_STEEL", "misc", 3.0D, 0.3D);
        item(items, "compass", "COMPASS", "misc", 6.0D, 0.6D);
        item(items, "recovery_compass", "RECOVERY_COMPASS", "misc", 25.0D, 2.5D);
        item(items, "clock", "CLOCK", "misc", 8.0D, 0.8D);
        item(items, "spyglass", "SPYGLASS", "misc", 10.0D, 1.0D);
        item(items, "brush", "BRUSH", "misc", 4.5D, 0.45D);
        item(items, "map", "MAP", "misc", 5.0D, 0.5D);
        item(items, "filled_map", "FILLED_MAP", "misc", 8.0D, 0.8D);
        item(items, "firework_rocket", "FIREWORK_ROCKET", "misc", 2.5D, 0.25D);
        item(items, "firework_star", "FIREWORK_STAR", "misc", 1.5D, 0.15D);
        item(items, "tnt", "TNT", "misc", 12.0D, 1.2D);
        item(items, "end_crystal", "END_CRYSTAL", "misc", 35.0D, 3.5D);
        item(items, "ender_chest_item", "ENDER_CHEST", "misc", 45.0D, 4.5D);
        item(items, "shulker_box", "SHULKER_BOX", "misc", 30.0D, 3.0D);
        item(items, "white_shulker_box", "WHITE_SHULKER_BOX", "misc", 32.0D, 3.2D);
        item(items, "bundle", "BUNDLE", "misc", 5.0D, 0.5D);
        item(items, "music_disc_13", "MUSIC_DISC_13", "misc", 25.0D, 2.5D);
        item(items, "music_disc_cat", "MUSIC_DISC_CAT", "misc", 25.0D, 2.5D);
        item(items, "music_disc_blocks", "MUSIC_DISC_BLOCKS", "misc", 25.0D, 2.5D);
        item(items, "music_disc_chirp", "MUSIC_DISC_CHIRP", "misc", 25.0D, 2.5D);
        item(items, "music_disc_far", "MUSIC_DISC_FAR", "misc", 25.0D, 2.5D);
        item(items, "music_disc_mall", "MUSIC_DISC_MALL", "misc", 25.0D, 2.5D);
        item(items, "music_disc_mellohi", "MUSIC_DISC_MELLOHI", "misc", 25.0D, 2.5D);
        item(items, "music_disc_stal", "MUSIC_DISC_STAL", "misc", 25.0D, 2.5D);
        item(items, "music_disc_strad", "MUSIC_DISC_STRAD", "misc", 25.0D, 2.5D);
        item(items, "music_disc_ward", "MUSIC_DISC_WARD", "misc", 25.0D, 2.5D);
        item(items, "music_disc_11", "MUSIC_DISC_11", "misc", 25.0D, 2.5D);
        item(items, "music_disc_wait", "MUSIC_DISC_WAIT", "misc", 25.0D, 2.5D);
        item(items, "music_disc_pigstep", "MUSIC_DISC_PIGSTEP", "misc", 35.0D, 3.5D);
        item(items, "music_disc_otherside", "MUSIC_DISC_OTHERSIDE", "misc", 35.0D, 3.5D);
        item(items, "music_disc_5", "MUSIC_DISC_5", "misc", 40.0D, 4.0D);
        item(items, "music_disc_relic", "MUSIC_DISC_RELIC", "misc", 40.0D, 4.0D);
        item(items, "goat_horn", "GOAT_HORN", "misc", 12.0D, 1.2D);
        item(items, "ominous_bottle", "OMINOUS_BOTTLE", "misc", 30.0D, 3.0D);
        item(items, "trial_key", "TRIAL_KEY", "misc", 18.0D, 1.8D);
        item(items, "ominous_trial_key", "OMINOUS_TRIAL_KEY", "misc", 28.0D, 2.8D);
        item(items, "heavy_core", "HEAVY_CORE", "misc", 45.0D, 4.5D);
        item(items, "bolt_armor_trim", "BOLT_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 22.0D, 2.2D);
        item(items, "flow_armor_trim", "FLOW_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 22.0D, 2.2D);
        item(items, "coast_armor_trim", "COAST_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 18.0D, 1.8D);
        item(items, "dune_armor_trim", "DUNE_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 18.0D, 1.8D);
        item(items, "eye_armor_trim", "EYE_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 20.0D, 2.0D);
        item(items, "rib_armor_trim", "RIB_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 20.0D, 2.0D);
        item(items, "sentry_armor_trim", "SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 18.0D, 1.8D);
        item(items, "vex_armor_trim", "VEX_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 20.0D, 2.0D);
        item(items, "wild_armor_trim", "WILD_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 18.0D, 1.8D);
        item(items, "wayfinder_armor_trim", "WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 22.0D, 2.2D);
        item(items, "raiser_armor_trim", "RAISER_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 22.0D, 2.2D);
        item(items, "shaper_armor_trim", "SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 22.0D, 2.2D);
        item(items, "host_armor_trim", "HOST_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 22.0D, 2.2D);
        item(items, "silence_armor_trim", "SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 35.0D, 3.5D);
        item(items, "snout_armor_trim", "SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 25.0D, 2.5D);
        item(items, "spire_armor_trim", "SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 25.0D, 2.5D);
        item(items, "tide_armor_trim", "TIDE_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 25.0D, 2.5D);
        item(items, "ward_armor_trim", "WARD_ARMOR_TRIM_SMITHING_TEMPLATE", "misc", 30.0D, 3.0D);
        item(items, "netherite_upgrade", "NETHERITE_UPGRADE_SMITHING_TEMPLATE", "misc", 55.0D, 5.5D);
        item(items, "diamond_horse_armor", "DIAMOND_HORSE_ARMOR", "misc", 35.0D, 3.5D);
        item(items, "golden_horse_armor", "GOLDEN_HORSE_ARMOR", "misc", 18.0D, 1.8D);
        item(items, "iron_horse_armor", "IRON_HORSE_ARMOR", "misc", 10.0D, 1.0D);
        item(items, "leather_horse_armor", "LEATHER_HORSE_ARMOR", "misc", 5.0D, 0.5D);
        item(items, "wolf_armor", "WOLF_ARMOR", "misc", 15.0D, 1.5D);
        item(items, "carrot_on_a_stick", "CARROT_ON_A_STICK", "misc", 3.0D, 0.3D);
        item(items, "warped_fungus_on_a_stick", "WARPED_FUNGUS_ON_A_STICK", "misc", 4.0D, 0.4D);
        item(items, "elytra", "ELYTRA", "misc", 200.0D, 20.0D);
        item(items, "beacon", "BEACON", "misc", 180.0D, 18.0D);
        item(items, "conduit", "CONDUIT", "misc", 90.0D, 9.0D);
        item(items, "scaffolding", "SCAFFOLDING", "misc", 0.5D, 0.05D);
        item(items, "ladder", "LADDER", "misc", 0.6D, 0.06D);
        item(items, "chain", "CHAIN", "misc", 2.0D, 0.2D);
        item(items, "iron_bars", "IRON_BARS", "misc", 1.5D, 0.15D);
        item(items, "painting", "PAINTING", "misc", 3.0D, 0.3D);
        item(items, "item_frame", "ITEM_FRAME", "misc", 2.5D, 0.25D);
        item(items, "glow_item_frame", "GLOW_ITEM_FRAME", "misc", 4.0D, 0.4D);
        item(items, "armor_stand", "ARMOR_STAND", "misc", 3.5D, 0.35D);
    }

    private static void item(
            Map<String, SoulBuyerSettings.SellableItemSettings> items,
            String id,
            String material,
            String category,
            double price,
            double points
    ) {
        SoulBuyerSettings.SellableItemSettings settings = new SoulBuyerSettings.SellableItemSettings();
        settings.material = material;
        settings.category = category;
        settings.basePrice = price;
        settings.basePoints = points;
        settings.customModelData = -1;
        items.put(id, settings);
    }
}
