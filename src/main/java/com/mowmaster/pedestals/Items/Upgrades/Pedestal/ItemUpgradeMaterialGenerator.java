package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.PedestalUtils.UpgradeUtils;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class ItemUpgradeMaterialGenerator extends ItemUpgradeBase {

    public ItemUpgradeMaterialGenerator(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyItemCapacity(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public int getUpgradeWorkRange(ItemStack coinUpgrade) { return 0; }

    @Override
    public ItemStack getUpgradeDefaultTool() {
        return new ItemStack(Items.STONE_PICKAXE);
    }

    public int getCobbleGenSpawnRate(ItemStack upgradeStack)
    {
        if(getItemCapacityIncrease(upgradeStack)>0)
        {
            return getItemCapacityIncrease(upgradeStack);
        }

        return 1;
    }

    @Nullable
    public CobbleGenRecipe getRecipe(Level level, ItemStack stackIn) {
        Container cont = MowLibContainerUtils.getContainer(1);
        cont.setItem(-1,stackIn);
        List<CobbleGenRecipe> recipes = level.getRecipeManager().getRecipesFor(CobbleGenRecipe.Type.INSTANCE,cont,level);
        return recipes.size() > 0 ? level.getRecipeManager().getRecipesFor(CobbleGenRecipe.Type.INSTANCE,cont,level).get(0) : null;
    }

    protected Collection<ItemStack> getGeneratedItem(CobbleGenRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }

    protected int getEnergyRequiredForGeneration(CobbleGenRecipe recipe) {
        return (recipe == null)?(0):(recipe.getResultEnergyNeeded());
    }

    protected int getExperienceRequiredForGeneration(CobbleGenRecipe recipe) {
        return (recipe == null)?(0):(recipe.getResultExperienceNeeded());
    }

    protected FluidStack getFluidRequiredForGeneration(CobbleGenRecipe recipe) {
        return (recipe == null)?(FluidStack.EMPTY):(recipe.getResultFluidNeeded());
    }

    public List<ItemStack> getItemToGenerate(BasePedestalBlockEntity pedestal, CobbleGenRecipe recipe)
    {
        ItemStack getInitialGeneratedItem = ItemStack.EMPTY;
        Level level = pedestal.getLevel();
        //ItemStack itemBlockBelow = new ItemStack(pedestal.getLevel().getBlockState(belowBlock).getBlock().asItem());
        if(recipe == null)return new ArrayList<>();
        getInitialGeneratedItem = getGeneratedItem(recipe).stream().findFirst().get();

        Block generatedBlock = Block.byItem(getInitialGeneratedItem.getItem());
        if(generatedBlock != Blocks.AIR)
        {
            ItemStack getToolFromPedestal = (pedestal.getToolStack().isEmpty())?(getUpgradeDefaultTool()):(pedestal.getToolStack());

            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                        .withRandom(level.random)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                        .withParameter(LootContextParams.THIS_ENTITY, getPlayer.get())
                        .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                return generatedBlock.defaultBlockState().getBlock().getDrops(generatedBlock.defaultBlockState(),builder);
            }
            else
            {
                LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                        .withRandom(level.random)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                        .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                return generatedBlock.defaultBlockState().getBlock().getDrops(generatedBlock.defaultBlockState(),builder);
            }
        }

        return new ArrayList<>();
    }

    public ItemStack getItemStackToGenerate(BasePedestalBlockEntity pedestal, CobbleGenRecipe recipe)
    {
        ItemStack getInitialGeneratedItem = ItemStack.EMPTY;
        Level level = pedestal.getLevel();
        //ItemStack itemBlockBelow = new ItemStack(pedestal.getLevel().getBlockState(belowBlock).getBlock().asItem());
        if(recipe == null)return ItemStack.EMPTY;
        getInitialGeneratedItem = getGeneratedItem(recipe).stream().findFirst().get();

        return getInitialGeneratedItem;
    }

    public List<ItemStack> getItemsToGenerate(BasePedestalBlockEntity pedestal, ItemStack blockToBreak)
    {
        Level level = pedestal.getLevel();
        Block generatedBlock = Block.byItem(blockToBreak.getItem());
        if(generatedBlock != Blocks.AIR)
        {
            ItemStack getToolFromPedestal = (pedestal.getToolStack().isEmpty())?(new ItemStack(Items.STONE_PICKAXE)):(pedestal.getToolStack());

            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                        .withRandom(level.random)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, getPlayer.get())
                        .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                return generatedBlock.defaultBlockState().getDrops(builder);
            }
            else
            {
                LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                        .withRandom(level.random)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                        .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                return generatedBlock.defaultBlockState().getDrops(builder);
            }
        }

        return new ArrayList<>();
    }

    /*@Override
    public int getComparatorRedstoneLevel(Level worldIn, BlockPos pos) {
        int hasItem=0;
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if(blockEntity instanceof BasePedestalBlockEntity pedestal) {
            List<ItemStack> itemstacks = pedestal.getItemStacks();
            if(itemstacks.size()>0)
            {
                int maxStackSizeDefault = 64;
                if(pedestal.hasFilter())
                {
                    IPedestalFilter filter =pedestal.getIPedestalFilter();
                    if(filter != null && filter.getFilterDirection().insert())
                    {
                        maxStackSizeDefault = Math.max(1,filter.canAcceptCountItems(pedestal,pedestal.getFilterInPedestal(), new ItemStack(Items.STONE,64).getMaxStackSize(), pedestal.getSlotSizeLimit(), new ItemStack(Items.STONE,64)));
                    }
                }
                int counter = 0;
                int maxStorageCount = Math.max(1,(pedestal.getPedestalSlots()-1)) * maxStackSizeDefault;
                for (ItemStack stack : itemstacks)
                {
                    //adjust max storage possible based on itemstacks present
                    if(stack.getMaxStackSize()<maxStackSizeDefault)
                    {
                        maxStorageCount-=maxStackSizeDefault;
                        maxStorageCount+=stack.getMaxStackSize();
                    }

                    counter+=stack.getCount();
                }
                float f = (float)counter/(float)maxStorageCount;
                hasItem = (int)Math.floor(f*15.0F);
            }
            else
            {
                float f = (float) pedestal.getItemInPedestal().getCount() / (float) pedestal.getItemInPedestal().getMaxStackSize();
                return (int) Math.floor(f * 15.0F);
            }
        }

        return hasItem;
    }*/

    //To save a recipe and verify if it hasnt changed so we dont have to keep pulling it every time
    //https://github.com/oierbravo/createsifter/blob/mc1.19/dev/src/main/java/com/oierbravo/createsifter/content/contraptions/components/sifter/SifterTileEntity.java
    @Override
    public void actionOnNeighborBelowChange(BasePedestalBlockEntity pedestal, BlockPos belowBlock) {

        CobbleGenRecipe recipe = getRecipe(pedestal.getLevel(),new ItemStack(pedestal.getLevel().getBlockState(belowBlock).getBlock().asItem()));
        ItemStack getOutput = getItemStackToGenerate(pedestal,recipe);
        List<ItemStack> getStackList = new ArrayList<>();
        getStackList.add(getOutput);
        int getEnergyNeeded = getEnergyRequiredForGeneration(recipe);
        int getExperienceNeeded = getExperienceRequiredForGeneration(recipe);
        FluidStack getFluidStackNeeded = getFluidRequiredForGeneration(recipe);
        CompoundTag tagCoin = new CompoundTag();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        if(coinInPedestal.hasTag()) { tagCoin = coinInPedestal.getTag(); }
        tagCoin = MowLibCompoundTagUtils.writeItemStackListToNBT(References.MODID, tagCoin, getStackList,"_stackList");
        tagCoin = MowLibCompoundTagUtils.writeFluidStackToNBT(References.MODID, tagCoin, getFluidStackNeeded,"_fluidStack");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID, tagCoin,getEnergyNeeded, "_energyNeeded");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID, tagCoin,getExperienceNeeded, "_xpNeeded");

        coinInPedestal.setTag(tagCoin);
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        //remove NBT saved on upgrade here
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(),"_stackList");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(),"_fluidStack");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(), "_energyNeeded");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(), "_xpNeeded");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        BlockPos posBelow = getPosOfBlockBelow(level,pedestalPos,1);

        int modifier = getCobbleGenSpawnRate(coin);

        //if itemstacklist is null, populate nbt. then rely on block below updates to modify things.
        if(MowLibCompoundTagUtils.readItemStackListFromNBT(References.MODID,coin.getTag(),"_stackList") == null)actionOnNeighborBelowChange(pedestal, posBelow);

        ItemStack getCobbleGenOutput = MowLibCompoundTagUtils.readItemStackListFromNBT(References.MODID,coin.getTag(),"_stackList").get(0);
        if(!getCobbleGenOutput.isEmpty())
        {
            List<ItemStack> getCobbleGenOutputs = getItemsToGenerate(pedestal,getCobbleGenOutput);
            FluidStack getFluidStackNeeded = MowLibCompoundTagUtils.readFluidStackFromNBT(References.MODID,coin.getTag(),"_fluidStack");
            int getEnergyNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_energyNeeded");
            int getExperienceNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_xpNeeded");

            boolean fluid = false;
            boolean energy = false;
            boolean xp = false;
            boolean damage = false;

            if(!getFluidStackNeeded.isEmpty())
            {
                if(!pedestal.removeFluid(getFluidStackNeeded, IFluidHandler.FluidAction.SIMULATE).isEmpty())
                {
                    fluid = true;
                }
                else return;
            }

            if(getEnergyNeeded>0)
            {
                if(pedestal.removeEnergy(getEnergyNeeded, true)>0)
                {
                    energy = true;
                }
                else return;
            }

            if(getExperienceNeeded>0)
            {
                if(pedestal.removeExperience(getExperienceNeeded, true)>0)
                {
                    xp = true;
                }
                else return;
            }

            if(PedestalConfig.COMMON.cobbleGeneratorDamageTools.get())
            {
                if(pedestal.hasTool())
                {
                    if(pedestal.getDurabilityRemainingOnInsertedTool()>0)
                    {
                        if(pedestal.damageInsertedTool(1,true))
                        {
                            damage = true;
                        }
                        else
                        {
                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                            return;
                        }
                    }
                    else
                    {
                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                        return;
                    }
                }
            }

            if(getCobbleGenOutputs.size()<=0){return;}
            else if(getCobbleGenOutputs.size()>0)
            {
                boolean itemsInserted = false;

                for (int i=0; i < getCobbleGenOutputs.size(); i++)
                {
                    if(getCobbleGenOutputs.get(i).isEmpty()) continue;

                    ItemStack stacked = getCobbleGenOutputs.get(i);
                    boolean hadSpacePreModifier = pedestal.hasSpaceForItem(stacked);
                    ItemStack stackedCopy = stacked.copy();
                    ItemStack testStack = pedestal.addItemStack(stackedCopy, true);
                    if(modifier>1) { stackedCopy.shrink(-modifier); }
                    if(testStack.isEmpty())
                    {
                        pedestal.addItem(stackedCopy, false);
                        itemsInserted = true;
                    }
                    else if(testStack.getCount()>0)
                    {
                        stackedCopy.shrink(testStack.getCount());
                        if(pedestal.addItem(stackedCopy, true))
                        {
                            pedestal.addItem(stackedCopy, false);
                            itemsInserted = true;
                        }
                    }
                }

                if(itemsInserted)
                {
                    if(fluid)pedestal.removeFluid(getFluidStackNeeded, IFluidHandler.FluidAction.EXECUTE);
                    if(energy)pedestal.removeEnergy(getEnergyNeeded, false);
                    if(xp)pedestal.removeExperience(getExperienceNeeded, false);
                    if(damage)pedestal.damageInsertedTool(1,false);
                }
            }
        }
        else
        {
            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY(),pedestalPos.getZ(),50,50,50));
        }
    }

}
