package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeBabyMaker extends ItemUpgradeBase
{
    public int rangeHeight = 1;

    public ItemUpgradeBabyMaker(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptArea() {return true;}

    @Override
    public boolean canAcceptRange() {
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

            int speed = getOperationSpeed(coinInPedestal);
            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal,world,itemInPedestal,coinInPedestal,pedestalPos);
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

        FakePlayer fakePlayer =  fakePedestalPlayer(pedestal,itemInPedestal).get();

        List<AnimalEntity> animalList = world.getEntitiesWithinAABB(AnimalEntity.class,getBox);
        for(AnimalEntity getAnimalFromList : animalList)
        {
            if(getAnimalFromList != null)
            {
                if (getAnimalFromList.isBreedingItem(itemInPedestal))
                {
                    if (getAnimalFromList.canFallInLove() && getAnimalFromList.getGrowingAge()==0  && !getAnimalFromList.isChild())
                    {
                        pedestal.removeItem(1);
                        getAnimalFromList.setInLove(fakePlayer);
                    }

                    if(hasAdvancedInventoryTargeting(coinInPedestal))
                    {
                        if (getAnimalFromList.isChild()) {
                            pedestal.removeItem(1);
                            if(!pedestal.hasParticleDiffuser())PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,getAnimalFromList.getPosX(),getAnimalFromList.getPosY()+0.5,getAnimalFromList.getPosZ(),76,255,0));
                            getAnimalFromList.ageUp((int)((float)(-getAnimalFromList.getGrowingAge() / 20) * 0.1F), true);
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

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item BABYMAKER = new ItemUpgradeBabyMaker(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/babymaker"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(BABYMAKER);
    }


}
