package com.mowmaster.ironpedestals.ClientSide;

import com.mojang.blaze3d.platform.InputConstants;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import com.mowmaster.ironpedestals.Items.Augments.AugmentType;
import com.mowmaster.ironpedestals.Menus.PedestalLinkPayload;
import com.mowmaster.ironpedestals.Menus.PedestalMenu;
import com.mowmaster.ironpedestals.Menus.PedestalSlots;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.client.MowLibClientParticles;
import com.mowmaster.mowlib.client.SlotGrid;
import com.mowmaster.mowlib.transport.TransportStorage;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.resource.Resource;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// The pedestal's menu screen (PedestalMenu), on MowLib's reading paper:
//   left paper  - the pedestal's item stacks (pages of 27 past the first 27: click the arrows or use the mouse
//                 wheel), the upgrade / tool / filter / work card slots and the three augment slots (empty ones show
//                 their item faded: a "ghost"), and the player's inventory
//   right paper - the linked pedestals, each with a toggle that outlines it in the world (PedestalOutlines)
// Slots, clicks, shift-clicks and dragging are vanilla's (AbstractContainerScreen); this only draws the paper,
// slot frames, ghosts and the link list, and handles the link toggles and page turning.
public class PedestalScreen extends AbstractContainerScreen<PedestalMenu>
{
    private static final String MODID = IronPedestalReferences.MODID;
    // MowLib's GUI sprite (textures/gui/sprites/reading_paper.png, nine-sliced by its .png.mcmeta)
    private static final Identifier PAPER = MowLibReferences.id("reading_paper");
    // Ink on paper (0xAARRGGBB): the reading HUD's dark brown, and a faded one
    private static final int INK = 0xFF3B2A1A;
    private static final int FADED_INK = 0x993B2A1A;
    // Slots on paper: a tan border, a slightly darker cream inside; the ghost is faded with the slot colour
    private static final int SLOT_BORDER = 0xFFA08A5E;
    private static final int SLOT_FILL = 0xFFDCCCA0;
    private static final int GHOST_FADE = 0xB0DCCCA0;
    // An augment that's off is faded with the paper's own colour (it has no slot behind it)
    private static final int PAPER_FADE = 0xB0EDE0BB;
    // A link row's remove button and the square for a link that isn't loaded (no colour to show)
    private static final int REMOVE_RED = 0xFFC0302A;
    private static final int UNKNOWN_TINT = 0xFF8A8A8A;
    private static final int BOX = 9;

    private static final int GAP = 4;
    private static final int LINKS_WIDTH = 172;
    private static final int PAD = 8;
    private static final int LINE = 10;
    private static final int LINK_ROW = 12;
    private static final int LINK_ROWS_SHOWN = 12;
    private static final int LINKS_HEIGHT = PAD * 2 + LINE + 2 + LINK_ROWS_SHOWN * LINK_ROW;

    // Gauges for the other storages (fluid tanks, energy, experience...), in a column right of the item stacks
    private static final int GAUGE_WIDTH = 10;
    private static final int GAUGE_STEP = 12;
    private static final int GAUGE_HEIGHT = PedestalMenu.ROWS * 18;
    private static final int TICK = 0x993B2A1A;

    // The left paper's width: the menu's, plus the gauge column
    private final int mainWidth;
    private int linkScroll;
    // The rows drawn last frame (for clicks and tooltips)
    private final List<LinkRow> shownLinks = new ArrayList<>();

    // One row of the link list: the other pedestal, and whether this one sends to it (or it sends here)
    private record LinkRow(BlockPos other, boolean outgoing) {}

    public PedestalScreen(PedestalMenu menu, Inventory inventory, Component title)
    {
        // The image is both papers side by side, so vanilla centres the pair
        super(menu, inventory, title, mainWidth(menu) + GAP + LINKS_WIDTH, PedestalMenu.HEIGHT);
        this.mainWidth = mainWidth(menu);
        inventoryLabelY = PedestalMenu.INVENTORY_Y - 11;
    }

    private BasePedestalBlockEntity pedestal()
    {
        return menu.getPedestal();
    }

    // The left paper is wider by one gauge per tank / amount storage (worked out once: a storage that changes size
    // closes the menu anyway). Static: it's needed before super() has run.
    private static int mainWidth(PedestalMenu menu)
    {
        int gauges = menu.gauges().size();
        return gauges == 0 ? PedestalMenu.WIDTH : PedestalMenu.WIDTH + gauges * GAUGE_STEP + 2;
    }

