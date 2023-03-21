package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.PedestalUtils.References;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeSmoker extends ItemUpgradeBase
{
    public ItemUpgradeSmoker(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public int baseEnergyCostPerDistance() {
        return baseEnergyCost();
    }

    public int baseEnergyCost(){ return PedestalConfig.COMMON.upgrade_smoker_baseEnergyCost.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {
        List<String> messages = new ArrayList<>();

        if(pedestal.getItemInPedestal().isEmpty())
        {
            if(baseEnergyCost()>0)
            {
                boolean hasItem = MowLibCompoundTagUtils.readBooleanFromNBT(MODID,pedestal.getCoinOnPedestal().getOrCreateTag(),"hasitem");
                if(pedestal.getStoredEnergy()<baseEnergyCost() && hasItem)
                {
                    messages.add(ChatFormatting.RED + "Needs Energy");
                }
            }
        }

        return messages;
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        //remove NBT saved on upgrade here
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,coinInPedestal.getOrCreateTag(),"input");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,coinInPedestal.getOrCreateTag(),"output");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,coinInPedestal.getOrCreateTag(),"cooktime");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,coinInPedestal.getOrCreateTag(),"xpamount");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,coinInPedestal.getOrCreateTag(),"_numdelay");
    }

    @Nullable
    protected SmokingRecipe getNormalRecipe(BasePedestalBlockEntity pedestal, ItemStack stackIn) {
        Level level = pedestal.getLevel();
        Container cont = MowLibContainerUtils.getContainer(1);
        cont.setItem(-1,stackIn);

        if (level == null) return null;
        RecipeManager recipeManager = level.getRecipeManager();
        Optional<SmokingRecipe> optional = recipeManager.getRecipeFor(RecipeType.SMOKING, cont, level);
        if (optional.isPresent())
        {
            return optional.orElse(null);
        }

        return null;
    }

    protected Collection<ItemStack> getNormalResults(SmokingRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }

    protected int getCookTime(SmokingRecipe recipe) {
        return (recipe == null)?(0):(recipe.getCookingTime());
    }

    protected int getExperienceResult(SmokingRecipe recipe) {
        float returner = (recipe == null)?(0):(recipe.getExperience());
        return Math.round(returner);
    }

    private ItemStack getResultFromCachedRecipe(ItemStack coin, ItemStack input)
    {
        ItemStack inputStack = MowLibCompoundTagUtils.readItemStackFromNBT(References.MODID,coin.getOrCreateTag(),"input");
        if(ItemHandlerHelper.canItemStacksStack(inputStack,input))
        {
            return MowLibCompoundTagUtils.readItemStackFromNBT(References.MODID,coin.getOrCreateTag(),"output");
        }
        return ItemStack.EMPTY;
    }

    private int getCookTimeFromCachedRecipe(ItemStack coin, ItemStack input)
    {
        ItemStack inputStack = MowLibCompoundTagUtils.readItemStackFromNBT(References.MODID,coin.getOrCreateTag(),"input");
        if(ItemHandlerHelper.canItemStacksStack(inputStack,input))
        {
            return MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getOrCreateTag(),"cooktime");
        }
        return -1;
    }

    private int getXpFromCachedRecipe(ItemStack coin, ItemStack input)
    {
        ItemStack inputStack = MowLibCompoundTagUtils.readItemStackFromNBT(References.MODID,coin.getOrCreateTag(),"input");
        if(ItemHandlerHelper.canItemStacksStack(inputStack,input))
        {
            return MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getOrCreateTag(),"xpamount");
        }
        return 0;
    }

    private void cacheRecipe(BasePedestalBlockEntity pedestal, ItemStack coin, ItemStack input)
    {
        SmokingRecipe recipe = getNormalRecipe(pedestal, input);
        if(recipe != null)
        {
            ItemStack result = getNormalResults(recipe).stream().findFirst().orElse(ItemStack.EMPTY);
            if(!result.isEmpty())
            {
                MowLibCompoundTagUtils.writeItemStackToNBT(References.MODID,coin.getOrCreateTag(),input,"input");
                MowLibCompoundTagUtils.writeItemStackToNBT(References.MODID,coin.getOrCreateTag(),result,"output");
                MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID,coin.getOrCreateTag(),getCookTime(recipe),"cooktime");
                MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID,coin.getOrCreateTag(),getExperienceResult(recipe),"xpamount");
                MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID,coin.getOrCreateTag(),getExperienceResult(recipe),"hasitem");
            }
        }
    }

    private boolean hasCachedRecipe(ItemStack coin)
    {
        ItemStack inputStack = MowLibCompoundTagUtils.readItemStackFromNBT(References.MODID,coin.getOrCreateTag(),"input");
        return !inputStack.isEmpty();
    }

    private int getCookTime(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getOrCreateTag(), "_numdelay");
    }

    private void resetCookTime(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), 0, "_numdelay");
    }

    private void incrementCookingTime(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int current = getCookTime(pedestal);
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), (current+1+getSpeedTicksReduced(pedestal.getCoinOnPedestal())), "_numdelay");
    }

    @Override
    public void updateAction(Level level, BasePedestalBlockEntity pedestal)
    {
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack coin = pedestal.getCoinOnPedestal();

        BlockPos posInventory = getPosOfBlockBelow(level,pedestalPos,1);
        ItemStack itemFromInv = ItemStack.EMPTY;
        LazyOptional<IItemHandler> cap = MowLibItemUtils.findItemHandlerAtPos(level,posInventory,getPedestalFacing(level, pedestalPos),true);
        if(!isInventoryEmpty(cap)) {
            if (cap.isPresent()) {
                IItemHandler handler = cap.orElse(null);
                BlockEntity invToPullFrom = level.getBlockEntity(posInventory);
                if(invToPullFrom instanceof BasePedestalBlockEntity) {
                    itemFromInv = ItemStack.EMPTY;

                }
                else {
                    if (handler != null) {
                        //Items
                        //Handle last, just incase any items match the other options
                        int i = getNextSlotWithItemsCapFilteredMachine(pedestal,cap);
                        if(i>=0)
                        {
                            itemFromInv = handler.getStackInSlot(i);
                            ItemStack copyIncoming = itemFromInv.copy();
                            copyIncoming.setCount(1);
                            if(itemFromInv != null && !itemFromInv.isEmpty())
                            {
                                if(!MowLibCompoundTagUtils.readBooleanFromNBT(MODID,pedestal.getCoinOnPedestal().getOrCreateTag(),"hasitem"))
                                {
                                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,pedestal.getCoinOnPedestal().getOrCreateTag(),true,"hasitem");
                                    pedestal.update();
                                }

                                if(hasCachedRecipe(coin))
                                {
                                    int cooktime = getCookTimeFromCachedRecipe(coin,copyIncoming);
                                    int xp = getXpFromCachedRecipe(coin,copyIncoming);
                                    ItemStack result = getResultFromCachedRecipe(coin,copyIncoming);

                                    int currentCookTime = getCookTime(pedestal);
                                    if(currentCookTime>=cooktime)
                                    {
                                        resetCookTime(pedestal);
                                        if(!result.isEmpty())
                                        {
                                            if(!handler.extractItem(i,1 ,true ).isEmpty() && pedestal.addItemStack(result, true).isEmpty())
                                            {
                                                handler.extractItem(i,1 ,false );
                                                pedestal.addExperience(xp,false);
                                                pedestal.addItemStack(result, false);
                                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+0.75D,pedestalPos.getZ(),255,150,0));
                                            }
                                        }
                                    }
                                    else
                                    {
                                        int cost = baseEnergyCost();
                                        if(pedestal.removeEnergy(cost,true)>=cost)
                                        {
                                            pedestal.removeEnergy(cost,false);
                                            incrementCookingTime(pedestal);
                                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+0.75D,pedestalPos.getZ(),50,50,50));
                                        }
                                    }
                                }
                                else
                                {
                                    cacheRecipe(pedestal,coin,copyIncoming);
                                }
                            }
                            else
                            {
                                if(MowLibCompoundTagUtils.readBooleanFromNBT(MODID,pedestal.getCoinOnPedestal().getOrCreateTag(),"hasitem"))
                                {
                                    MowLibCompoundTagUtils.writeBooleanToNBT(MODID,pedestal.getCoinOnPedestal().getOrCreateTag(),false,"hasitem");
                                    pedestal.update();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
