package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.pedestals;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeExpBottler extends ItemUpgradeBaseExp
{

    public ItemUpgradeExpBottler(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    public int getTransferRate(ItemStack stack)
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

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (tick%speed == 0) {
                    upgradeAction(world, coinInPedestal, pedestalPos);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        if(!hasMaxXpSet(coinInPedestal)) {setMaxXP(coinInPedestal,getExpCountByLevel(15));}
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        ItemStack itemFromInv = ItemStack.EMPTY;

        //if(world.getTileEntity(posInventory) !=null)
        //{
        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            TileEntity invToPullFrom = world.getTileEntity(posInventory);
            if(invToPullFrom instanceof TilePedestal) {
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
                                int modifier = getTransferRate(coinInPedestal);

                                //If we can extract the correct amount of bottles(If it returns empty then it CANT work)
                                if(!(handler.extractItem(i,modifier ,true ).equals(ItemStack.EMPTY)))
                                {
                                    int rate = (modifier * 11);
                                    ItemStack getBottle = new ItemStack(Items.EXPERIENCE_BOTTLE,modifier);
                                    TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                                    if(pedestalInv instanceof TilePedestal) {
                                        if(((TilePedestal) pedestalInv).canAcceptItems(getBottle)>=rate)
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
                                                    ((TilePedestal) pedestalInv).addItem(getBottle);
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
        //}
    }

    /*@Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {


        if(stack.hasTagCompound())
        {
            if(getTransferRate(stack)>0)
            {
                tooltip.add("Bottled per Opperation: " + tr);
            }
            else
            {
                tooltip.add("Bottled per Opperation: 1");
            }

            if(stack.isItemEnchanted() && getOperationSpeed(stack) >0)
            {

            }
            else
            {
                tooltip.add(TextFormatting.RED + "Operational Speed: Normal Speed");
            }
        }
        else
        {
            tooltip.add("Bottled per Opperation: 1");
            tooltip.add(TextFormatting.RED + "Operational Speed: Normal Speed");
        }
    }*/

    public int getExpBuffer(ItemStack stack)
    {
        return  15;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.func_240702_b_(getExpTransferRateString(stack));
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        rate.func_240699_a_(TextFormatting.GRAY);
        speed.func_240699_a_(TextFormatting.RED);

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
