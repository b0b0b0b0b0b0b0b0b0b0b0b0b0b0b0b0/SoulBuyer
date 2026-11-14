package bm.b0b0b0.soulBuyer.message;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MessageDefaults {

    private MessageDefaults() {
    }

    public static Map<String, Object> ru() {
        Map<String, Object> messages = new LinkedHashMap<>();
        messages.put("prefix", "&#FFD700» ");

        messages.put("categories.ores", "Руды");
        messages.put("categories.mobs", "Мобы");
        messages.put("categories.plants", "Растения");
        messages.put("categories.blocks", "Блоки");
        messages.put("categories.misc", "Прочее");

        messages.put("gui.nav.close", "&#FF5555Закрыть");
        messages.put("gui.nav.back", "&#FFAA00Назад");

        messages.put("gui.buyer.title", "&#FFD700Скупщик");
        messages.put("gui.buyer.border", " ");
        messages.put("gui.buyer.preview", "&#55FF55Предпросмотр");
        messages.put("gui.buyer.preview-lore", List.of(
                "&#AAAAAAЗаработок: &#FFFFFF{money} монет",
                "&#AAAAAAОчки: &#FFFFFF{points}",
                "&#AAAAAAПредметов: &#FFFFFF{count}",
                "&#AAAAAAМножитель: &#FFFFFF×{multiplier}",
                "&#AAAAAAРынок: &#FFFFFF×{market}"
        ));
        messages.put("gui.buyer.sell-all", "&#55FF55Продать всё из инвентаря");
        messages.put("gui.buyer.sell-all-lore", List.of(
                "&#AAAAAAБыстро добавить скупаемые",
                "&#AAAAAAпредметы из вашего инвентаря"
        ));
        messages.put("gui.buyer.confirm", "&#55FF55Подтвердить продажу");
        messages.put("gui.buyer.confirm-lore", List.of(
                "&#AAAAAAИтого: &#FFFFFF{money} монет",
                "&#AAAAAAОчки: &#FFFFFF{points}"
        ));
        messages.put("gui.buyer.close", "&#FF5555Закрыть");
        messages.put("gui.buyer.close-lore", List.of("&#AAAAAAВернуть предметы"));

        messages.put("boosters.insufficient-points", "&#FF5555Не хватает &#E9D5FF{need} &#FF5555очков SoulBuyer на &#E9D5FF{offer}&#FF5555.\n&#9CA3AFНужно &#FFFFFF{price} &#9CA3AF· у вас &#FFFFFF{balance}");
        messages.put("boosters.insufficient-vault", "&#FF5555Не хватает &#FCD34D{need} &#FF5555монет на &#E9D5FF{offer}&#FF5555.\n&#9CA3AFНужно &#FFFFFF{price} &#9CA3AF· у вас &#FFFFFF{balance}");
        messages.put("boosters.insufficient-playerpoints", "&#FF5555Не хватает &#FCD34D{need} &#FF5555PlayerPoints на &#E9D5FF{offer}&#FF5555.\n&#9CA3AFНужно &#FFFFFF{price} &#9CA3AF· у вас &#FFFFFF{balance}");

        messages.put("sell.not-sellable", "&#FF5555{item} не скупается.");
        messages.put("sell.in-progress", "&#FFAA00Продажа уже выполняется...");
        messages.put("sell.empty", "&#FF5555Нет предметов для продажи.");
        messages.put("sell.success", "&#55FF55Продано &#FFFFFF{count} &#55FF55поз. на &#FFFFFF{money} &#55FF55монет (+&#FFFFFF{points} &#55FF55очков)");
        messages.put("sell.success-no-points", "&#55FF55Продано &#FFFFFF{count} &#55FF55поз. на &#FFFFFF{money} &#55FF55монет");
        messages.put("sell.partial", "&#FFAA00Часть предметов не скупается и возвращена.");
        messages.put("sell.failed", "&#FF5555Ошибка продажи. Предметы возвращены.");
        messages.put("sell.returned", "&#FFAA00Непроданные предметы возвращены.");
        messages.put("sell.no-economy", "&#FF5555Экономика недоступна. Обратитесь к администрации.");

        messages.put("command.no-permission", "&#FF5555Недостаточно прав.");
        messages.put("command.player-only", "&#FF5555Только для игроков.");
        messages.put("command.reload-success", "&#55FF55SoulBuyer перезагружен.");
        messages.put("command.reload-failed", "&#FF5555Ошибка перезагрузки SoulBuyer.");
        messages.put("command.globalbooster-disabled", "&#FF5555Глобальные бустеры отключены.");
        messages.put("command.globalbooster-empty", "&#AAAAAAАктивных глобальных бустеров нет.");
        messages.put("command.globalbooster-list-header", "&#FCD34DГлобальные бустеры:");
        messages.put("command.globalbooster-list-line", "&#E9D5FF{type} &#AAAAAAэффект &#FFFFFF{effect} &#AAAAAAосталось &#FFFFFF{remaining}");
        messages.put("command.globalbooster-activated", "&#86EFACГлобальный бустер &#E9D5FF{offer} &#86EFACвключён.");
        messages.put("command.globalbooster-unknown-offer", "&#FF5555Оффер &#E9D5FF{offer} &#FF5555не найден.");
        messages.put("command.globalbooster-cleared-all", "&#86EFACВсе глобальные бустеры сняты.");
        messages.put("command.globalbooster-cleared-type", "&#86EFACГлобальный бустер &#E9D5FF{type} &#86EFACснят.");
        messages.put("command.globalbooster-clear-miss", "&#AAAAAAГлобальный бустер &#E9D5FF{type} &#AAAAAAне активен.");
        messages.put("error.database", "&#FF5555SoulBuyer ещё загружается, подождите...");

        return messages;
    }

    public static Map<String, Object> en() {
        Map<String, Object> messages = new LinkedHashMap<>();
        messages.put("prefix", "&#FFD700» ");

        messages.put("categories.ores", "Ores");
        messages.put("categories.mobs", "Mobs");
        messages.put("categories.plants", "Plants");
        messages.put("categories.blocks", "Blocks");
        messages.put("categories.misc", "Misc");

        messages.put("gui.nav.close", "&#FF5555Close");
        messages.put("gui.nav.back", "&#FFAA00Back");

        messages.put("gui.buyer.title", "&#FFD700Buyer");
        messages.put("gui.buyer.border", " ");
        messages.put("gui.buyer.preview", "&#55FF55Preview");
        messages.put("gui.buyer.preview-lore", List.of(
                "&#AAAAAAEarnings: &#FFFFFF{money} coins",
                "&#AAAAAAPoints: &#FFFFFF{points}",
                "&#AAAAAAItems: &#FFFFFF{count}",
                "&#AAAAAAMultiplier: &#FFFFFF×{multiplier}",
                "&#AAAAAAMarket: &#FFFFFF×{market}"
        ));
        messages.put("gui.buyer.sell-all", "&#55FF55Sell all from inventory");
        messages.put("gui.buyer.sell-all-lore", List.of(
                "&#AAAAAAdd sellable items",
                "&#AAAAAAfrom your inventory"
        ));
        messages.put("gui.buyer.confirm", "&#55FF55Confirm sale");
        messages.put("gui.buyer.confirm-lore", List.of(
                "&#AAAAAATotal: &#FFFFFF{money} coins",
                "&#AAAAAAPoints: &#FFFFFF{points}"
        ));
        messages.put("gui.buyer.close", "&#FF5555Close");
        messages.put("gui.buyer.close-lore", List.of("&#AAAAAAReturn items"));

        messages.put("boosters.insufficient-points", "&#FF5555Need &#E9D5FF{need} &#FF5555more SoulBuyer points for &#E9D5FF{offer}&#FF5555.\n&#9CA3AFCost &#FFFFFF{price} &#9CA3AF· you have &#FFFFFF{balance}");
        messages.put("boosters.insufficient-vault", "&#FF5555Need &#FCD34D{need} &#FF5555more coins for &#E9D5FF{offer}&#FF5555.\n&#9CA3AFCost &#FFFFFF{price} &#9CA3AF· you have &#FFFFFF{balance}");
        messages.put("boosters.insufficient-playerpoints", "&#FF5555Need &#FCD34D{need} &#FF5555more PlayerPoints for &#E9D5FF{offer}&#FF5555.\n&#9CA3AFCost &#FFFFFF{price} &#9CA3AF· you have &#FFFFFF{balance}");

        messages.put("sell.not-sellable", "&#FF5555{item} is not accepted.");
        messages.put("sell.in-progress", "&#FFAA00Sale already in progress...");
        messages.put("sell.empty", "&#FF5555No items to sell.");
        messages.put("sell.success", "&#55FF55Sold &#FFFFFF{count} &#55FF55stacks for &#FFFFFF{money} &#55FF55coins (+&#FFFFFF{points} &#55FF55points)");
        messages.put("sell.success-no-points", "&#55FF55Sold &#FFFFFF{count} &#55FF55stacks for &#FFFFFF{money} &#55FF55coins");
        messages.put("sell.partial", "&#FFAA00Some items were returned — not sellable.");
        messages.put("sell.failed", "&#FF5555Sale failed. Items returned.");
        messages.put("sell.returned", "&#FFAA00Unsold items returned.");
        messages.put("sell.no-economy", "&#FF5555Economy unavailable. Contact staff.");

        messages.put("command.no-permission", "&#FF5555Insufficient permissions.");
        messages.put("command.player-only", "&#FF5555Players only.");
        messages.put("command.reload-success", "&#55FF55SoulBuyer reloaded.");
        messages.put("command.reload-failed", "&#FF5555SoulBuyer reload failed.");
        messages.put("command.globalbooster-disabled", "&#FF5555Global boosters are disabled.");
        messages.put("command.globalbooster-empty", "&#AAAAAANo active global boosters.");
        messages.put("command.globalbooster-list-header", "&#FCD34DGlobal boosters:");
        messages.put("command.globalbooster-list-line", "&#E9D5FF{type} &#AAAAAAeffect &#FFFFFF{effect} &#AAAAAAleft &#FFFFFF{remaining}");
        messages.put("command.globalbooster-activated", "&#86EFACGlobal booster &#E9D5FF{offer} &#86EFACenabled.");
        messages.put("command.globalbooster-unknown-offer", "&#FF5555Offer &#E9D5FF{offer} &#FF5555not found.");
        messages.put("command.globalbooster-cleared-all", "&#86EFACAll global boosters cleared.");
        messages.put("command.globalbooster-cleared-type", "&#86EFACGlobal booster &#E9D5FF{type} &#86EFACcleared.");
        messages.put("command.globalbooster-clear-miss", "&#AAAAAAGlobal booster &#E9D5FF{type} &#AAAAAAis not active.");
        messages.put("error.database", "&#FF5555SoulBuyer is still loading, please wait...");

        return messages;
    }
}
