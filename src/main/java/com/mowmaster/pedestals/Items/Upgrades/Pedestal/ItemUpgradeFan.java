package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.Recipes.BaseBlockEntityFilter;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeFan extends ItemUpgradeBase
{
    public ItemUpgradeFan(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyRange(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyArea(ItemStack upgradeItemStack) {
        return PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get();
    }

    @Override
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 1; }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_fan_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_fan_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_fan_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_fan_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_fan_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_fan_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_fan_dustColor.get(),PedestalConfig.COMMON.upgrade_fan_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_fan_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_fan_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_fan_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_fan_selectedMultiplier.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {

        List<String> messages = super.getUpgradeHUD(pedestal);

        if(messages.size()<=0)
        {
            if(baseEnergyCostPerDistance()>0)
            {
                if(pedestal.getStoredEnergy()<baseEnergyCostPerDistance())
                {
                    messages.add(ChatFormatting.RED + "Needs Energy");
                    messages.add(ChatFormatting.RED + "To Operate");
                }
            }
            if(baseXpCostPerDistance()>0)
            {
                if(pedestal.getStoredExperience()<baseXpCostPerDistance())
                {
                    messages.add(ChatFormatting.GREEN + "Needs Experience");
                    messages.add(ChatFormatting.GREEN + "To Operate");
                }
            }
            if(baseDustCostPerDistance().getDustAmount()>0)
            {
                if(pedestal.getStoredEnergy()<baseEnergyCostPerDistance())
                {
                    messages.add(ChatFormatting.LIGHT_PURPLE + "Needs Dust");
                    messages.add(ChatFormatting.LIGHT_PURPLE + "To Operate");
                }
            }
        }

        return messages;
    }

    @Override
    public ItemStack getUpgradeDefaultTool() {
        return new ItemStack(Items.STICK);
    }

    @Nullable
    protected BaseBlockEntityFilter getRecipeFilterBlock(Level level, ItemStack stackIn) {
        Container container = MowLibContainerUtils.getContainer(1);
        container.setItem(-1,stackIn);
        List<BaseBlockEntityFilter> recipes = level.getRecipeManager().getRecipesFor(BaseBlockEntityFilter.Type.INSTANCE,container,level);
        return level != null ? (recipes.size() > 0)?(recipes.stream().findFirst().get()):(null) : null;
    }

    protected String getProcessResultFilterBlock(BaseBlockEntityFilter recipe) {
        return (recipe == null)?(""):(recipe.getResultEntityString());
    }

    protected int getProcessResultFilterBlockMobType(BaseBlockEntityFilter recipe) {
        return (recipe == null)?(1):(recipe.getResultMobType());
    }

    protected boolean getProcessResultFilterBlockIsBaby(BaseBlockEntityFilter recipe) {
        return (recipe == null)?(false):(recipe.getResultBaby());
    }

    public boolean hasBaseBlock(ItemStack coinInPedestal)
    {
        CompoundTag tag = new CompoundTag();
        if(coinInPedestal.hasTag())tag = coinInPedestal.getTag();
        ItemStack getBaseBlockList = MowLibCompoundTagUtils.readItemStackFromNBT(MODID, tag, "_baseblockStack");
        if(!getBaseBlockList.isEmpty())
        {
            String entityString = MowLibCompoundTagUtils.readStringFromNBT(MODID, coinInPedestal.getTag(), "_entityString");

            if(entityString != "")
            {
                return true;
            }
        }

        return false;
    }

    public boolean allowEntity(ItemStack coinInPedestal, Entity entityIn)
    {
        ItemStack baseBlock = MowLibCompoundTagUtils.readItemStackFromNBT(MODID, coinInPedestal.getTag(), "_baseBlockStack");
        if(!baseBlock.isEmpty())
        {
            String entityString = MowLibCompoundTagUtils.readStringFromNBT(MODID, coinInPedestal.getTag(), "_entityString");
            if(entityString != "")
            {
                int entityTypeNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coinInPedestal.getTag(), "_entityType");
                boolean isBaby = MowLibCompoundTagUtils.readBooleanFromNBT(MODID, coinInPedestal.getTag(), "_entityIsBaby");
                if(entityTypeNum==0)
                {
                    if(entityIn.getClassification(false).equals(MobCategory.byName(entityString)))
                    {
                        if(isBaby && entityIn instanceof LivingEntity)
                        {
                            LivingEntity entity = ((LivingEntity)entityIn);
                            if (entity.isBaby())
                            {
                                return true;
                            }

                            return false;
                        }

                        return true;
                    }
                }
                else if(entityTypeNum==1)
                {
                    if(!EntityType.byString(entityString).isPresent())
                    {
                        //System.out.println(entityString +" is Not a Valid Entity Type");
                        return false;
                    }
                    else if(entityIn.getType().equals(EntityType.byString(entityString).get()))
                    {
                        if(isBaby && entityIn instanceof LivingEntity)
                        {
                            LivingEntity entity = ((LivingEntity)entityIn);
                            if (entity.isBaby())
                            {
                                return true;
                            }

                            return false;
                        }

                        return true;
                    }
                }

                return false;
            }
        }

        return true;
    }

    protected void addMotion(double speed, Direction enumfacing, Entity entity) {

        switch (enumfacing) {
            case DOWN:
                entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y - speed, entity.getDeltaMovement().z);
                break;
            case UP:
                entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y + speed, entity.getDeltaMovement().z);
                break;
            case NORTH:
                entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, entity.getDeltaMovement().z - speed);
                break;
            case SOUTH:
                entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, entity.getDeltaMovement().z + speed);
                break;
            case WEST:
                entity.setDeltaMovement(entity.getDeltaMovement().x - speed, entity.getDeltaMovement().y, entity.getDeltaMovement().z);
                break;
            case EAST:
                entity.setDeltaMovement(entity.getDeltaMovement().x + speed, entity.getDeltaMovement().y, entity.getDeltaMovement().z);
                break;
        }
    }

    @Override
    public void updateAction(Level level, BasePedestalBlockEntity pedestal) {
        /*int configSpeed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get();
        int speed = configSpeed;
        if(pedestal.hasSpeed())speed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get() - pedestal.getTicksReduced();
        //Make sure speed has at least a value of 1
        if(speed<=0)speed = 1;
        if(level.getGameTime()%speed == 0 )
        {
        }*/
        if(level.getGameTime()%2 == 0)
        {
                fanAction(pedestal, level,pedestal.getPos(),pedestal.getCoinOnPedestal());
        }
    }

    public void fanAction(BasePedestalBlockEntity pedestal, Level level, BlockPos posOfPedestal, ItemStack coinInPedestal) {
        ItemStack workCardItemStack = pedestal.getWorkCardInPedestal();
        if (workCardItemStack.getItem() instanceof WorkCardArea) {
            List<LivingEntity> entities = WorkCardArea.getEntitiesInRangeOfUpgrade(level, LivingEntity.class, workCardItemStack, pedestal);

            if (removeFuelForAction(pedestal, 0, false)) {
                Direction facing = getPedestalFacing(level,posOfPedestal);
                for (LivingEntity entity : entities) {
                    if (entity == null) continue;
                    if (!allowEntity(coinInPedestal, entity)) continue;
                    if (entity instanceof Player player && player.isCrouching()) continue;

                    addMotion((((facing == Direction.UP)?(0.2D):(0.1D)) + (double)(((getSpeedTicksReduced(coinInPedestal)==0)?(1):(getSpeedTicksReduced(coinInPedestal)))/PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get())), facing, entity);
                }
            }
        }
    }

    /*@Override
    public void runClientStuff(BasePedestalBlockEntity pedestal) {
        upgradeAction(pedestal,pedestal.getLevel(),pedestal.getPos(),pedestal.getCoinOnPedestal());
        //addMotion((0.1D * pedestal.getTicksReduced()), getPedestalFacing(pedestal.getLevel(),pedestal.getPos()),pedestal.getLevel().getEntity(MowLibCompoundTagUtils.readIntegerFromNBT(MODID,pedestal.getCoinOnPedestal().getTag(),"_fanEntityId")));
    }*/

    @Override
    public void actionOnAddedToPedestal(Player player, BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnAddedToPedestal(player, pedestal, coinInPedestal);
        Level level = pedestal.getLevel();
        CompoundTag tagCoin = new CompoundTag();
        if(coinInPedestal.hasTag()) { tagCoin = coinInPedestal.getTag(); }
        ItemStack getBaseBlock = getBaseBlock(pedestal);
        BaseBlockEntityFilter filter = getRecipeFilterBlock(level,getBaseBlock);
        tagCoin = MowLibCompoundTagUtils.writeItemStackToNBT(MODID, tagCoin, getBaseBlock, "_baseBlockStack");
        tagCoin = MowLibCompoundTagUtils.writeStringToNBT(MODID, tagCoin, getProcessResultFilterBlock(filter), "_entityString");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(MODID, tagCoin, getProcessResultFilterBlockMobType(filter), "_entityType");
        tagCoin = MowLibCompoundTagUtils.writeBooleanToNBT(MODID, tagCoin, getProcessResultFilterBlockIsBaby(filter), "_entityIsBaby");
        coinInPedestal.setTag(tagCoin);
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        //remove NBT saved on upgrade here
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_baseBlockStack");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_entityString");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_entityType");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_entityIsBaby");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_fanEntityId");
    }

    public ItemStack getBaseBlock(BasePedestalBlockEntity pedestal)
    {
        return new ItemStack(pedestal.getLevel().getBlockState(getPosOfBlockBelow(pedestal.getLevel(),pedestal.getPos(),1)).getBlock().asItem());
    }

    @Override
    public void actionOnNeighborBelowChange(BasePedestalBlockEntity pedestal, BlockPos belowBlock) {
        Level level = pedestal.getLevel();
        CompoundTag tagCoin = new CompoundTag();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        if(coinInPedestal.hasTag()) { tagCoin = coinInPedestal.getTag(); }
        ItemStack getBaseBlock = getBaseBlock(pedestal);
        BaseBlockEntityFilter filter = getRecipeFilterBlock(level,getBaseBlock);
        tagCoin = MowLibCompoundTagUtils.writeItemStackToNBT(MODID, tagCoin, getBaseBlock, "_baseBlockStack");
        tagCoin = MowLibCompoundTagUtils.writeStringToNBT(MODID, tagCoin, getProcessResultFilterBlock(filter), "_entityString");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(MODID, tagCoin, getProcessResultFilterBlockMobType(filter), "_entityType");
        tagCoin = MowLibCompoundTagUtils.writeBooleanToNBT(MODID, tagCoin, getProcessResultFilterBlockIsBaby(filter), "_entityIsBaby");
        coinInPedestal.setTag(tagCoin);
    }
}
