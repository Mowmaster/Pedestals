package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
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

public class ItemUpgradeMilker extends ItemUpgradeBase
{
    public int rangeHeight = 1;

    public ItemUpgradeMilker(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    public int getRangeWidth(ItemStack stack)
    {
        int rangeWidth = 0;
        int rW = getRangeModifier(stack);
        rangeWidth = ((rW)+1);
        return  rangeWidth;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        int speed = getOperationSpeed(coinInPedestal);
        if(!world.isBlockPowered(pedestalPos))
        {
            if (tick%speed == 0) {
                upgradeAction(world, itemInPedestal, coinInPedestal, pedestalPos);
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int width = getRangeWidth(coinInPedestal);
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,rangeHeight);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,rangeHeight);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);
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
                    int i = getNextSlotWithItemsCap(cap,getStackInPedestal(world,posOfPedestal));
                        if(i>=0)
                        {
                            itemFromInv = handler.getStackInSlot(i);
                            if(itemFromInv.getItem().equals(Items.BUCKET))
                            {
                                //Milking Code Here
                                ItemStack milkBucket = new ItemStack(Items.MILK_BUCKET,1);
                                List<CowEntity> moo = world.getEntitiesWithinAABB(CowEntity.class,getBox);
                                for(CowEntity moomoo : moo)
                                {
                                    if (!moomoo.isChild() && itemInPedestal.equals(ItemStack.EMPTY))
                                    {
                                        world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_COW_MILK, SoundCategory.BLOCKS, 0.5F, 1.0F);
                                        TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                                        if(pedestalInv instanceof TilePedestal) {
                                            handler.extractItem(i,1 ,false );
                                            ((TilePedestal) pedestalInv).addItem(milkBucket);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        //}
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int s3 = getRangeWidth(stack);
        String tr = "" + (s3+s3+1) + "";

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_("2");
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        area.func_240699_a_(TextFormatting.WHITE);
        tooltip.add(area);

        speed.func_240699_a_(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item MILKER = new ItemUpgradeMilker(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/milker"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(MILKER);
    }


}