    private int linksLeft()
    {
        return leftPos + mainWidth + GAP;
    }

    // ---- Drawing ----

    // Under the slots: the papers, the slot frames, the ghosts and the link list
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PAPER, leftPos, topPos, mainWidth, PedestalMenu.HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PAPER, linksLeft(), topPos, LINKS_WIDTH, LINKS_HEIGHT);

        for (Slot slot : menu.slots)
        {
            if (!slot.isActive()) continue;
            // A slot's x/y is where its item goes; the frame is one pixel further out
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            SlotGrid.slot(graphics, x, y, SLOT_BORDER, SLOT_FILL);
            if (slot instanceof PedestalSlots.Part part && !slot.hasItem())
            {
                graphics.item(part.getPart().ghost(), x + 1, y + 1);
                SlotGrid.fill(graphics, x, y, GHOST_FADE);
            }
        }
        extractAugmentButtons(graphics);
        extractGauges(graphics);
        extractLinks(graphics);
    }

    // ---- Gauges ----

    private int gaugeX(int index)
    {
        return leftPos + PedestalMenu.WIDTH - 2 + index * GAUGE_STEP;
    }

    private int gaugeY()
    {
        return topPos + PedestalMenu.ITEMS_Y - 1;
    }

    // The gauge under the point, or -1
    private int gaugeAt(double mouseX, double mouseY)
    {
        if (mouseY < gaugeY() || mouseY >= gaugeY() + GAUGE_HEIGHT) return -1;
        List<PedestalMenu.Gauge> gauges = menu.gauges();
        for (int index = 0; index < gauges.size(); index++)
        {
            if (mouseX >= gaugeX(index) && mouseX < gaugeX(index) + GAUGE_WIDTH) return index;
        }
        return -1;
    }

    private void extractGauges(GuiGraphicsExtractor graphics)
    {
        List<PedestalMenu.Gauge> gauges = menu.gauges();
        for (int index = 0; index < gauges.size(); index++) extractGauge(graphics, gauges.get(index).type(), gauges.get(index).index(), gaugeX(index), gaugeY());
    }

    // One gauge: a framed column, filled from the bottom as far as the storage is full (a fluid shows its own
    // texture and tint, other types their colour), with tick marks every eighth (longer at the quarters and half).
    // A generic helper so the storage's resource and the type agree on R (see BasePedestalBlockEntity#send).
    private <R extends Resource> void extractGauge(GuiGraphicsExtractor graphics, TransportType<R> type, int index, int x, int y)
    {
        TransportStorage<R> storage = pedestal().getStorage(type);
        graphics.fill(x, y, x + GAUGE_WIDTH, y + GAUGE_HEIGHT, SLOT_BORDER);
        graphics.fill(x + 1, y + 1, x + GAUGE_WIDTH - 1, y + GAUGE_HEIGHT - 1, SLOT_FILL);
        if (index >= storage.size()) return;
        R resource = storage.getResource(index);
        long amount = storage.getAmountAsLong(index);
        long capacity = storage.getCapacityAsLong(index, resource);
        int inner = GAUGE_HEIGHT - 2;
        int filled = capacity <= 0 || amount <= 0 ? 0 : (int) Math.max(1, Math.min(inner, amount * inner / capacity));
        if (filled > 0)
        {
            int top = y + 1 + inner - filled;
            if (resource instanceof FluidResource fluid) extractFluid(graphics, fluid, x + 1, top, GAUGE_WIDTH - 2, filled);
            else graphics.fill(x + 1, top, x + GAUGE_WIDTH - 1, y + 1 + inner, 0xFF000000 | type.getRgb());
        }
        for (int tick = 1; tick < 8; tick++)
        {
            int tickY = y + 1 + inner - inner * tick / 8;
            int length = tick == 4 ? 6 : tick % 2 == 0 ? 4 : 2;
            graphics.fill(x + 1, tickY, x + 1 + length, tickY + 1, TICK);
        }
    }

    // A fluid's still texture (from its block model, the same sprite the world uses), tinted the way NeoForge tints it
    // in buckets (MowLib's fluidColor; -1 = untinted, like lava), stacked in squares up the column
    private static void extractFluid(GuiGraphicsExtractor graphics, FluidResource fluid, int x, int y, int width, int height)
    {
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(fluid.getFluid().defaultFluidState()).stillMaterial().sprite();
        int tint = MowLibClientParticles.fluidColor(fluid);
        int color = tint == -1 ? 0xFFFFFFFF : 0xFF000000 | tint;
        for (int drawn = 0; drawn < height; drawn += width)
        {
            int piece = Math.min(width, height - drawn);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y + height - drawn - piece, width, piece, color);
        }
    }

    // The hover text: "Water: 1000 mB / 32000 mB", or the type's name with 0 / capacity when empty
    private <R extends Resource> List<Component> gaugeTooltip(TransportType<R> type, int index)
    {
        TransportStorage<R> storage = pedestal().getStorage(type);
        List<Component> lines = new ArrayList<>();
        if (index >= storage.size()) return lines;
        R resource = storage.getResource(index);
        long amount = storage.getAmountAsLong(index);
        long capacity = storage.getCapacityAsLong(index, resource);
        lines.add(amount > 0 ? type.describeStored(resource, amount, capacity)
                : Component.empty().append(type.getName()).append(": " + type.amountText(0) + " / " + type.amountText(capacity)));
        if (type == TransportTypes.FLUIDS.get()) lines.add(Component.translatable(MODID + ".screen.fluid_hint").withStyle(ChatFormatting.GRAY));
        return lines;
    }

    // ---- Augment buttons ----

    // The three augment buttons, in a row where the augment slots used to be: each is its icon in a slot frame,
    // faded while off ("ghost") and in full while on. The particle & render augment's icon shows its mode.
    private void extractAugmentButtons(GuiGraphicsExtractor graphics)
    {
        for (AugmentType type : AugmentType.values())
        {
            int x = augmentButtonX(type);
            int y = topPos + PedestalMenu.PARTS_Y - 1;
            // blit draws (part of) any texture: here the whole 16x16 icon at its own size, straight on the paper
            graphics.blit(RenderPipelines.GUI_TEXTURED, type.icon(pedestal().getDiffuserMode()), x + 1, y + 1, 0, 0, 16, 16, 16, 16);
            if (!pedestal().hasAugment(type)) SlotGrid.fill(graphics, x, y, PAPER_FADE);
        }
    }

    // A button's frame's left edge (one pixel left of where an item in it would go)
    private int augmentButtonX(AugmentType type)
    {
        return leftPos + PedestalMenu.AUGMENTS_X + type.ordinal() * 18 - 1;
    }

    // The augment button under the point, or null
    private @Nullable AugmentType augmentAt(double mouseX, double mouseY)
    {
        int y = topPos + PedestalMenu.PARTS_Y - 1;
        if (mouseY < y || mouseY >= y + SlotGrid.SLOT) return null;
        for (AugmentType type : AugmentType.values())
        {
            int x = augmentButtonX(type);
            if (mouseX >= x && mouseX < x + SlotGrid.SLOT) return type;
        }
        return null;
    }

    // The labels, relative to the left paper's corner: the title and tier, the page (when there's more than one),
    // "Inventory". text(..., false): no drop shadow (ink has none).
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        Component heading = title.copy().append(Component.translatable(MODID + ".screen.tier", pedestal().getTier().tier()));
        graphics.text(font, heading, titleLabelX, titleLabelY, INK, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, INK, false);
        if (menu.getPageCount() > 1)
        {
            String page = "< " + (menu.getPage() + 1) + "/" + menu.getPageCount() + " >";
            graphics.text(font, page, PedestalMenu.WIDTH - PAD - font.width(page), titleLabelY, FADED_INK, false);
        }
    }

    // The link list on the right paper: the pedestals this one sends to, then the ones that send to it (loaded ones;
    // only the sender stores a link, so these are found among the client's loaded pedestals). Each row: a toggle box
    // (the outline's colour when on), a square in that pedestal's own colour (its dye, or its tier colour), an arrow
    // button showing the direction (click: turn it round), where it is, and a red X that removes the link.
    private void extractLinks(GuiGraphicsExtractor graphics)
    {
        Level level = pedestal().getLevel();
        List<LinkRow> rows = new ArrayList<>();
        pedestal().getLinks().forEach(link -> rows.add(new LinkRow(link, true)));
        pedestal().getClientSenders().forEach(sender -> rows.add(new LinkRow(sender.getBlockPos(), false)));
        int outgoing = pedestal().getLinks().size();
        int left = linksLeft() + PAD;
        int y = topPos + PAD;
        graphics.text(font, Component.translatable(MODID + ".screen.links", outgoing, Config.MAX_LINKS.getAsInt(), rows.size() - outgoing), left, y, INK, false);
        y += LINE + 2;
        linkScroll = Math.clamp(linkScroll, 0, Math.max(0, rows.size() - LINK_ROWS_SHOWN));
        shownLinks.clear();
        if (rows.isEmpty() || level == null)
        {
            graphics.textWithWordWrap(font, Component.translatable(MODID + ".screen.no_links"), left, y, LINKS_WIDTH - PAD * 2, FADED_INK, false);
            return;
        }
        for (int row = 0; row < LINK_ROWS_SHOWN && linkScroll + row < rows.size(); row++)
        {
            int index = linkScroll + row;
            LinkRow link = rows.get(index);
            BlockPos other = link.other();
            shownLinks.add(link);
            int rowY = y + row * LINK_ROW;
            boolean on = PedestalOutlines.isShown(level.dimension(), other);
            box(graphics, left, rowY, on ? PedestalOutlines.colorFor(index) : SLOT_FILL);
            box(graphics, left + BOX + 3, rowY, pedestalTint(level, other));
            // The direction button: an arrow, right = this pedestal sends to it, left = it sends here
            int arrowX = arrowX();
            box(graphics, arrowX, rowY, SLOT_FILL);
            graphics.text(font, link.outgoing() ? "\u2192" : "\u2190", arrowX + 1, rowY, INK, false);
            int distance = (int) Math.round(Math.sqrt(other.distSqr(pedestal().getBlockPos())));
            graphics.text(font, Component.translatable(MODID + ".screen.link_at", other.getX(), other.getY(), other.getZ(), distance), arrowX + BOX + 3, rowY + 1, INK, false);
            // The remove button: a red X at the row's right end
            int removeX = removeX();
            graphics.fill(removeX, rowY, removeX + BOX, rowY + BOX, SLOT_BORDER);
            graphics.text(font, "x", removeX + 2, rowY, REMOVE_RED, false);
        }
    }

    private int arrowX()
    {
        return linksLeft() + PAD + 2 * (BOX + 3);
    }

    private boolean overArrow(double mouseX)
    {
        return mouseX >= arrowX() && mouseX < arrowX() + BOX;
    }

    // A small framed square of one colour
    private static void box(GuiGraphicsExtractor graphics, int x, int y, int color)
    {
        graphics.fill(x, y, x + BOX, y + BOX, SLOT_BORDER);
        graphics.fill(x + 1, y + 1, x + BOX - 1, y + BOX - 1, color);
    }

    // The colour a linked pedestal shows (what its model is tinted with), grey if it isn't loaded or isn't a pedestal
    private static int pedestalTint(Level level, BlockPos pos)
    {
        if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof BasePedestalBlockEntity linked)) return UNKNOWN_TINT;
        return 0xFF000000 | linked.getTint(0);
    }

    private int removeX()
    {
        return linksLeft() + LINKS_WIDTH - PAD - BOX;
    }

    // On top of everything: tooltips for the link rows and the empty part slots (vanilla does the items' own)
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        Level level = pedestal().getLevel();
        int row = linkRowAt(mouseX, mouseY);
        if (row >= 0 && level != null)
        {
            LinkRow link = shownLinks.get(row);
            boolean on = PedestalOutlines.isShown(level.dimension(), link.other());
            String key = mouseX >= removeX() ? ".screen.link_remove"
                    : overArrow(mouseX) ? (link.outgoing() ? ".screen.link_sends" : ".screen.link_receives")
                    : on ? ".screen.outline_hide" : ".screen.outline_show";
            List<Component> lines = level.isLoaded(link.other()) ? List.of(Component.translatable(MODID + key))
                    : List.of(Component.translatable(MODID + key), Component.translatable(MODID + ".screen.unloaded"));
            graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
        }
        else if (gaugeAt(mouseX, mouseY) >= 0 && menu.getCarried().isEmpty())
        {
            PedestalMenu.Gauge gauge = menu.gauges().get(gaugeAt(mouseX, mouseY));
            graphics.setTooltipForNextFrame(font, gaugeTooltip(gauge.type(), gauge.index()), Optional.empty(), mouseX, mouseY);
        }
        else if (augmentAt(mouseX, mouseY) != null)
        {
            AugmentType type = augmentAt(mouseX, mouseY);
            boolean on = pedestal().hasAugment(type);
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable(MODID + ".screen.augment." + type.key()));
            lines.add(Component.translatable(MODID + (on ? ".screen.augment_on" : ".screen.augment_off")));
            if (type == AugmentType.DIFFUSER)
            {
                lines.add(Component.translatable(AugmentType.diffuserModeKey(pedestal().getDiffuserMode())));
                lines.add(Component.translatable(MODID + ".screen.augment_mode_hint"));
            }
            graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
        }
        else if (hoveredSlot instanceof PedestalSlots.Part part && !part.hasItem() && menu.getCarried().isEmpty())
        {
            // ironpedestals.screen.slot.<part>: what goes in it
            graphics.setTooltipForNextFrame(font, Component.translatable(MODID + ".screen.slot." + part.getPart().name().toLowerCase(Locale.ROOT)), mouseX, mouseY);
        }
    }

    // ---- Clicks and scrolling ----

    // The link row under the point, or -1
    private int linkRowAt(double mouseX, double mouseY)
    {
        int left = linksLeft() + PAD;
        int top = topPos + PAD + LINE + 2;
        if (mouseX < left || mouseX >= linksLeft() + LINKS_WIDTH - PAD || mouseY < top) return -1;
        int row = (int) ((mouseY - top) / LINK_ROW);
        return row < shownLinks.size() ? row : -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        // Augment buttons: left-click switches it on/off, right-click on the particle & render one changes its mode.
        // The server does it (vanilla's menu button message, PedestalMenu#clickMenuButton); the screen shows the
        // result when the pedestal's update arrives.
        // A fluid gauge clicked with something on the cursor: the server fills or empties it (a bucket, a tank...)
        int gauge = gaugeAt(event.x(), event.y());
        if (gauge >= 0)
        {
            PedestalMenu.Gauge clicked = menu.gauges().get(gauge);
            if (clicked.type() == TransportTypes.FLUIDS.get() && !menu.getCarried().isEmpty())
            {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, PedestalMenu.FLUID_TANK + clicked.index());
            }
            return true;
        }

        AugmentType augment = augmentAt(event.x(), event.y());
        if (augment != null)
        {
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, augment.ordinal());
            else if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && augment == AugmentType.DIFFUSER)
            {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, PedestalMenu.CYCLE_DIFFUSER);
            }
            return true;
        }
        // Since 26.3 mouse buttons use SDL's numbers, so compare with InputConstants rather than 0
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT)
        {
            Level level = pedestal().getLevel();
            int row = linkRowAt(event.x(), event.y());
            if (row >= 0 && level != null)
            {
                LinkRow link = shownLinks.get(row);
                if (event.x() >= removeX())
                {
                    // Removing and turning round happen on the server (PedestalLinkPayload); the outline goes now
                    ClientPacketDistributor.sendToServer(new PedestalLinkPayload(link.other(), false));
                    PedestalOutlines.hide(level.dimension(), link.other());
                }
                else if (overArrow(event.x()))
                {
                    ClientPacketDistributor.sendToServer(new PedestalLinkPayload(link.other(), true));
                }
                else
                {
                    PedestalOutlines.toggle(level.dimension(), pedestal().getBlockPos(), link.other(), PedestalOutlines.colorFor(linkScroll + row));
                }
                return true;
            }
            // The page label "< 1/2 >": its left part goes back, its right part forward
            if (menu.getPageCount() > 1 && event.y() >= topPos + titleLabelY - 2 && event.y() < topPos + titleLabelY + 10
                    && event.x() >= leftPos + PedestalMenu.WIDTH / 2 && event.x() < leftPos + PedestalMenu.WIDTH)
            {
                menu.setPage(menu.getPage() + (event.x() < leftPos + PedestalMenu.WIDTH - PAD - 20 ? -1 : 1));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    // The wheel scrolls the link list (over the right paper) or turns item pages (over the items)
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (scrollY != 0)
        {
            int step = (int) -Math.signum(scrollY);
            if (mouseX >= linksLeft())
            {
                linkScroll += step;
                return true;
            }
            if (menu.getPageCount() > 1 && mouseY < topPos + PedestalMenu.PARTS_Y)
            {
                menu.setPage(menu.getPage() + step);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
