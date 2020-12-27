package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnderExporter extends ItemUpgradeBase
{
    public ItemUpgradeEnderExporter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {
        return super.canAcceptAdvanced();
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);

            if(!world.isBlockPowered(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(world,pedestalPos,itemInPedestal,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        PlayerEntity player = ((ServerWorld) world).getPlayerByUuid(getPlayerFromCoin(coinInPedestal));
        if(player != null)
        {
            if(itemInPedestal.isEmpty())
            {
                if(hasAdvancedInventoryTargeting(coinInPedestal))
                {
                    ItemStack itemInPlayer = ItemStack.EMPTY;
                    itemInPlayer = IntStream.range(0,player.inventory.getSizeInventory())//Int Range
                            .mapToObj((player.inventory)::getStackInSlot)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!itemInPlayer.isEmpty())
                    {
                        addToPedestal(world,posOfPedestal,itemInPlayer);
                        int getSlot = getPlayerSlotWithMatchingStackExact(player.inventory,itemInPlayer);
                        player.inventory.removeStackFromSlot(getSlot);
                    }
                }
                else
                {
                    ItemStack itemInEnderChest = ItemStack.EMPTY;
                    itemInEnderChest = IntStream.range(0,player.getInventoryEnderChest().getSizeInventory())//Int Range
                            .mapToObj((player.getInventoryEnderChest())::getStackInSlot)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!itemInEnderChest.isEmpty())
                    {
                        addToPedestal(world,posOfPedestal,itemInEnderChest);
                        int getSlot = getEnderChestSlotWithMatchingStackExact(player.getInventoryEnderChest(),itemInEnderChest);
                        player.getInventoryEnderChest().removeStackFromSlot(getSlot);
                    }
                }
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(coin));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(speed);
    }

    public static final Item ENDEREXPORT = new ItemUpgradeEnderExporter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/enderexport"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENDEREXPORT);
    }


}