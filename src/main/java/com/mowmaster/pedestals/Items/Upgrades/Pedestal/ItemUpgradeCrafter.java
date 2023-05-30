package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.PedestalUtils.References;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class ItemUpgradeCrafter extends ItemUpgradeBase
{
    public ItemUpgradeCrafter(Properties p_41383_) {
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
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 3; }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,coinInPedestal.getOrCreateTag(),"input");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,coinInPedestal.getOrCreateTag(),"output");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,coinInPedestal.getOrCreateTag(),"hasrecipe");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal);
        if (allPositions.isEmpty()) return;

        modifierAction(level, pedestal, allPositions);
    }

    @Nullable
    protected CraftingRecipe recipeGetCraftingRecipe(BasePedestalBlockEntity pedestal, List<ItemStack> ingredientStacks) {
        Level level = pedestal.getLevel();
        if(level == null) return null;
        if(ingredientStacks.size()<=0)return null;

        CraftingContainer cont = MowLibContainerUtils.getContainerCrafting(1,1);
        if(ingredientStacks.size()>1)cont = MowLibContainerUtils.getContainerCrafting(2,2);
        if(ingredientStacks.size()>4)cont = MowLibContainerUtils.getContainerCrafting(3,3);
        if(ingredientStacks.size()>9)cont = MowLibContainerUtils.getContainerCrafting(4,4);
        if(ingredientStacks.size()>16)cont = MowLibContainerUtils.getContainerCrafting(5,5);
        if(ingredientStacks.size()>25)cont = MowLibContainerUtils.getContainerCrafting(6,6);

        int size = ingredientStacks.size();
        int iteratorSize = (size<cont.getContainerSize())?(cont.getContainerSize()):(size);

        for(int i=0;i<iteratorSize;i++)
        {
            if(i<size)
            {
                if(!ingredientStacks.get(i).isEmpty())
                {
                    cont.setItem(-1,ingredientStacks.get(i));
                }
                else
                {
                    cont.setItem(-1,ItemStack.EMPTY);
                }
            }
            else
            {
                cont.setItem(-1,ItemStack.EMPTY);
            }
        }

        if(cont.getContainerSize()>0)
        {
            RecipeManager recipeManager = level.getRecipeManager();
            Optional<CraftingRecipe> optional = recipeManager.getRecipeFor(RecipeType.CRAFTING,cont,level);
            if (optional.isPresent())
            {
                return optional.orElse(null);
            }
        }

        return null;
    }

    protected Collection<ItemStack> getNormalResults(CraftingRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }


    private List<ItemStack> getResultFromCachedRecipe(ItemStack coin, List<ItemStack> incomingStack)
    {
        List<ItemStack> results = new ArrayList<>();
        List<ItemStack> inputStack = MowLibCompoundTagUtils.readItemStackListFromNBT(References.MODID,coin.getOrCreateTag(),"input");
        if(incomingStack.size()==inputStack.size())
        {
            for(int i=0;i<incomingStack.size();i++)
            {
                if(doItemsMatchWithEmpty(incomingStack.get(i),inputStack.get(i))) { continue; }
                else { return results; }
            }

            return MowLibCompoundTagUtils.readItemStackListFromNBT(References.MODID,coin.getOrCreateTag(),"output");
        }

        return results;
    }

    private void cacheRecipe(BasePedestalBlockEntity pedestal, ItemStack coin, List<ItemStack> input)
    {
        CraftingRecipe recipe = recipeGetCraftingRecipe(pedestal, input);
        if (recipe !=null)
        {
            Collection<ItemStack> stackResults = getNormalResults(recipe);
            if(stackResults.size()>0)
            {
                MowLibCompoundTagUtils.writeItemStackListToNBT(References.MODID,coin.getOrCreateTag(),input,"input");
                List<ItemStack> stackies = new ArrayList<>();
                for(ItemStack stacks: stackResults)
                {
                    stackies.add(stacks);
                }
                MowLibCompoundTagUtils.writeItemStackListToNBT(References.MODID,coin.getOrCreateTag(),stackies,"output");
                MowLibCompoundTagUtils.writeBooleanToNBT(References.MODID,coin.getOrCreateTag(),true,"hasrecipe");
            }
        }
        else
        {
            MowLibCompoundTagUtils.writeBooleanToNBT(References.MODID,coin.getOrCreateTag(),false,"hasrecipe");
        }



    }

    private boolean hasCachedRecipe(ItemStack coin)
    {
        return MowLibCompoundTagUtils.readBooleanFromNBT(References.MODID,coin.getOrCreateTag(),"hasrecipe");
    }



    public void modifierAction(Level level, BasePedestalBlockEntity pedestal, List<BlockPos> listed)
    {
        if(!level.isClientSide())
        {
            boolean fullstop = false;
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            List<ItemStack> ingredientList = new ArrayList<>();
            for(int i=0; i<listed.size(); i++)
            {
                BlockEntity invToCheck = level.getBlockEntity(listed.get(i));
                if(invToCheck ==null)
                {
                    fullstop = true;
                    break;
                }
                else if(invToCheck instanceof BasePedestalBlockEntity pedestalToCheck) {
                    ingredientList.add(pedestalToCheck.getItemInPedestal());
                }
            }

            if(!fullstop)
            {
                if(ingredientList.size()>0)
                {
                    if(hasCachedRecipe(coinInPedestal))
                    {

                        List<ItemStack> stackResult = getResultFromCachedRecipe(coinInPedestal,ingredientList);
                        ItemStack result = stackResult.stream().findFirst().orElse(ItemStack.EMPTY);
                        if(!result.isEmpty())
                        {
                            if(pedestal.addItem(result,true))
                            {
                                for(int j=0; j<listed.size(); j++)
                                {
                                    BlockEntity invToCheck = level.getBlockEntity(listed.get(j));
                                    if(invToCheck ==null)
                                    {
                                        fullstop = true;
                                        break;
                                    }
                                    else if(invToCheck instanceof BasePedestalBlockEntity pedestalToCheck) {

                                        ItemStack pedestalToCheckStack = pedestalToCheck.getItemInPedestal().copy();
                                        if(!pedestalToCheck.removeItem(1,true).isEmpty())
                                        {
                                            pedestalToCheck.removeItem(1,false);
                                            if(pedestalToCheckStack.hasCraftingRemainingItem())pedestal.addItem(pedestalToCheckStack.getCraftingRemainingItem(),false);
                                            BlockPos pedestalToCheckPoint = getPosOfBlockBelow(level,pedestalToCheck.getPos(),-1);
                                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestalToCheck.getLevel(),pedestalToCheck.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalToCheckPoint.getX(),pedestalToCheckPoint.getY(),pedestalToCheckPoint.getZ(),50,200,0));
                                        }
                                    }
                                }

                                if(!fullstop)
                                {
                                    for(ItemStack stacks:stackResult)
                                    {
                                        if(pedestal.addItem(stacks,true))
                                        {
                                            pedestal.addItem(stacks,false);
                                        }
                                        else
                                        {
                                            BlockPos pedestalPos = getPosOfBlockBelow(level,pedestal.getPos(),-1);
                                            ItemStack dropMe = stacks;
                                            ItemEntity itementity = new ItemEntity(level, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), dropMe);
                                            itementity.setDefaultPickUpDelay();
                                            itementity.setDeltaMovement(0.0,0.0,0.0);
                                            itementity.moveTo(Vec3.atCenterOf(pedestalPos));
                                            level.addFreshEntity(itementity);
                                        }
                                    }

                                    BlockPos pedestalToCheckPoint = getPosOfBlockBelow(level,pedestal.getPos(),-1);
                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalToCheckPoint.getX(),pedestalToCheckPoint.getY(),pedestalToCheckPoint.getZ(),0,255,0));
                                }
                            }
                            else
                            {
                                BlockPos pedestalToCheckPoint = getPosOfBlockBelow(level,pedestal.getPos(),-1);
                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalToCheckPoint.getX(),pedestalToCheckPoint.getY(),pedestalToCheckPoint.getZ(),50,50,50));
                            }
                        }
                        else
                        {
                            cacheRecipe(pedestal,coinInPedestal,ingredientList);
                        }
                    }
                    else
                    {
                        cacheRecipe(pedestal,coinInPedestal,ingredientList);
                    }
                }
            }
        }
    }
}
