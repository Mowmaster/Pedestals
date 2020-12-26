package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeExpBottler extends ItemUpgradeBaseExp
{

    public ItemUpgradeExpBottler(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    public int getBottlingRate(ItemStack stack)
    {
        int bottlingRate = 1;
        switch (getCapacityModifier(stack))
        {
            case 0:
                bottlingRate = 1;
                break;
            case 1:
                bottlingRate=2;
                break;
            case 2:
                bottlingRate = 4;
                break;
            case 3:
                bottlingRate = 8;
                break;
            case 4:
                bottlingRate = 12;
                break;
            case 5:
                bottlingRate=16;
                break;
            default: bottlingRate=1;
        }
        return  bottlingRate;
    }

    @Override
    public int getExpBuffer(ItemStack stack)
    {
        int value = 30;
        switch (getCapacityModifier(stack))
        {
            case 0:
                value = 5;//
                break;
            case 1:
                value=10;//
                break;
            case 2:
                value = 15;//
                break;
            case 3:
                value = 20;//
                break;
            case 4:
                value = 25;//
                break;
            case 5:
                value=30;//
                break;
            default: value=5;
        }

        return  value;
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
                    upgradeAction(world, coinInPedestal, pedestalPos);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int getMaxXpValue = getExpCountByLevel(getExpBuffer(coinInPedestal));
        if(!hasMaxXpSet(coinInPedestal) || readMaxXpFromNBT(coinInPedestal) != getMaxXpValue) {setMaxXP(coinInPedestal, getMaxXpValue);}
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        ItemStack itemFromInv = ItemStack.EMPTY;

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(hasAdvancedInventoryTargeting(coinInPedestal))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            TileEntity invToPullFrom = world.getTileEntity(posInventory);
            if(invToPullFrom instanceof PedestalTileEntity) {
                itemFromInv = ItemStack.EMPTY;
            }
            else {
                if(handler != null)
                {
                    int i = getNextSlotWithItemsCap(cap ,getStackInPedestal(world,posOfPedestal));
                    if(i>=0)
                    {
                        itemFromInv = handler.getStackInSlot(i);
                        int slotCount = itemFromInv.getCount();
                        if(itemFromInv.getItem().equals(Items.GLASS_BOTTLE))
                        {
                            //BottlingCodeHere
                            //11 exp per bottle
                            int modifier = getBottlingRate(coinInPedestal);

                            //If we can extract the correct amount of bottles(If it returns empty then it CANT work)
                            if(!(handler.extractItem(i,modifier ,true ).equals(ItemStack.EMPTY)))
                            {
                                int rate = (modifier * 11);
                                ItemStack getBottle = new ItemStack(Items.EXPERIENCE_BOTTLE,modifier);
                                TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                                if(pedestalInv instanceof PedestalTileEntity) {
                                    if(((PedestalTileEntity) pedestalInv).canAcceptItems(world,posOfPedestal,getBottle)>=rate)
                                    {
                                        int currentlyStoredExp = getXPStored(coinInPedestal);
                                        if(currentlyStoredExp > 0)
                                        {
                                            if(currentlyStoredExp >= rate)
                                            {
                                                int getExpLeftInPedestal = currentlyStoredExp - rate;
                                                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                                setXPStored(coinInPedestal,getExpLeftInPedestal);
                                                handler.extractItem(i,modifier ,false );
                                                ((PedestalTileEntity) pedestalInv).addItem(getBottle);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int modifier = getBottlingRate(coin);
        int rate = (modifier * 11);
        if(!world.isBlockPowered(pos))
        {
            if(getXPStored(coin)>=rate)
            {
                spawnParticleAroundPedestalBase(world,tick,pos,0.2f,0.95f,0.2f,1.0f);
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

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_xp");
        xpstored.appendString(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.DUMMY_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString("" + getBottlingRate(stack) + "");
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
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + getBottlingRate(stack) + "");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        rate.mergeStyle(TextFormatting.GRAY);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item XPBOTTLER = new ItemUpgradeExpBottler(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xpbottler"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPBOTTLER);
    }


}
