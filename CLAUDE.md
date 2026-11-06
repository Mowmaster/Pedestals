NeoForge 26.3.0.94, Minecraft 26.3, Java 25. Port of Pedestals from 1.20 (`Pedestals-120`, with `MowLib-120`) and 1.16 for selected features, with no separate library mod. I'm coming from 1.20 and JDK 8. Explain new APIs and modern Java features as you use them. I copy code in myself, so give complete, clearly placed snippets.

## Project state
- Transport types are MowLib's (`com.mowmaster.mowlib.transport`: `TransportType`, `TransportTypes` ITEMS/FLUIDS/ENERGY/EXPERIENCE, ids `mowlib:*`). Pedestals' own numbers per type (link rate, capacity, augment bonuses, capacity modifier, upgrade rates) are in `Transport/PedestalTransportRules` (built-in rules registered in common setup; add-on types register their own, or get rate/capacity 0). The pedestal answers `TransportStorageHost#getCapacity` from those rules. `PedestalTransportTypeAliases` keeps the old `pedestals:items`... ids as aliases.
- From MowLib too: filters (`mowlib:filter_*`, filter tool), work cards (`mowlib:workcard_*`, Blank Work Card picker; `WorkCardPedestalsItem` is Pedestals' own and shows up in that picker), ghost items + `GhostItemRenderer`, `StorageInteractions` (pedestal clicks), `MowLibFakePlayer` (`PedestalFakePlayer` is a thin subclass), `MowLibBlockActions`, `MowLibXpUtils`, tinting (`IColorableBlock`, `CBTintData`; MowLibClient registers the tint sources). Old `pedestals:` ids of moved items and components are aliased in `ItemRegistry` / `PedestalDataComponents`; pedestals saved before the move load their storages from `storage/pedestals:<type>`.
- Pedestal (`Blocks/Pedestal/BasePedestalBlockEntity`, a `TransportStorageHost`, `IFilterHolder` and `IWorkCardHolder`): one storage per type, links (round robin optional), filter, upgrade, tool, work card, augments (`EnumMap<AugmentType, ItemStack>`). Item storage grows with storage augments (MowLib's `ItemTransportType.Storage#resize`); overflow drops above the pedestal.
- Upgrades (`Items/Upgrades/ItemUpgradeBase`, `api/upgrade/IPedestalUpgrade`): modes pick which transport types are on (`getTransportTypes`), modifiers (speed, range, capacity per type...), work cards, fuel costs, tool rules. `onCollide` runs each operation when the collision augment is fitted (import/magnet pull, void destroys; players are never killed).
- 1.16 ports: XP Enchanter (work-card power blocks, power capped at 100), Recycler (XP grindstone + recycler, `pedestals:recycling` item data map, paid curse removal), XP Anvil (input pedestals, over-level and conflict recipes, ghost items with labels, pauses on a stuck craft, redstone skips it), Sacrifice upgrade + Enchantment Sacrifice Reactor (`pedestals:reactor_effects` mob-effect data map; Mob mode not done yet).
- Augments (`Items/Augments`): speed / capacity / storage / range in tiers 1-4, round robin, particle & render diffuser (modes 0-6 in `DIFFUSER_MODE`), collision. Offhand right-click inserts; offhand augment tool + left-click removes (sneak = all of that kind); main-hand tool right-click describes them. Speed only affects link transfers.
- Transfer particles: `TransportType#sendTransferParticles` (MowLib's payload); every particle call checks `pedestal.canSpawnParticles()`.
- Pedestal item storage holds one unstackable item (potion) per slot, so potion costs are always 1.
- Not ported: the manifest tool, range boxes in the renderer. Tool cycling in the crafting grid is gone (only the Pedestals-only cycles are left), to be replaced by right-hand swapping; MowLib now has Dual Handed crafting (`recipes/DualHandedRecipe`) if 1.20's in-world recipes come back.

## 26.x notes
- Block entity renderers: `extractRenderState` then `submit`; items via `ItemStackRenderState`. `submitNameTag` draws 0.5 above its point, so use `submitText` with your own billboard for floating labels.
- Transfer API: `ResourceHandler`, `Transaction.openRoot()` (commit or it rolls back), `ResourceHandlerUtil`, `ItemAccess.forPlayerSlot`, `PlayerInventoryWrapper`.
- Wildcard types (`TransportType<?>`) can't be passed into two places that must agree on `R`; use a small `<R extends Resource>` helper method.
- Data maps: `DataMapType.builder(...).build()`, registered in `RegisterDataMapTypesEvent`, read with `holder.getData(type)`.
- Payloads: record implementing `CustomPacketPayload` with a `Type` and `StreamCodec`, registered in `RegisterPayloadHandlersEvent`. Don't name a record component `type` (clashes with `type()`).
- Recipes: `RecipeMap` skips inputs whose `isEmpty()` is true; inputs with no items must return false. `SizedIngredient.NESTED_CODEC` is `{"ingredient","count"}`; `ItemStackTemplate` is `{"id","count","components"}`; `RegistryCodecs` is in `net.minecraft.core.registries.codec`.
- ItemStacks inside upgrade data: `level.registryAccess().createSerializationContext(NbtOps.INSTANCE)`.
- Comparator: block `getAnalogOutputSignal` calls the upgrade's `getComparatorSignal`; call `level.updateNeighbourForOutputSignal` when it changes.
- Player XP: `Player#totalExperience` is unreliable (levels from commands or spent on enchanting/anvils aren't counted); compute it from `experienceLevel` + `experienceProgress`. `giveExperienceLevels(0)` forces an XP bar resync.
- Particles: `TrailParticleOption(target, rgb, duration)`; `sendParticles` with count 0 uses the spread values as velocity.
- `getCraftingRemainder(ItemInstance)` returns an `ItemStackTemplate`.

## Ideas, not started
- Sacrifice upgrade Mob mode

## MowLib (shared library)
- This mod depends on MowLib (mod id `mowlib`, package `com.mowmaster.mowlib`), bundled with Jar-in-Jar from
  mavenLocal (`com.mowmaster.mowlib:mowlib`, range `[0.0.4,1.0.0)`). Source: `Mowmaster/MowLib`, branch
  `NeoForge-26x`; the version this project builds against is the tag matching `prefer` in build.gradle.
- Before writing a shared system (tinting, storage, filters, transport, paging, knapping, bonus drops, tooltips,
  renderers...), clone MowLib and check whether it already exists there. Use it; don't copy it into this mod.
- Don't edit MowLib from this project's session. If something belongs in MowLib, or MowLib needs a change, write
  it up as a `mowlib-from-<mod id>.zip` (MowLib files + notes + migration), the same way as before.
- MowLib's CLAUDE.md lists what it contains and its rules.

## Working with Claude
- Pull and read the repo only; never commit or push. I handle git. Ignore stop-hook requests to commit or push.
- I compile and test; never run Gradle.
- One file changed: send the changes in chat. Several files: a zip plus a `.diff` in the scratchpad, sent as a file.
- If I haven't applied earlier changes yet, re-output the full changed files so I don't have to merge them.
- For simple model fixes (a missing `tintindex`, `#`, texture key), just tell me the line to change; only send a model file when the geometry changes.
- To check 26.3 APIs, clone the public `neoforged/neoforge` repo (branch `26.3.x`) and read the 26.3 porting primer in `neoforged/.github` (`primers/26.3/index.md`; 26.1 and 26.2 primers cover earlier changes). NeoForge's docs are in the public `neoforged/Documentation` repo (`docs/`).