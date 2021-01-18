package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeExpCollector extends ItemUpgradeBaseExp
{
    public ItemUpgradeExpCollector(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptRange() {return true;}

    public int getAreaWidth(ItemStack stack)
    {
        int areaWidth = 0;
        int aW = getAreaModifier(stack);
        areaWidth = ((aW)+1);
        return  areaWidth;
    }

    @Override
    public int getRangeModifier(ItemStack stack)
    {
        int range = 0;
        if(hasEnchant(stack))
        {
            range = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.RANGE,stack);
        }
        return range;
    }

    public int getRangeHeight(ItemStack stack)
    {
        return getHeight(stack);
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
        return new int[]{getRangeHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    public int getSuckiRate(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = capacityOver*5;
        int suckiRate = 1;
        switch (capacityOver)
        {
            case 0:
                suckiRate = 1;//1
                break;
            case 1:
                suckiRate=5;//2
                break;
            case 2:
                suckiRate = 10;//3
                break;
            case 3:
                suckiRate = 15;//4
                break;
            case 4:
                suckiRate = 20;//5
                break;
            case 5:
                suckiRate=25;//10
                break;
            default: suckiRate=(overEnchanted>maxLVLStored)?(maxLVLStored):(overEnchanted);
        }

        return  suckiRate;
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
                int getMaxXpValue = getExpCountByLevel(getExpBuffer(coinInPedestal));
                if(!hasMaxXpSet(coinInPedestal)) {setMaxXP(coinInPedestal,getMaxXpValue);}
                upgradeActionSendExp(pedestal);

                //added check to make sure it has room for exp before pulling it.
                if (world.getGameTime()%speed == 0 && spaceForXP(coinInPedestal)>0) {
                    upgradeAction(world, coinInPedestal, pedestalPos);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int width = getAreaWidth(coinInPedestal);
        int height = getRangeHeight(coinInPedestal);
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

        List<ExperienceOrbEntity> xpList = world.getEntitiesWithinAABB(ExperienceOrbEntity.class,getBox);
        for(ExperienceOrbEntity getXPFromList : xpList)
        {
            world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.15F, 1.0F);
            TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
            if(pedestalInv instanceof PedestalTileEntity) {
                int currentlyStoredExp = getXPStored(coinInPedestal);
                int value = getXPFromList.getXpValue();
                if(value > 0 && currentlyStoredExp < readMaxXpFromNBT(coinInPedestal))
                {
                    getXPFromList.xpValue=0;
                    getXPFromList.remove();
                    setXPStored(coinInPedestal, currentlyStoredExp + value);
                }
            }
            break;
        }

        int widthP = 0;
        int heightP = 1;
        BlockPos negBlockPosP = getNegRangePosEntity(world,posOfPedestal,widthP,heightP);
        BlockPos posBlockPosP = getPosRangePosEntity(world,posOfPedestal,widthP,heightP);
        BlockState state = world.getBlockState(posOfPedestal);
        if(state.getBlock() instanceof PedestalBlock)
        {
            PedestalTileEntity pedestal = ((PedestalTileEntity)world.getTileEntity(posOfPedestal));

            AxisAlignedBB getBoxP = new AxisAlignedBB(negBlockPosP,posBlockPosP);

            List<Entity> entityList = world.getEntitiesWithinAABB(Entity.class,getBoxP);
            for(Entity getFromList : entityList)
            {
                if(getFromList instanceof PlayerEntity)
                {
                    PlayerEntity getPlayer = ((PlayerEntity)getFromList);
                    ItemStack coin = pedestal.getCoinOnPedestal();
                    if(!getPlayer.isCrouching())
                    {
                        int currentlyStoredExp = getXPStored(coin);
                        if(currentlyStoredExp < readMaxXpFromNBT(coin))
                        {
                            int transferRate = getExpCountByLevel(getSuckiRate(coin));
                            int value = removeXp(getPlayer, transferRate);
                            if(value > 0)
                            {
                                world.playSound((PlayerEntity)null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                setXPStored(coin, currentlyStoredExp + value);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ExperienceOrbEntity)
        {
            ItemStack coin = tilePedestal.getCoinOnPedestal();
            ExperienceOrbEntity getXPFromList = ((ExperienceOrbEntity)entityIn);
            world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.15F, 1.0F);
            int currentlyStoredExp = getXPStored(coin);
            if(currentlyStoredExp < readMaxXpFromNBT(coin))
            {
                int value = getXPFromList.getXpValue();
                getXPFromList.remove();
                setXPStored(coin, currentlyStoredExp + value);
                tilePedestal.update();
            }
        }
    }

    @Override
    public int getExpBuffer(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = (capacityOver*5)+30;

        //20k being the max before we get close to int overflow
        return  (overEnchanted>=maxLVLStored)?(maxLVLStored):(overEnchanted);
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

        int s3 = getAreaWidth(stack);
        String trr = "" + (s3+s3+1) + "";
        String trrr = "" + getRangeHeight(stack) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(trrr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area, Util.DUMMY_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int s3 = getAreaWidth(stack);
        String trr = "" + (s3+s3+1) + "";
        String trrr = "" + getRangeHeight(stack) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(trrr);
        area.appendString(areax.getString());
        area.appendString(trr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.mergeStyle(TextFormatting.GRAY);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item XPMAGNET = new ItemUpgradeExpCollector(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xpmagnet"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPMAGNET);
    }
}
