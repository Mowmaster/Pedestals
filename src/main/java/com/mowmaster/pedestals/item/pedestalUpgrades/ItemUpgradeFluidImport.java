package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.crafting.CalculateColor;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFluidImport extends ItemUpgradeBaseFluid
{
    public ItemUpgradeFluidImport(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public boolean canSendItem(PedestalTileEntity tile)
    {
        if(tile !=null)
        {
            return getFluidInItem(tile.getItemInPedestal()).isEmpty();
        }
        return true;
    }

    @Override
    public int canAcceptCount(World world, BlockPos pos,ItemStack inPedestal, ItemStack itemStackIncoming) {

        //If incoming item has a fluid then set max stack to 1, if the pedestal has an item then 0, else allow normal transferring
        return (!getFluidInItem(itemStackIncoming).isEmpty())?(1):((inPedestal.isEmpty())?(itemStackIncoming.getMaxStackSize()):(0));
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{0,0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos)) {
                if (world.getGameTime() % speed == 0) {
                    if(hasFluidInCoin(coinInPedestal))
                    {
                        upgradeActionSendFluid(pedestal);
                    }

                    upgradeAction(pedestal);
                }
            }

        }
    }

    public FluidStack getFluidInItem(ItemStack itemInPedestal)
    {
        //TODO: Maybe have a fluid recipe thingy for people to add other tiems that 'contain' fluids??? have an input, fluid, amount, and output for the recipe???
        if(FluidUtil.getFluidHandler(itemInPedestal).isPresent())
        {
            FluidStack fluidInItem = FluidUtil.getFluidContained(itemInPedestal).get();
            return fluidInItem;
        }
        return FluidStack.EMPTY;
    }

//https://github.com/mekanism/Mekanism/blob/be11c0df7d6ffece12da666b3100fc5e6d8ce0ab/src/main/java/mekanism/common/inventory/slot/IFluidHandlerSlot.java#L137
    public void upgradeAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();

        if(!itemInPedestal.isEmpty())
        {
            FluidStack fluidIn = getFluidInItem(itemInPedestal);
            if(!fluidIn.isEmpty())
            {

                //Need to combine this and that stuff below into 1 thing..
                int amountIn = fluidIn.getAmount();
                int rate = getFluidTransferRate(coinInPedestal);
                //If it can set amount to transfer rate, otherwise check if it has enough for a bucket, if all fails, then check to see what it can transfer
                int transferRate = (amountIn>=rate)?(rate):((amountIn>=FluidAttributes.BUCKET_VOLUME)?(FluidAttributes.BUCKET_VOLUME):(amountIn));

                if(availableFluidSpaceInCoin(coinInPedestal) >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                {
                    FluidStack fluidToInsert = new FluidStack(fluidIn,transferRate);

                    if(!fluidIn.isEmpty() && addFluid(coinInPedestal,fluidToInsert,true))
                    {
                        addFluid(coinInPedestal,fluidIn,false);
                    }
                }

                //Technically already checked for in fluidIn
                //https://github.com/mekanism/Mekanism/blob/be11c0df7d6ffece12da666b3100fc5e6d8ce0ab/src/main/java/mekanism/common/inventory/slot/IFluidHandlerSlot.java#L44
                Optional<IFluidHandlerItem> fluidContainerItemIn = FluidUtil.getFluidHandler(itemInPedestal).resolve();
                if(fluidContainerItemIn.isPresent())
                {
                    IFluidHandlerItem fluidHandlerItem = fluidContainerItemIn.get();
                    FluidStack fluidInActualDrain = fluidHandlerItem.drain(fluidIn,IFluidHandler.FluidAction.EXECUTE);
                    if(!fluidInActualDrain.isEmpty())
                    {
                        //only return the item IF its empty
                        ItemStack returnerStack = fluidHandlerItem.getContainer();
                        addFluid(coinInPedestal,fluidInActualDrain,false);
                        pedestal.setItemInPedestal(returnerStack);
                    }
                }
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getTranslationKey() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getTranslationKey() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getTranslationKey() + ".chat_fluidseperator");
            fluid.appendString("" + fluidStored.getDisplayName().getString() + "");
            fluid.appendString(fluidSplit.getString());
            fluid.appendString("" + fluidStored.getAmount() + "");
            fluid.appendString(fluidLabel.getString());
            fluid.mergeStyle(TextFormatting.BLUE);
            player.sendMessage(fluid,Util.DUMMY_UUID);
        }

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString("" +  getFluidTransferRate(stack) + "");
        rate.appendString(fluidLabel.getString());
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getTranslationKey() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getTranslationKey() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getTranslationKey() + ".chat_fluidseperator");
            fluid.appendString("" + fluidStored.getDisplayName().getString() + "");
            fluid.appendString(fluidSplit.getString());
            fluid.appendString("" + fluidStored.getAmount() + "");
            fluid.appendString(fluidLabel.getString());
            fluid.mergeStyle(TextFormatting.BLUE);
            tooltip.add(fluid);
        }

        TranslationTextComponent fluidcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_fluidcapacity");
        fluidcapacity.appendString(""+ getFluidbuffer(stack) +"");
        fluidcapacity.appendString(fluidLabel.getString());
        fluidcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(fluidcapacity);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + getFluidTransferRate(stack) + "");
        rate.appendString(fluidLabel.getString());
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item FLUIDIMPORT = new ItemUpgradeFluidImport(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidimport"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDIMPORT);
    }


}
