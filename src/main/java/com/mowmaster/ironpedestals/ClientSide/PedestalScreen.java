package com.mowmaster.ironpedestals.ClientSide;

import com.mojang.blaze3d.platform.InputConstants;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import com.mowmaster.ironpedestals.Menus.PedestalMenu;
import com.mowmaster.ironpedestals.Menus.PedestalSlots;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.client.SlotGrid;
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

    private static final int GAP = 4;
    private static final int LINKS_WIDTH = 150;
    private static final int PAD = 8;
    private static final int LINE = 10;
    private static final int LINK_ROW = 12;
    private static final int LINK_ROWS_SHOWN = 12;
    private static final int LINKS_HEIGHT = PAD * 2 + LINE + 2 + LINK_ROWS_SHOWN * LINK_ROW;

    private int linkScroll;
    private final List<BlockPos> shownLinks = new ArrayList<>();

    public PedestalScreen(PedestalMenu menu, Inventory inventory, Component title)
    {
        // The image is both papers side by side, so vanilla centres the pair
        super(menu, inventory, title, PedestalMenu.WIDTH + GAP + LINKS_WIDTH, PedestalMenu.HEIGHT);
        inventoryLabelY = PedestalMenu.INVENTORY_Y - 11;
    }

    private BasePedestalBlockEntity pedestal()
    {
        return menu.getPedestal();
    }

    private int linksLeft()
    {
        return leftPos + PedestalMenu.WIDTH + GAP;
    }

    // ---- Drawing ----

    // Under the slots: the papers, the slot frames, the ghosts and the link list
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PAPER, leftPos, topPos, PedestalMenu.WIDTH, PedestalMenu.HEIGHT);
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
        extractLinks(graphics);
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

    // The link list on the right paper: a toggle box (the outline's colour when on) and where each link is
    private void extractLinks(GuiGraphicsExtractor graphics)
    {
        Level level = pedestal().getLevel();
        List<BlockPos> links = pedestal().getLinks();
        int left = linksLeft() + PAD;
        int y = topPos + PAD;
        graphics.text(font, Component.translatable(MODID + ".screen.links", links.size(), Config.MAX_LINKS.getAsInt()), left, y, INK, false);
        y += LINE + 2;
        linkScroll = Math.clamp(linkScroll, 0, Math.max(0, links.size() - LINK_ROWS_SHOWN));
        shownLinks.clear();
        if (links.isEmpty() || level == null)
        {
            graphics.textWithWordWrap(font, Component.translatable(MODID + ".screen.no_links"), left, y, LINKS_WIDTH - PAD * 2, FADED_INK, false);
            return;
        }
        for (int row = 0; row < LINK_ROWS_SHOWN && linkScroll + row < links.size(); row++)
        {
            int index = linkScroll + row;
            BlockPos link = links.get(index);
            shownLinks.add(link);
            int rowY = y + row * LINK_ROW;
            boolean on = PedestalOutlines.isShown(level.dimension(), link);
            graphics.fill(left, rowY, left + 9, rowY + 9, SLOT_BORDER);
            graphics.fill(left + 1, rowY + 1, left + 8, rowY + 8, on ? PedestalOutlines.colorFor(index) : SLOT_FILL);
            int distance = (int) Math.round(Math.sqrt(link.distSqr(pedestal().getBlockPos())));
            graphics.text(font, Component.translatable(MODID + ".screen.link_at", link.getX(), link.getY(), link.getZ(), distance), left + 13, rowY + 1, INK, false);
        }
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
            BlockPos link = shownLinks.get(row);
            Component name = level.isLoaded(link) ? level.getBlockState(link).getBlock().getName() : Component.translatable(MODID + ".screen.unloaded");
            boolean on = PedestalOutlines.isShown(level.dimension(), link);
            graphics.setTooltipForNextFrame(font, List.of(name, Component.translatable(MODID + (on ? ".screen.outline_hide" : ".screen.outline_show"))),
                    Optional.empty(), mouseX, mouseY);
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
        // Since 26.3 mouse buttons use SDL's numbers, so compare with InputConstants rather than 0
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT)
        {
            Level level = pedestal().getLevel();
            int row = linkRowAt(event.x(), event.y());
            if (row >= 0 && level != null)
            {
                PedestalOutlines.toggle(level.dimension(), shownLinks.get(row), PedestalOutlines.colorFor(linkScroll + row));
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
