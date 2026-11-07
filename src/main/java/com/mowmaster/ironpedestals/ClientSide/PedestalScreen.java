package com.mowmaster.ironpedestals.ClientSide;

import com.mojang.blaze3d.platform.InputConstants;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import com.mowmaster.ironpedestals.Items.Augments.AugmentType;
import com.mowmaster.ironpedestals.api.upgrade.IPedestalUpgrade;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.client.SlotGrid;
import com.mowmaster.mowlib.transport.TransportStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// What's in a pedestal, on MowLib's reading paper (empty-hand right-click opens it; see BasePedestalBlock):
//   the item stacks; the upgrade, tool, work card (when the upgrade uses one) and filter; the three augments; and the
//   linked pedestals, each with a toggle that outlines it in the world (PedestalOutlines).
// It only shows things (nothing can be taken out here), so there's no menu or packets: the client's copy of the block
// entity already has everything (BasePedestalBlockEntity#getUpdateTag), and it's read again every frame so the
// screen stays current. It closes when the pedestal is gone or the player walks away.
public class PedestalScreen extends Screen
{
    private static final String MODID = IronPedestalReferences.MODID;
    // MowLib's GUI sprite (textures/gui/sprites/reading_paper.png, nine-sliced by its .png.mcmeta)
    private static final Identifier PAPER = MowLibReferences.id("reading_paper");
    // Ink on paper (0xAARRGGBB): the reading HUD's dark brown, and a faded one for empty things
    private static final int INK = 0xFF3B2A1A;
    private static final int FADED_INK = 0x993B2A1A;
    // Slots on paper: a tan border, a slightly darker cream than the paper inside
    private static final int SLOT_BORDER = 0xFFA08A5E;
    private static final int SLOT_FILL = 0xFFDCCCA0;
    private static final int HOVER = 0x80FFFFFF;

    private static final int PAD = 10;
    private static final int SLOT = SlotGrid.SLOT;
    private static final int COLUMNS = 9;
    // One labelled slot (upgrade, tool...): the caption above, the slot under it, this wide
    private static final int CELL = 58;
    private static final int LINE = 10;
    private static final int LINK_ROW = 12;
    private static final int LINK_ROWS_SHOWN = 6;
    private static final int PAPER_WIDTH = PAD * 2 + CELL * 4;
    private static final double MAX_DISTANCE_SQR = 10 * 10;

    private final BlockPos pos;
    // Worked out while drawing, for clicks and scrolling
    private int left;
    private int linksTop;
    private int linkScroll;
    private final List<BlockPos> shownLinks = new ArrayList<>();

    public PedestalScreen(BlockPos pos)
    {
        super(Component.translatable(MODID + ".screen.pedestal"));
        this.pos = pos.immutable();
    }

    // Screens are set on the Gui since 26.2 (Minecraft#gui)
    public static void open(BlockPos pos)
    {
        Minecraft.getInstance().gui.setScreen(new PedestalScreen(pos));
    }

    // The game keeps running behind it, so the contents update while it's open
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    private @Nullable BasePedestalBlockEntity pedestal()
    {
        Level level = minecraft.level;
        return level != null && level.getBlockEntity(pos) instanceof BasePedestalBlockEntity pedestal ? pedestal : null;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (pedestal() == null || minecraft.player == null || minecraft.player.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_DISTANCE_SQR) onClose();    }

    // ---- Drawing (every frame) ----

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        BasePedestalBlockEntity pedestal = pedestal();
        if (pedestal == null) return;

