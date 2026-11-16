# IronPedestals changes once MowLib has the TODO additions

MowLib now has the additions from `mowlib-from-ironpedestals.zip`: merged in MowLib 86506d0, unreleased after 0.0.5,
and unchanged from the zip apart from 26.1.2 version comments. Nothing below has been done yet. Until it is,
`PedestalItemStorage` / `PedestalFluidStorage` override MowLib's methods of the same names (same signatures, same
behaviour), so the mod compiles and works as before.

## 0. Build
- Done: build.gradle asks for `name: "mowlib-${minecraft_version}"` (MowLib publishes per Minecraft version).
- MowLib's `mod_version` is still 0.0.5 in alpha, so the range `[0.0.5,1.0.0)` / `prefer '0.0.5'` stays. The new
  APIs are in whatever 0.0.5 is in mavenLocal: republish MowLib after pulling before building.
- When this mod moves to Stonecutter, MowLib already handles the 26.1.2 differences in the parts used here
  (`LinkOutlines` draws with `ShapeRenderer` there). Test outlines in game on 26.1.2.

## 1. Item slots (`getSlotLimit`, bulk saving)
- `BasePedestalBlockEntity`: override `TransportStorageHost#getSlotLimit(type, resource, normalLimit)` for ITEMS with
  the current `getItemSlotLimit` maths (tier items per slot, scaled for <64 stackers, unstackables stay 1).
- Create the item storage with `TransportTypes.ITEMS.createStorage(this)` (MowLib's `ItemTransportType.Storage`).
- Delete `Transport/PedestalItemStorage.java`. Old saves: MowLib reads the same `bulk_stacks` key with
  `CBBulkItemHandler.STACK_CODEC`. Compare the formats before deleting; if they differ, keep a read-only fallback in
  the BE's load.
- `Menus/PedestalMenu` / `PedestalSlots.Storage`: `storage::set` must point at MowLib's Storage (check that `set`
  is public there, or use `setStacks`). Check how much a slot can take: one normal stack.
- Item overflow on resize: keep the BE's drop code, using MowLib's Storage.

## 2. Fluid tanks (`getTankCount`, `resize`, `tank`, `tankHolding`)
- BE: override `getTankCount(type)` for FLUIDS with `getFluidTankCount`. Create fluids with
  `TransportTypes.FLUIDS.createStorage(this)`.
- Resize once a second / on load: `FluidTransportType.Storage#resize(shrink)` returns the removed fluid. Keep the
  current "drop as a packet" for what comes back.
- `findFluid` / `getFluidAmount` in the BE: re-point them at MowLib's Storage, or drop them for `tankHolding`.
- `ItemUpgradeDropper`, `ItemUpgradeDrain`: use `TransportTypes.FLUIDS.dropInWorld` (now tries every tank) and
  `Storage#tank(i)` / `tankHolding`, not `PedestalFluidStorage#dropInWorld` / `#tankHolding`.
- `PedestalMenu#gauges`: one gauge per `storage.size()` tank, as now.
- Delete `Transport/PedestalFluidStorage.java`. Save format: both use `stacks` (fluid) as a list. Check before
  deleting.

## 3. Cursor bucket clicks
- `PedestalMenu#useFluidContainer(tank)`: becomes
  `StorageInteractions.useCursorContainer(player, TransportTypes.FLUIDS, fluidStorage.tank(tank))`. The button ids
  (`FLUID_TANK + tank`) stay.

## 4. Linking (`ILinkable`, `LinkList`, `LinkResult`)
- BE `implements ILinkable`:
  - `getLinkList()` returns a `new LinkList(this)` field, replacing the `links` list.
  - `getLinkRange()` returns the tier link range.
  - `getMaxLinks()` returns the current link cap.
  - `onLinksChanged()` = setChanged + client update.
  - `getLinkPoint()` = centre + facing offset 0.32 (from `PedestalBeams`).
  - `getLinkColor()` = `getTint(0)`.
  - `getBlockPos` / `getLevel` come from BlockEntity.
