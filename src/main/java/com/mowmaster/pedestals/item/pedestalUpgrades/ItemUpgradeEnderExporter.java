package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
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
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnderExporter extends ItemUpgradeBase
{
    public ItemUpgradeEnderExporter(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {
        return super.canAcceptAdvanced();
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isClientSide)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getBlockPos();

            int speed = getOperationSpeed(coinInPedestal);

            if(!world.hasNeighborSignal(pedestalPos))
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
        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.NIL_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(coin);
        if(map.size() > 0 && getNumNonPedestalEnchants(map)>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getDescriptionId() + ".chat_enchants");
            enchant.withStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.NIL_UUID);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.withStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants,Util.NIL_UUID);
                }
            }
        }

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".chat_speed");
        speed.append(getOperationSpeedString(coin));
        speed.withStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".tooltip_speed");
        speed.append(getOperationSpeedString(stack));

        speed.withStyle(TextFormatting.RED);

        tooltip.add(speed);
    }

    public static final Item ENDEREXPORT = new ItemUpgradeEnderExporter(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/enderexport"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENDEREXPORT);
    }


}