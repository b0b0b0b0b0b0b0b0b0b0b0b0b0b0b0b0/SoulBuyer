# SoulBuyer

> [Russian version](../../README.md)

**Advanced resource buyer for Paper 1.21+ and Folia:** live GUI, dynamic market, catalog rotation, **booster shop**, **autosell with three modes**, **PlayerPoints integration** (separate donate buyer or full payout in points), player progression, and multiple storage backends — from YAML on a single server to MySQL + Redis across a network.

---

## Overview

**SoulBuyer** is not “drop an item in a slot and wait.” The player opens a menu and sees a **grid of buyable resources** with current prices, points, and market coefficient. Left-click on an item — **sell all of that resource from inventory**; right-click — **pick quantity** in a separate submenu. Category filters, sorting, pagination, and a “sell all” button are included.

The more one resource is sold on the server, the lower its **market coefficient** (with a configurable floor and gradual recovery). Sales grant **progression points** and **category XP** (ores, mobs, plants, blocks, misc); points can be spent in the **booster shop** for temporary buffs. Payout goes through **Vault** or **PlayerPoints** (see [PlayerPoints and donate buyer](#playerpoints-and-donate-buyer)).

**Autosell** is a separate donate perk: the player chooses *when* to turn in resources — on pickup, when opening the buyer menu, or **chest contents when opening a container**. Categories, notifications, and minimum price are configurable.

The catalog does not stagnate: on a timer the set of buyable items changes, market coefficients reset, and players get a chat notification. At the bottom of the menu — a **live stats panel**: time until rotation, sold today, how many resources are currently in rotation.

## Features

Why put it on your server:

- **Out of the box** — ~380 items, GUI, rotation, market, and lang (ru/en/fi) from first start; no manual config wrangling
- **Everything configurable** — prices, catalog, GUI slots, permissions, market limits, rotation, autosell, economy; texts in `lang/`, numbers in `config.yml` / `items.yml`
- **GUI over commands** — selling, filters, sorting, quantity, autosell; commands are aliases only
- **Live market** — price drops from mass turn-ins, decay returns to normal; player sees the coefficient in lore
- **Catalog rotation** — timer picks a new set from the pool; chat notification; open menus refresh in place
- **Donate buyer** — Vault + PlayerPoints: two menus (`/buyer` and `/donbuyer`) or one buyer paid entirely in points; two flags in config
- **Autosell** — donate perk: pickup / menu / chest; categories, min price, notifications; in dual mode — choose payout (coins or points)
- **Boosters** — temporary buff shop in the buyer menu; paid with progression points, Vault, or PlayerPoints (configurable)
- **Personal sell limits** — daily cap per item-id (anti-dumping); LuckPerms nodes and limit booster
- **Progression** — points, category XP, VIP multipliers via LuckPerms; incentive to farm, not just loot
- **Live stats** — timer until rotation, “sold today”, assortment size right in the menu (+ PlaceholderAPI)
- **Category animation** — filter icons cycle real items from the current rotation
- **Optimized** — all DB I/O async; main thread never blocked; HikariCP; Redis only when the network needs it
- **Storage choice** — `flat` / `sqlite` / `mysql` + Redis for multiple Paper servers sharing one market
- **Folia-ready** — region-aware schedulers on hot paths; no legacy `BukkitScheduler` for sync logic
- **Safe sales** — secure storage per transaction; on error or disconnect items are returned
- **API for other plugins** — `SoulBuyerApi`: menus, quotes, selling from your own GUI, item categories, progress
- **Automatic player language detection** (CLIENT/SERVER locale in config) — v1.4.0
- **Optional bStats metrics** (opt-out in config) — v1.4.0

---

## Buyer GUI

| Zone | What it does |
|------|--------------|
| **Top row** | Category filters + “Your progress” block (points, multiplier) |
| **21-slot grid** | Buyable item icons: price, points, market, amount in inventory. **LMB** — sell all, **RMB** — quantity submenu |
| **← → arrows** | Pagination (4th row, on the sides of the grid) |
| **Bottom row** | Sort (single toggle button), “sell all”, **boosters**, **autosell**, **“Until rotation” panel** |
| **Categories** | “All” — Vault icon (storage); others — **live animation**: icon changes and shows a real item from that category’s current assortment |

**Sorting** (comparator, click — next mode): highest price → lowest → most points → least points.

**Boosters** (nether star in bottom row): **LMB** — temporary buff shop. Available from **regular** and **donate** menus. Details in [Boosters](#boosters).

**Autosell** (emerald in bottom row): **LMB** — settings screen, **RMB** — on/off. Details in [Autosell](#autosell).

**Texts and colors** — in `lang/ru.yml`, `lang/en.yml`, `lang/fi.yml`. Hex colors, gradient title, lore for categories and items — all without rebuilding the JAR.

### Menu icons (smithing templates, armor trims)

In `gui/buyer.yml` → `hideVanillaItemTooltip: true` (default): tooltip shows only SoulBuyer lore — no vanilla “Smithing Template”, “Applies to”, attributes, etc.

| Item type | How it appears in the slot |
|-----------|----------------------------|
| Regular resources | Item icon + your lore |
| **Armor trims** (bolt, flow, coast…) | Iron chestplate **with trim pattern** |
| Netherite Upgrade | Netherite ingot |

**How to verify after an update**

1. Build the JAR, replace on the server, restart.
2. Open `/buyer` → “Misc” category (or wherever trims are in rotation).
3. Slot should show **trim on chestplate**, not a plain gray chestplate without pattern.
4. Hover — only SoulBuyer name and price, no vanilla clutter.
5. Same in autosell: emerald → **RMB** on category → trims with pattern.

**If the icon is still wrong**

- `plugins/SoulBuyer/gui/buyer.yml` must have `hideVanillaItemTooltip: true`.
- Client **1.21+**; turn off **F3+H** (advanced tooltips confuse things).
- Admin: `config.yml` → `debug-tooltip: true` → `/soulbuyer admin reload` → `/soulbuyer admin debug-tooltip` → `/buyer`. Keep **false** on production (default).

---

## Autosell

Separate donate mechanic (`soulbuyer.autosell`, OP by default). The player configures behavior — settings are saved per-player (YAML or MySQL).

### When to sell

One of three modes (toggle button in settings menu):

| Mode | Icon | When it triggers |
|------|------|------------------|
| **On pickup** | Hopper | Item enters inventory from the ground or fishing |
| **On menu open** | Emerald | Buyer GUI opens — sells everything matching **from player inventory** |
| **On chest open** | Chest | Container opens — sells everything matching **from chest slots**. Player inventory untouched |

Works with chest, barrel, shulker, ender chest, hopper, dispenser, dropper, minecart with chest. Respects **enabled categories**, **item selection**, **min price**, and **current rotation** (only item-ids currently in the buyer are sold).

### Other settings

| Option | Meaning |
|--------|---------|
| **On/off** | Master switch (center of menu or RMB on emerald in buyer) |
| **Categories** | Ores, mobs, plants, blocks, misc — **LMB** toggles whole category |
| **Items in category** | **RMB** on category icon → selection screen: which item-ids from that category to autosell |
| **Notifications** | Action bar / chat / off |
| **Min price** | Do not sell items below threshold (after market and multipliers) |
| **Payout target** | Dual buyer only: regular buyer (Vault) or donate (PlayerPoints). Click ingot in center of row 4 |

### Per-item selection in a category

After enabling a category, **all items in that category from the current rotation** autosell by default. To keep only bones from “Mobs” or only diamonds from “Ores”:

1. Autosell menu → **RMB** on the category.
2. Grid opens with **only active** item-ids (as in `/buyer` now).
3. **Click** item — toggle autosell for it (highlight = enabled).
4. Cannot disable the **last** enabled item in an active category.

If an item **drops out of rotation** — it is not shown in selection and **is not autosold**, even if it was enabled before. After it returns to rotation, the player’s setting is preserved (unless the id is in the disabled list).

**Example:** “Mobs” enabled → RMB → disable everything except `bone` → on pickup/chest/menu only mob bones sell (and only while bones are in rotation).

### Player settings storage

Per-player in `data/autosell/{uuid}.yml` (flat) or column `settings` in table `soulbuyer_autosell` (MySQL). Text format:

```
enabled=true
trigger=pickup
notify=actionbar
min-unit-price=0
payout=vault
categories=ores,mobs,plants,blocks,misc
disabled-items=bone,rotten_flesh
```

| Key | Meaning |
|-----|---------|
| `categories` | Enabled categories (comma-separated) |
| `disabled-items` | Item-ids **excluded** from autosell within enabled categories. Empty = all active category items |

Other keys — trigger mode, notifications, min price, payout.

Book slot — short in-game help. If nothing to sell in inventory, no extra chat spam.

In **both** buyer menus (regular and `/donbuyer`) the autosell button shows current payout mode; configuration is in the same autosell screen whether entered from either menu.

### Why servers use it

- **Donate perk** without pay-to-win: same prices and market, less grind.
- **Base sorting**: “chest” mode — open storage, junk inside sells instantly, useful stuff stays in the chest.
- **AFK farm**: “pickup” mode — picked up resource is already sold.
- **Casual players**: “menu” mode — opened `/buyer`, excess sold automatically.

In `config.yml` → `autosell` section: enable/disable feature server-wide, defaults for new players, min price steps, delay after pickup.

---

## Boosters

Shop for **temporary buffs** — opened from regular (`/buyer`) and donate (`/donbuyer`) menus: **“Boosters”** button (nether star) → separate screen with three offers.

Purchase debits currency from `config.yml` → `boosters.currency`:

| Value | Debit |
|-------|-------|
| `progression_points` | **SoulBuyer points** (default — earned from sales, previously had nowhere to spend) |
| `vault` | Coins via Vault |
| `playerpoints` | PlayerPoints |

### Three boosters (defaults, all in config)

| ID | Effect | Duration | Price (default) |
|----|--------|----------|-------------------|
| `multiplier` | **+0.5** to personal income multiplier | 1 h | 125 points |
| `money` | **×1.25** to coins/points from sale | 1 h | 175 points |
| `limit` | **×2** to personal daily limit per item-id | 30 min | 75 points |

Prices, duration, effect strength, materials and GUI slots — in `config.yml` → `boosters.offers` and `gui/boosters.yml`.

Each booster button lore shows: effect, price, duration, **whether active now** and time left. Re-buying **refreshes** the timer for that type.

### Sell limits (linked to `limit` booster)

Parallel **anti-dumping** mechanic — `sell-limits` section in `config.yml`:

- Each player has a **daily limit** per `item-id` (default 64/day).
- LuckPerms nodes `soulbuyer.limit.vip` / `soulbuyer.limit.premium` raise cap (128 / 256).
- Limit booster multiplies the final limit.
- `sell-limits.enabled: false` — limits fully disabled.

When limit exceeded, sale is **trimmed** to allowed amount; excess returns to inventory.

### Permissions and fresh install

| Node | Access |
|------|--------|
| `soulbuyer.boosters` | Booster shop (default: true) |

Java defaults: boosters **enabled**, currency — **progression points**, all three offers and GUI in place after first start. Active boosters saved per-player (`data/boosters/` or table `soulbuyer_boosters`).

---

## PlayerPoints and donate buyer

SoulBuyer is **not Vault-only**. With [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/) on the server, the plugin pays for resources in **donate points** — no hacks, separate commands, same market, rotation, and progression.

One catalog `items.yml`, one set of prices and coefficients — only **payout currency** changes (server coins or PlayerPoints).

### Three modes (`economy` section in `config.yml`)

| `player-points-enabled` | `donate-buyer-enabled` | What the player sees |
|---------------------------|------------------------|----------------------|
| `false` | — | Classic: `/buyer` → payout in **Vault** |
| `true` | `false` | **One** buyer: `/buyer` → everything in **PlayerPoints** (Vault not required) |
| `true` | `true` | **Two** buyers: regular for coins + **donate** for points |

```yaml
economy:
  player-points-enabled: false   # enable PlayerPoints
  donate-buyer-enabled: false    # true = second menu paid only in points
```

On **Folia without Vault**, install PlayerPoints and set `player-points-enabled: true`, or the plugin auto-switches if Vault is missing but PlayerPoints is present.

### Two buyers — why it matters

Donate servers often split economy: **farm → coins**, **donate content → points**. SoulBuyer covers both with one JAR:

| Menu | Command | Payout | Who |
|------|---------|--------|-----|
| **Regular** | `/buyer`, `/sell`, `/soulbuyer` | Vault (coins) | Everyone with `soulbuyer.use` |
| **Donate** | `/donbuyer`, `/dbuyer`, `/ppbuyer` | PlayerPoints | With `soulbuyer.donate` |

- Same assortment and **live market** — price drops from mass sales in both menus.
- **Progression points** and category XP accrue as usual (meta-progress, not payout currency).
- Separate **title and lore** on donate menu — player sees points payout immediately.
- **Autosell** follows player setting: in dual mode — Vault or PlayerPoints; PlayerPoints-only mode — always points.

### Fresh install

Java defaults: PlayerPoints **off**, Vault only — as before. Enable with two lines in `config.yml`, restart. `/donbuyer` and aliases are in `plugin.yml`; if donate mode is off, `/donbuyer` replies that the feature is unavailable.

### Dependencies

| Plugin | When needed |
|--------|-------------|
| **Vault** + economy | Default; required in dual mode and classic mode |
| **PlayerPoints** | `player-points-enabled: true` |

Both are `softdepend` in `plugin.yml`: plugin starts, waits for the needed economy, then opens GUI.

---

## Dynamic market

- Each resource has a **base price** and **market coefficient** (starts at 1.0).
- Mass sales **lower** the coefficient down to configured minimum.
- **Decay** gradually returns price to 1.0 if the resource stops being sold.
- In **mysql + network** mode, coefficients sync via Redis pub/sub.

Player sees market multiplier in item lore in the GUI.

---

## Catalog rotation

Full catalog (~380 vanilla resources in `items.yml`) is the **pool**. The buyer shows a **random subset** at once (default 48 items, minimum from each category).

| Parameter | Default | Meaning |
|-----------|---------|---------|
| `catalog-rotation.enabled` | `true` | Rotation on/off |
| `interval-seconds` | `3600` | Change interval (1 hour) |
| `active-item-count` | `48` | Items in menu |
| `reset-market-on-rotation` | `true` | Reset market coefficients on change |
| `notify.enabled` | `true` | Chat message on update |

Rotation state in `data/rotation.yml`. Open menus **refresh without closing**.

---

## Stats panel (bottom center)

Instead of “Page 1/3” — **clock with live timer** (updates every second):

- **Until rotation** — time left until next rotation
- **Sold today** — coins, points, stacks
- **In assortment** — how many resources are in rotation now

Lore placeholders in lang:

| Placeholder | Description |
|-------------|-------------|
| `{rotation_left}` | Timer until rotation or “disabled” / “soon” |
| `{sold_today_money}` | Coins today |
| `{sold_today_points}` | Points today |
| `{sold_today_stacks}` | Stacks sold |
| `{sold_alltime_money}` | Coins all time |
| `{sold_alltime_points}` | Points all time |
| `{sold_alltime_stacks}` | Stacks all time |
| `{active_items}` | Items in current assortment |

---

## Progression

- **Points** per sale (from item `base-points` + bonus from base price × `points-per-currency`).
- **Category XP** — grows from points earned; **money** bonus from **XP levels** (not raw number), capped by `max-category-bonus`.
- **Permission multipliers** — VIP, Premium, etc. (highest granted wins).
- **Dominant category bonus** — extra income from main category XP level (`category-xp-per-level`, `dominant-category-bonus-per-level`).
- Points can be **spent** in booster shop (if `boosters.currency: progression_points`).
- Anti-exploit: `max-unit-price`, `max-payout-per-sale` per transaction.

---

## Safety and reliability

- **Secure storage** for transaction duration — items not lost on lag or error.
- On quit / failure — **return** unsold items to inventory.
- Sales logged (`sales.log` or table `soulbuyer_sales`) — basis for “sold today” stats.
- All DB I/O **async**, main thread never blocked.

---

## Data storage

| Mode | When to use |
|------|-------------|
| `flat` | Single server, YAML + sales log (default) |
| `sqlite` | Single server, file `data/data.db` |
| `mysql` | Server network, shared DB + Redis for market |

Player progress, market coefficients, rotation, sales log — all survive restart.

---

## Commands

Main: `/soulbuyer`. Menu aliases (configurable): `/buyer`, `/sell`, `/sb`, `/rbuyer`, `/bs`.

**Donate buyer** (only when `economy.donate-buyer-enabled: true`): `/donbuyer`. Aliases: `/dbuyer`, `/ppbuyer`, `/donatesell`.

| Command | Action |
|---------|--------|
| `/soulbuyer` | Open buyer GUI (Vault or PlayerPoints — per `economy` mode) |
| `/buyer`, `/sell`, … | Same (aliases) |
| `/donbuyer`, `/dbuyer`, … | Donate menu — payout in **PlayerPoints** (dual mode) |
| `/soulbuyer admin reload` | Reload config, gui, lang |

> Aliases inherit subcommands: `/buyer admin reload` = `/soulbuyer admin reload`.

---

## Permissions

| Node | Access |
|------|--------|
| `soulbuyer.use` | GUI and selling (regular buyer) |
| `soulbuyer.donate` | Donate menu `/donbuyer` (PlayerPoints, dual mode) |
| `soulbuyer.admin` | `/soulbuyer admin reload` |
| `soulbuyer.autosell` | Autosell (donate, OP by default) |
| `soulbuyer.boosters` | Booster shop (default: true) |
| `soulbuyer.limit.vip` | Daily sell limit 128/item-id |
| `soulbuyer.limit.premium` | Daily sell limit 256/item-id |
| `soulbuyer.multiplier.vip` | Multiplier ×1.1 (default) |
| `soulbuyer.multiplier.premium` | Multiplier ×1.25 (default) |

With multiple multiplier nodes, the **highest** multiplier applies.

---

## PlaceholderAPI

`softdepend` — if PAPI is on the server, expansion **`soulbuyer`** registers.

| Placeholder | Description |
|-------------|-------------|
| `%soulbuyer_rotation_left%` | Timer until rotation (text) |
| `%soulbuyer_rotation_seconds%` | Seconds until rotation (number) |
| `%soulbuyer_sold_today_money%` | Coins sold today |
| `%soulbuyer_sold_today_points%` | Points today |
| `%soulbuyer_sold_today_stacks%` | Stacks sold today |
| `%soulbuyer_sold_alltime_money%` | Coins sold all time |
| `%soulbuyer_sold_alltime_points%` | Points all time |
| `%soulbuyer_sold_alltime_stacks%` | Stacks sold all time |
| `%soulbuyer_active_items%` | Items in current assortment |

PAPI placeholders can go in `lang/*.yml` strings — they resolve when rendering the GUI.

---

## Configuration

| File | Contents |
|------|----------|
| `config.yml` | Storage, market, rotation, progression, **boosters**, **sell limits**, permissions, category icon animation, **locale**, **check-for-updates**, **bstats** |
| `items.yml` | Catalog: id, material, category, base-price, base-points |
| `gui/general.yml`, `gui/buyer.yml`, `gui/quantity.yml`, `gui/autosell.yml`, `gui/boosters.yml` | Slots, button materials, action types |
| `lang/en.yml`, `lang/ru.yml`, `lang/fi.yml` | Bundled locales: titles, lore, messages, hex colors |
| `lang/*.yml` (on disk) | Custom locales — copy bundled file or add e.g. `lang/de.yml`; any `*.yml` in `lang/` loads on startup and reload |
| `data/rotation.yml` | Current rotation and next change time |
| `data/market.yml` | Market coefficients (flat mode) |
| `data/players/` | Player progress (flat mode) |
| `data/boosters/` | Active player boosters (flat mode) |
| `data/sell-limits/` | Today’s sales per item-id (flat mode) |

Defaults are in Java — after first start everything works **out of the box**: ~380 items, rotation, GUI, catalog, stats.

### Language (`config.yml` → `locale`)

| Key | Purpose |
|-----|---------|
| `locale-mode` | `CLIENT` (default) — each player’s Minecraft language; `SERVER` — one language for everyone |
| `server-locale` | Used only when `locale-mode: SERVER` (e.g. `ru`, `en`, `fi`, or custom code matching `lang/<code>.yml`) |
| `fallback-locale` | Missing keys and CLIENT fallback when no matching file (usually `en`) |

Bundled: `lang/en.yml`, `lang/ru.yml`, `lang/fi.yml`. Add custom files under `plugins/SoulBuyer/lang/`; reload with `/soulbuyer admin reload`.

### Other `config.yml` keys

| Key | Purpose |
|-----|---------|
| `check-for-updates` | Notify console when a new plugin version is available (default: `true`) |
| `bstats.enabled` | Anonymous metrics to [bStats](https://bstats.org) (default: `true`; set `false` to opt out) |

Key sections:

```yaml
storage-type: flat          # flat | sqlite | mysql
economy:                      # Vault / PlayerPoints / dual buyer
catalog-rotation:           # timed assortment change
category-icon-animation:    # live category icon rotation
market:                       # decay, min-coefficient, flush
progression:                  # multipliers, points, category XP, payout caps
boosters:                     # purchase currency, offers (price, effect, duration)
sell-limits:                  # daily limits, permission tiers
locale:                       # locale-mode, server-locale, fallback-locale
bstats:                       # enabled
check-for-updates: true
```

---

## Roadmap

- Extended admin commands

---

## Requirements

| Component | Required |
|-----------|----------|
| Paper 1.21+ (Folia supported) | yes |
| Java 21 | yes |
| Vault + economy | default; not needed if PlayerPoints only (`player-points-enabled: true`, `donate-buyer-enabled: false`) |
| PlayerPoints | optional; required when `player-points-enabled: true` |
| MySQL + Redis | only `storage-type: mysql` on a network |
| PlaceholderAPI | optional |

---

## Public API

Interface `SoulBuyerApi` registers in Bukkit `ServicesManager`. Another plugin connects via `depend` or `softdepend: [SoulBuyer]`.

### Getting the API

```java
import bm.b0b0b0.soulBuyer.api.SoulBuyerApi;
import bm.b0b0b0.soulBuyer.api.SoulBuyerApiProvider;
import bm.b0b0b0.soulBuyer.api.SoulBuyerSellDelivery;
import bm.b0b0b0.soulBuyer.api.SoulBuyerSellReturnPolicy;
import org.bukkit.Bukkit;

SoulBuyerApi api = SoulBuyerApiProvider.get();
// or: Bukkit.getServicesManager().load(SoulBuyerApi.class)

if (!api.isReady()) {
    return;
}
```

`SoulBuyerApiProvider.get()` always returns an object: if SoulBuyer is not ready yet — stub with `isReady() == false` and no-op methods.

### Method reference

| Method | Returns | Description |
|--------|---------|-------------|
| `isReady()` | `boolean` | Plugin loaded, catalog and economy ready |
| `openBuyerMenu(Player)` | — | Open standard buyer GUI |
| `quoteItem(Player, itemId)` | `Optional<ItemUnitQuote>` | Price and points for 1 unit with market, multipliers, inventory amount |
| `quoteStack(Player, ItemStack)` | `Optional<ItemUnitQuote>` | Same for item in hand / slot |
| `marketCoefficient(itemId)` | `double` | Current market coefficient (1.0 = base) |
| `cachedPoints(Player)` | `double` | Progression points from cache |
| `cachedMultiplier(Player)` | `double` | Total personal multiplier (permission × category + boosters) |
| `cachedCategoryXp(Player, categoryId)` | `double` | Category XP from cache |
| `fetchProgress(UUID)` | `CompletableFuture<PlayerProgress>` | Async load progress from storage; updates cache |
| `isSellable(itemId)` | `boolean` | Item in `items.yml` pool |
| `isInActiveCatalog(itemId)` | `boolean` | Item in current rotation |
| `activeCatalogSize()` | `int` | Items in rotation now |
| `categoryId(itemId)` | `Optional<String>` | Category from `items.yml` (`ores`, `mobs`, `plants`, `blocks`, `misc`, …) |
| `categoryId(ItemStack)` | `Optional<String>` | Category by material + custom model data |
| `isAutosellFeatureEnabled()` | `boolean` | Autosell enabled server-wide |
| `canUseAutosell(Player)` | `boolean` | Player has autosell permission |
| `isAutosellEnabled(Player)` | `boolean` | Player master autosell switch (not category/item details) |
| `isSaleInProgress(Player)` | `boolean` | Sale transaction in progress (secure storage busy) |
| `sellAll(Player)` | `boolean` | Sell all buyable from inventory |
| `sellAll(Player, Runnable)` | `boolean` | Same + callback on completion |
| `sellAll(Player, SoulBuyerSellDelivery, Runnable)` | `boolean` | Sale with notification channel choice |
| `sellItem(Player, itemId)` | `boolean` | Sell all of item-id from inventory |
| `sellItem(Player, itemId, Runnable)` | `boolean` | Same + callback |
| `sellItemAmount(Player, itemId, amount, Runnable)` | `boolean` | Sell specified amount from inventory |
| `sellStacks(Player, stacks)` | `boolean` | Sell given stacks (see below) |
| `sellStacks(Player, stacks, Runnable)` | `boolean` | Same + callback |
| `sellStacks(Player, stacks, returnPolicy, delivery, Runnable)` | `boolean` | Full control of return policy and notifications |

Sell methods return `false` if API not ready, player null, list empty, or another sale in progress. `true` — transaction **accepted**; check callback and player messages for result.

### Models

**`ItemUnitQuote`** — `unitPrice`, `unitPoints`, `marketCoefficient`, `playerMultiplier`, `inventoryAmount`.

**`PlayerProgress`** — `playerId`, `points`, `categoryXp` (map categoryId → XP).

### Selling from inventory

`sellAll`, `sellItem`, `sellItemAmount` work like SoulBuyer GUI buttons: take items from player inventory, apply limits, boosters, market, progression, payout (Vault / PlayerPoints per config). On error, unsold items return to inventory.

```java
if (api.isSaleInProgress(player)) {
    return;
}
api.sellAll(player, SoulBuyerSellDelivery.ACTION_BAR, () -> {
    // main thread, after transaction completes
    player.sendMessage("Done!");
});
```

### Selling from your GUI — `sellStacks`

For a custom menu with “Sell all” button:

1. Build `List<ItemStack>` from your GUI slots.
2. Filter by category via `categoryId` if needed.
3. **Remove stacks from GUI** before call (with `CALLER_OWNS_ITEMS`).
4. Call `sellStacks`.

SoulBuyer calculates price, limits, boosters, points, category XP and pays the player. Multiple stacks in one call — one transaction.

```java
List<ItemStack> toSell = new ArrayList<>();
for (ItemStack stack : guiSlots) {
    if (stack == null || stack.isEmpty()) {
        continue;
    }
    api.categoryId(stack).filter("ores"::equals).ifPresent(category -> toSell.add(stack.clone()));
}
if (toSell.isEmpty() || api.isSaleInProgress(player)) {
    return;
}

// remove toSell from your GUI before call
boolean started = api.sellStacks(
        player,
        toSell,
        SoulBuyerSellReturnPolicy.CALLER_OWNS_ITEMS,
        SoulBuyerSellDelivery.SILENT,
        () -> refreshMyGui(player)
);
if (!started) {
    // return items to GUI manually
}
```

| `SoulBuyerSellReturnPolicy` | Behavior on cancel / error |
|-----------------------------|----------------------------|
| `RETURN_TO_PLAYER` | Default. Unsold returned to player inventory |
| `CALLER_OWNS_ITEMS` | SoulBuyer does not touch items — your plugin rolls back GUI |

| `SoulBuyerSellDelivery` | Player notification |
|-------------------------|---------------------|
| `CHAT` | Chat message (default) |
| `ACTION_BAR` | Action bar |
| `SILENT` | No SoulBuyer messages |

Some stacks may not sell due to **daily limit** — excess returns to player (or stays with caller under `CALLER_OWNS_ITEMS`).

### Categories — `categoryId`

Category comes from `category` field in `items.yml` for each item-id. Typical values: `ores`, `mobs`, `plants`, `blocks`, `misc`. Custom ids are fine — must match GUI filters and autosell.

```java
api.categoryId("raw_iron");           // Optional["ores"]
api.categoryId(player.getInventory().getItemInMainHand()); // by material + CMD
api.isSellable("raw_iron");          // true if id in pool
api.isInActiveCatalog("raw_iron");   // true if currently in rotation
```

### Progress cache

`cachedPoints` / `cachedMultiplier` / `cachedCategoryXp` read memory. After join data loads async; for exact values before opening menu call `fetchProgress(uuid).join()` or wait for callback.

`fetchProgress` is safe from async threads; mutate Bukkit API from its callback only via `runTask`.

**Autosell and API:** category and per-item selection is configured only through SoulBuyer GUI (`gui/autosell.yml`). No `toggleAutosellItem` / `getDisabledItems` in `SoulBuyerApi` — for custom menus use `sellStacks` and your own filtering via `categoryId` / `isInActiveCatalog`.

### Dependency in `plugin.yml`

```yaml
softdepend: [SoulBuyer]
```

or `depend: [SoulBuyer]` if your plugin cannot work without the buyer.

---
