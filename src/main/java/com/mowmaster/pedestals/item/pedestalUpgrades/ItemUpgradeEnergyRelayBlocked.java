package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnergyRelayBlocked extends ItemUpgradeBaseEnergyFilter
{
    public ItemUpgradeEnergyRelayBlocked(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    //Since Energy Transfer is as fast as possible, speed isnt needed, just capacity
    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        return false;
    }

    @Override
    public int canAcceptCount(World world, BlockPos posPedestal, ItemStack inPedestal, ItemStack itemStackIncoming)
    {
        return 0;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(stack.getItem() instanceof ItemUpgradeBase && enchantment.getRegistryName().getNamespace().equals(Reference.MODID))
        {
            return !EnchantmentRegistry.COINUPGRADE.equals(enchantment.type) && super.canApplyAtEnchantingTable(stack, enchantment);
        }
        return false;
    }

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return (stack.getCount()==1)?(super.isBookEnchantable(stack, book)):(false);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
            if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}

            if(!world.isBlockPowered(pedestalPos))
            {
                //Always send energy, as fast as we can within the Pedestal Energy Network
                upgradeActionSendEnergy(pedestal);
            }
        }
    }

    public static final Item RFRELAYBLOCKED = new ItemUpgradeEnergyRelayBlocked(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rfrelayblocked"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFRELAYBLOCKED);
    }


}