- Remove these from the BE: `addLink`, `removeLink`, `toggleLink`, `flipLink`, `removeLinksTo`, its own link result
  type, and the `links` save/load. Call `linkList.save(output)` / `linkList.load(input)` instead. The save key
  `links` is the same, so old pedestals keep their links.
- BE `preRemoveSideEffects`: `LinkList.removeLinksTo(serverLevel, pos, largestTierLinkRange)`.
- serverTick link transfers / round robin: iterate `getLinkList().get()` (or `size()` + `get(i)` for the round-robin
  index). Dead-link cleanup: `getLinkList().removeAll(dead)`.
- `Menus/PedestalLinkPayload`: flip uses `getLinkList().flip(other)` and remove uses `getLinkList().remove(pos)`.
  Show the `LinkResult` message.
- `BasePedestalBlock`: keep the `useItemOn` PASS for linking tools, but check against MowLib's `LinkingToolItem`.

## 5. Linking tool
- `Items/Tools/LinkingToolItem`: delete it and register `new com.mowmaster.mowlib.link.LinkingToolItem(props)` as
  `tool_linkingtool` (keep the `tool_linkingtoolbackwards` alias), or keep a thin subclass if a pedestal-specific
  tooltip is wanted.
- `PedestalDataComponents`: remove `LINKED_POS` / `LINK_STICKY`. Tools in old worlds carry
  `ironpedestals:linked_pos` / `ironpedestals:link_sticky`. Losing a saved position is harmless, but an unknown
  component may fail to load: alias both ids to the `mowlib:` ones (as with the other moved components) or keep
  them registered as deprecated.
- Any placement code that links with the tool in the offhand: use `LinkingToolItem#linkWith`.

## 6. Client
- `BasePedestalBlockEntity.CLIENT_LOADED` / `getClientSenders`: use `ClientLoadedBlockEntities.loaded(this)` in
  `onLoad`, and `unloaded(this)` in `onChunkUnloaded` / `setRemoved`. Incoming links for the menu come from
  `ClientLoadedBlockEntities.all(BasePedestalBlockEntity.class, level)` filtered on `getLinkList().contains(pos)`.
- Delete `ClientSide/PedestalBeams.java`. MowLib's `LinkBeams` draws automatically; check that the out-of-range red
  beam and the tint match.
- Delete `ClientSide/PedestalOutlines.java`. In `PedestalScreen`, the outline toggles call
  `LinkOutlines.toggle / isShown / colorFor / hide`.
- `PedestalScreen`: use `PaperGui` (paper, ink, slot frames, ghost items, augment icon toggles) and `StorageGauge`
  (gauges + tooltips + fluid sprite) in place of the local drawing code. Keep the layout constants.
- Delete `textures/misc/link_beam.png` (MowLib has it).

## 7. Lang (`assets/ironpedestals/lang/en_us.json`)
Remove the keys MowLib now provides:
- `ironpedestals.tool_link_unsucess`, `_removed`, `_itsself`, `_distance`, `_cleared`, `_success_send`,
  `_success_receive`, `_saved_once`, `_saved_sticky`, `_mode_send`, `_mode_receive`
- `ironpedestals.tool_tip_block_selected`, `_block_selected_sticky`, `_link_send`, `_link_receive`, `_link_howto`

Keep the `ironpedestals.screen.link*` keys (the menu is still ours). MowLib's strings say "block" rather than
"pedestal"; override them in our lang file if the wording matters.

## 8. CLAUDE.md
- Empty the MowLib TODO items that were done, and update "Project state":
  - transport: `getSlotLimit` / `getTankCount` instead of our storages
  - fluids: `tank(i)` / `tankHolding`; `dropInWorld` now tries every tank
  - linking: MowLib `link/`, beams/outlines from MowLib
  - screen: `PaperGui` / `StorageGauge`
- Remove the "never `getResource(0)`" workaround notes that no longer apply.