        TransportStorage<ItemResource> items = pedestal.getItemStorage();
        int itemRows = SlotGrid.rows(items.size(), COLUMNS);
        List<BlockPos> links = pedestal.getLinks();
        int linkRows = Math.min(LINK_ROWS_SHOWN, Math.max(1, links.size()));
        int height = PAD + LINE + 4                         // title
                + LINE + itemRows * SLOT + 6                // items
                + LINE + SLOT + 6                           // upgrade, tool, work card, filter
                + LINE + SLOT + 6                           // augments
                + LINE + linkRows * LINK_ROW + PAD;         // links
        left = width / 2 - PAPER_WIDTH / 2;
        int top = Math.max(4, this.height / 2 - height / 2);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PAPER, left, top, PAPER_WIDTH, height);

        int y = top + PAD;
        // Title: the block's name (Copper Pedestal...) and its tier
        Component title = pedestal.getBlockState().getBlock().getName().copy()
                .append(Component.translatable(MODID + ".screen.tier", pedestal.getTier().tier()));
        inkCentred(graphics, title, width / 2, y, INK);
        y += LINE + 4;

        // Item stacks, rows of 9, centred. The count can be past 99 (big stack slots), so it's written separately.
        graphics.text(font, Component.translatable(MODID + ".screen.items"), left + PAD, y, INK, false);
        y += LINE;
        int gridLeft = width / 2 - COLUMNS * SLOT / 2;
        for (int index = 0; index < items.size(); index++)
        {
            int x = SlotGrid.slotX(gridLeft, index, COLUMNS);
            int slotY = SlotGrid.slotY(y, index, COLUMNS, 0);
            ItemStack stack = ItemUtil.getStack(items, index);
            drawSlot(graphics, stack, x, slotY, stack.getCount(), mouseX, mouseY, null);
        }
        y += itemRows * SLOT + 6;

        // Upgrade, tool, work card (only if the upgrade uses one, or one is in), filter
        ItemStack upgrade = pedestal.getUpgrade();
        boolean usesCard = upgrade.getItem() instanceof IPedestalUpgrade pedestalUpgrade && pedestalUpgrade.getWorkCardType(upgrade) != null;
        List<Cell> parts = new ArrayList<>();
        parts.add(new Cell("upgrade", upgrade));
        parts.add(new Cell("tool", pedestal.getTool()));
        if (usesCard || pedestal.hasWorkCard()) parts.add(new Cell("workcard", pedestal.getWorkCard()));
        parts.add(new Cell("filter", pedestal.getFilter()));
        drawCells(graphics, parts, y, mouseX, mouseY);
        y += LINE + SLOT + 6;

        // The augments left after the tiers: round robin, particle & render, collision
        List<Cell> augments = new ArrayList<>();
        for (AugmentType type : AugmentType.values()) augments.add(new Cell("augment_" + type.key(), pedestal.getAugment(type)));
        drawCells(graphics, augments, y, mouseX, mouseY);
        y += LINE + SLOT + 6;

        // Linked pedestals, with a toggle each for its outline
        graphics.text(font, Component.translatable(MODID + ".screen.links", links.size(), Config.MAX_LINKS.getAsInt()), left + PAD, y, INK, false);
        y += LINE;
        linksTop = y;
        linkScroll = Math.clamp(linkScroll, 0, Math.max(0, links.size() - LINK_ROWS_SHOWN));
        shownLinks.clear();
        if (links.isEmpty())
        {
            graphics.text(font, Component.translatable(MODID + ".screen.no_links"), left + PAD, y + 2, FADED_INK, false);
            return;
        }
        for (int row = 0; row < LINK_ROWS_SHOWN && linkScroll + row < links.size(); row++)
        {
            int index = linkScroll + row;
            BlockPos link = links.get(index);
            shownLinks.add(link);
            int rowY = y + row * LINK_ROW;
            boolean on = PedestalOutlines.isShown(minecraft.level.dimension(), link);
            // The toggle: a box, filled with the outline's colour when on
            int boxX = left + PAD;
            graphics.fill(boxX, rowY + 1, boxX + 9, rowY + 10, SLOT_BORDER);
            graphics.fill(boxX + 1, rowY + 2, boxX + 8, rowY + 9, on ? PedestalOutlines.colorFor(index) : SLOT_FILL);
            // What's there and where, and how far
            Component name = minecraft.level.isLoaded(link) ? minecraft.level.getBlockState(link).getBlock().getName() : Component.translatable(MODID + ".screen.unloaded");
            int distance = (int) Math.round(Math.sqrt(link.distSqr(pos)));
            Component where = Component.translatable(MODID + ".screen.link_at", link.getX(), link.getY(), link.getZ(), distance);
            Component text = Component.empty().append(name).append(" ").append(where);
            // Too wide for the paper (long names, big coordinates): just where it is
            if (font.width(text) > PAPER_WIDTH - PAD * 2 - 14) text = where;
            graphics.text(font, text, boxX + 14, rowY + 2, INK, false);
            if (overLinkRow(row, mouseX, mouseY)) graphics.setTooltipForNextFrame(font, Component.translatable(MODID + (on ? ".screen.outline_hide" : ".screen.outline_show")), mouseX, mouseY);
        }
        if (links.size() > LINK_ROWS_SHOWN)
        {
            String more = (linkScroll + 1) + "-" + (linkScroll + shownLinks.size()) + "/" + links.size();
            graphics.text(font, more, left + PAPER_WIDTH - PAD - font.width(more), linksTop - LINE, FADED_INK, false);
        }
    }

    // A caption over a slot, for each cell, spread across the paper
    private void drawCells(GuiGraphicsExtractor graphics, List<Cell> cells, int y, int mouseX, int mouseY)
    {
        int cellsLeft = width / 2 - cells.size() * CELL / 2;
        for (int index = 0; index < cells.size(); index++)
        {
            Cell cell = cells.get(index);
            int centre = cellsLeft + index * CELL + CELL / 2;
            Component caption = Component.translatable(MODID + ".screen." + cell.key());
            inkCentred(graphics, caption, centre, y, cell.stack().isEmpty() ? FADED_INK : INK);
            drawSlot(graphics, cell.stack(), centre - SLOT / 2, y + LINE, cell.stack().getCount(), mouseX, mouseY, caption);
        }
    }

    // Text centred on x, without a drop shadow (ink has none; centeredText always draws one)
    private void inkCentred(GuiGraphicsExtractor graphics, Component text, int x, int y, int color)
    {
        graphics.text(font, text, x - font.width(text) / 2, y, color, false);
    }

    // One slot with its item; the item's tooltip (or, when empty, what goes there) under the mouse
    private void drawSlot(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, long amount, int mouseX, int mouseY, @Nullable Component emptyName)
    {
        SlotGrid.slot(graphics, x, y, SLOT_BORDER, SLOT_FILL);
        SlotGrid.item(graphics, font, stack, x, y, amount);
        if (mouseX < x || mouseX >= x + SLOT || mouseY < y || mouseY >= y + SLOT) return;
        SlotGrid.fill(graphics, x, y, HOVER);
        if (!stack.isEmpty()) graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
        else if (emptyName != null) graphics.setTooltipForNextFrame(font, Component.translatable(MODID + ".screen.empty", emptyName), mouseX, mouseY);
    }

    private boolean overLinkRow(int row, double mouseX, double mouseY)
    {
        int rowY = linksTop + row * LINK_ROW;
        return mouseX >= left + PAD && mouseX < left + PAPER_WIDTH - PAD && mouseY >= rowY && mouseY < rowY + LINK_ROW;
    }

    // ---- Clicking and scrolling ----

    // A left click on a link row toggles its outline
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        // Since 26.3 mouse buttons use SDL's numbers, so compare with InputConstants rather than 0
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && minecraft.level != null)
        {
            for (int row = 0; row < shownLinks.size(); row++)
            {
                if (!overLinkRow(row, event.x(), event.y())) continue;
                PedestalOutlines.toggle(minecraft.level.dimension(), shownLinks.get(row), PedestalOutlines.colorFor(linkScroll + row));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    // The mouse wheel scrolls the link list when there are more than fit
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (scrollY != 0)
        {
            linkScroll -= (int) Math.signum(scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    // A captioned slot: its lang key (ironpedestals.screen.<key>) and what's in it
    private record Cell(String key, ItemStack stack) {}
}
