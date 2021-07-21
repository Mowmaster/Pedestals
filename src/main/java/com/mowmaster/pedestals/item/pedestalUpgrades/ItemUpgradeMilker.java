package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.references.Reference;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeMilker extends ItemUpgradeBaseFluid
{
    public int rangeHeight = 1;

    public ItemUpgradeMilker(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptArea() {return true;}

    @Override
    public boolean canAcceptRange() {
        return true;
    }

    @Override
    public boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public boolean canAcceptAdvanced() {
        return true;
    }

    public int getAreaWidth(ItemStack stack)
    {
        int areaWidth = 0;
        int aW = getAreaModifier(stack);
        areaWidth = ((aW)+1);
        return  areaWidth;
    }

    public int getHeight(ItemStack stack)
    {
        return  getRangeTiny(stack);
    }

    public FluidStack getFluidInItem(ItemStack itemInPedestal)
    {
        //TODO: Maybe have a fluid recipe thingy for people to add other tiems that 'contain' fluids??? have an input, fluid, amount, and output for the recipe???
        if(FluidUtil.getFluidHandler(itemInPedestal).isPresent())
        {
            FluidStack fluidInItem = FluidUtil.getFluidContained(itemInPedestal).orElse(FluidStack.EMPTY);
            return fluidInItem;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{getHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                if(hasFluidInCoin(coinInPedestal))
                {
                    upgradeActionSendFluid(pedestal);
                }

                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal, world, itemInPedestal, coinInPedestal, pedestalPos);
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int width = getAreaWidth(coinInPedestal);
        int height = getHeight(coinInPedestal);
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);

        ItemStack itemFromInv = ItemStack.EMPTY;
        ItemStack itemForHand = new ItemStack(Items.BUCKET);
        ItemStack itemMilkBucket = new ItemStack(Items.MILK_BUCKET);


        FakePlayer fakePlayer =  fakePedestalPlayer(pedestal).get();
        fakePlayer.setPosition(posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ());

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(cap.isPresent())
        {
            if(!isInventoryEmpty(cap))
            {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                if (((hasAdvancedInventoryTargeting(coinInPedestal) && invToPullFrom instanceof PedestalTileEntity)||!(invToPullFrom instanceof PedestalTileEntity))?(false):(true)) {
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
                                if(moo.size()>0)
                                {
                                    for(CowEntity moomoo : moo)
                                    {
                                        if (!moomoo.isChild() && itemInPedestal.equals(ItemStack.EMPTY))
                                        {
                                            BlockPos mooie = moomoo.getPosition();
                                            if(!pedestal.hasParticleDiffuser())PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,mooie.getX(),mooie.getY()+0.5,mooie.getZ(),255,255,255));
                                            if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_COW_MILK, SoundCategory.BLOCKS, 0.5F, 1.0F);
                                            if(!handler.extractItem(i,1 ,true ).isEmpty()){
                                                handler.extractItem(i,1 ,false );
                                                pedestal.addItem(milkBucket);
                                                break;
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
        else
        {
            if(availableFluidSpaceInCoin(coinInPedestal)>= FluidAttributes.BUCKET_VOLUME)
            {
                FluidStack fluid = getFluidInItem(itemMilkBucket);
                if(!fluid.isEmpty())
                {
                    List<CowEntity> moo = world.getEntitiesWithinAABB(CowEntity.class,getBox);
                    if(moo.size()>0)
                    {
                        for(CowEntity moomoo : moo)
                        {
                            if(!fakePlayer.getHeldItemMainhand().equals(itemInPedestal))
                            {
                                fakePlayer.setHeldItem(Hand.MAIN_HAND,itemForHand);
                            }
                            ActionResultType result = moomoo.func_230254_b_(fakePlayer,Hand.MAIN_HAND);
                            fluid = getFluidInItem(fakePlayer.getHeldItemMainhand());
                            if (result.isSuccessOrConsume() && addFluid(pedestal,coinInPedestal,fluid,true))
                            {
                                BlockPos mooie = moomoo.getPosition();
                                if(!pedestal.hasParticleDiffuser())PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,mooie.getX(),mooie.getY()+0.5,mooie.getZ(),255,255,255));
                                if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_COW_MILK, SoundCategory.BLOCKS, 0.5F, 1.0F);
                                addFluid(pedestal,coinInPedestal,fluid,false);
                                break;
                            }
                        }
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

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("" + getHeight(stack) + "");
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);

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

        ResourceLocation disabled = new ResourceLocation("pedestals", "enchant_limits/advanced_blacklist");
        ITag<Item> BLACKLISTED = ItemTags.getCollection().get(disabled);

        if(BLACKLISTED !=null)
        {
            //if item isnt in blacklist tag
            if(!BLACKLISTED.contains(stack.getItem()))
            {
                //if any of the enchants are over level 5
                if(intOperationalSpeedOver(stack) >5 || getCapacityModifierOver(stack) >5 || getAreaModifierUnRestricted(stack) >5 || getRangeModifier(stack) >5)
                {
                    //if it doesnt have advanced
                    if(getAdvancedModifier(stack)<=0)
                    {
                        TranslationTextComponent warning = new TranslationTextComponent(Reference.MODID + ".advanced_warning");
                        warning.mergeStyle(TextFormatting.RED);
                        tooltip.add(warning);
                    }
                }
            }
            //if it is
            else
            {
                //Advanced disabled warning only shows after upgrade has advanced on it, isnt great, but alerts the user it wont work unfortunately
                if(getAdvancedModifier(stack)>0)
                {
                    TranslationTextComponent disabled_warning = new TranslationTextComponent(Reference.MODID + ".advanced_disabled_warning");
                    disabled_warning.mergeStyle(TextFormatting.DARK_RED);
                    tooltip.add(disabled_warning);
                }
            }
        }

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("" + getHeight(stack) + "");
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);

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

    public static final Item MILKER = new ItemUpgradeMilker(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/milker"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(MILKER);
    }


}
