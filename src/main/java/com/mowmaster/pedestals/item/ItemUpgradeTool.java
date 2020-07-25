package com.mowmaster.pedestals.item;

import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeTool extends Item {

    public ItemUpgradeTool() {
        super(new Properties().maxStackSize(1).containerItem(UPGRADE).group(PEDESTALS_TAB));
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(this.getItem());
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World worldIn = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getPos();

        if(!worldIn.isRemote)
        {
            BlockState getBlockState = worldIn.getBlockState(pos);
            if(player.isCrouching())
            {

            }
            else
            {
                if(worldIn.getBlockState(pos).getBlock() instanceof BlockPedestalTE) {
                    //Checks Tile at location to make sure its a TilePedestal
                    TileEntity tileEntity = worldIn.getTileEntity(pos);
                    if (tileEntity instanceof TilePedestal) {
                        TilePedestal tilePedestal = (TilePedestal) tileEntity;
                        if(tilePedestal.hasCoin())
                        {
                            Item coinInPed = tilePedestal.getCoinOnPedestal().getItem();
                            if(coinInPed instanceof ItemUpgradeBase)
                            {
                                ((ItemUpgradeBase) coinInPed).chatDetails(player, tilePedestal);
                            }
                        }
                    }
                }
            }
        }

        return super.onItemUse(context);
    }

    public static final Item UPGRADE = new ItemUpgradeTool().setRegistryName(new ResourceLocation(MODID, "upgradetool"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(UPGRADE);
    }




}
