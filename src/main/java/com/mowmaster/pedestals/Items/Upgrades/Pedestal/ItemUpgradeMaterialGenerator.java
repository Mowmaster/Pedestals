package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

    public int getCobbleGenSpawnRate(ItemStack upgradeStack) {
        return Math.max(getItemCapacityIncrease(upgradeStack), 1);
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

    protected DustMagic getDustRequiredForGeneration(CobbleGenRecipe recipe) {
        return (recipe == null)?(DustMagic.EMPTY):(recipe.getResultDustNeeded());
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

    public List<ItemStack> getItemsToGenerate(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack blockToBreak) {
        Block generatedBlock = Block.byItem(blockToBreak.getItem());
        if(generatedBlock != Blocks.AIR) {
            ItemStack toolStack = pedestal.getToolStack();

            LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                    .withRandom(level.random)
                    .withParameter(LootContextParams.ORIGIN, new Vec3(pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ()))
                    .withParameter(LootContextParams.TOOL, toolStack);

            WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
            if (fakePlayerReference != null && fakePlayerReference.get() != null) {
                builder.withOptionalParameter(LootContextParams.THIS_ENTITY, fakePlayerReference.get());

            }
            return generatedBlock.defaultBlockState().getDrops(builder);

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
        DustMagic getDustNeeded = getDustRequiredForGeneration(recipe);
        CompoundTag tagCoin = new CompoundTag();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        if(coinInPedestal.hasTag()) { tagCoin = coinInPedestal.getTag(); }
        tagCoin = MowLibCompoundTagUtils.writeItemStackListToNBT(References.MODID, tagCoin, getStackList,"_stackList");
        tagCoin = MowLibCompoundTagUtils.writeFluidStackToNBT(References.MODID, tagCoin, getFluidStackNeeded,"_fluidStack");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID, tagCoin,getEnergyNeeded, "_energyNeeded");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID, tagCoin,getExperienceNeeded, "_xpNeeded");
        DustMagic.setDustMagicInTag(coinInPedestal.getTag(),getDustNeeded);

        coinInPedestal.setTag(tagCoin);
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        //remove NBT saved on upgrade here
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(),"_stackList");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(),"_fluidStack");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(), "_energyNeeded");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(), "_xpNeeded");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MowLibReferences.MODID, coinInPedestal.getTag(), "_dustMagicColor");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MowLibReferences.MODID, coinInPedestal.getTag(), "_dustMagicAmount");
    }

    private boolean needsFuel(FluidStack getFluidStackNeeded, int getEnergyNeeded, int getExperienceNeeded, DustMagic getDustNeeded)
    {
        if(!getFluidStackNeeded.isEmpty())return true;
        if(getEnergyNeeded>0)return true;
        if(getExperienceNeeded>0)return true;
        if(!getDustNeeded.isEmpty())return true;
        return false;
    }

    private boolean hasEnoughFuel(BasePedestalBlockEntity pedestal, FluidStack getFluidStackNeeded, int getEnergyNeeded, int getExperienceNeeded, DustMagic getDustNeeded,boolean toolDamage, int modifier)
    {
        FluidStack pedestalsFluid = pedestal.getStoredFluid();
        int pedestalEnergy = pedestal.getStoredEnergy();
        int pedestalExperience = pedestal.getStoredExperience();
        DustMagic pedestalDust = pedestal.getStoredDust();

        if(!getFluidStackNeeded.isEmpty())
        {
            int increase = getFluidStackNeeded.getAmount()*modifier;
            if(pedestalsFluid.getAmount()<increase)
            {
                return false;
            }
        }
        if(getEnergyNeeded>0)
        {
            int increase = pedestalEnergy*modifier;
            if(pedestalEnergy<increase)
            {
                return false;
            }
        }
        if(getExperienceNeeded>0)
        {
            int increase = pedestalExperience*modifier;
            if(pedestalExperience<increase)
            {
                return false;
            }
        }
        if(!getDustNeeded.isEmpty())
        {
            int increase = pedestalDust.getDustAmount()*modifier;
            if(pedestalDust.getDustAmount()<increase)
            {
                return false;
            }
        }
        if(toolDamage)
        {
            int increase = modifier;
            if(!pedestal.damageInsertedTool(increase,true))
            {
                return false;
            }
        }

        return true;
    }

    private boolean removeFuel(BasePedestalBlockEntity pedestal, FluidStack getFluidStackNeeded, int getEnergyNeeded, int getExperienceNeeded, DustMagic getDustNeeded,boolean toolDamage, int modifier)
    {
        boolean returner = false;
        boolean needsFuel = needsFuel(getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded);
        FluidStack neededFluid = getFluidStackNeeded.copy();
        int neededEnergy = getEnergyNeeded;
        int neededExperience = getExperienceNeeded;
        DustMagic neededDust = getDustNeeded.copy();

        //Should be enough to trust that we can just check and remove
        if(needsFuel)
        {
            if(hasEnoughFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,toolDamage,modifier))
            {
                if(!neededFluid.isEmpty())
                {
                    int increase = neededFluid.getAmount()*modifier;
                    neededFluid.setAmount(increase);
                    if(pedestal.removeFluid(neededFluid, IFluidHandler.FluidAction.SIMULATE).equals(neededFluid))
                    {
                        returner = true;
                        pedestal.removeFluid(neededFluid, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
                if(neededEnergy>0)
                {
                    int increase = neededEnergy*modifier;
                    if(pedestal.removeEnergy(increase, true)>0)
                    {
                        returner = true;
                        pedestal.removeEnergy(increase, false);
                    }
                }
                if(neededExperience>0)
                {
                    int increase = neededExperience*modifier;
                    if(pedestal.removeExperience(increase, true)>0)
                    {
                        returner = true;
                        pedestal.removeExperience(increase, false);
                    }
                }
                if(!neededDust.isEmpty())
                {
                    int increase = neededDust.getDustAmount()*modifier;
                    neededDust.setDustAmount(increase);
                    if(pedestal.removeDust(neededDust, IDustHandler.DustAction.SIMULATE).equals(neededDust))
                    {
                        returner = true;
                        pedestal.removeDust(neededDust, IDustHandler.DustAction.EXECUTE);
                    }
                }
                if(toolDamage)
                {
                    int increase = modifier;
                    pedestal.damageInsertedTool(increase,false);
                    returner = true;
                }
            }
        }
        else
        {
            returner = true;
        }

        return returner;
    }

    private ItemStack getGeneratorRecipeResult(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack upgrade) {
        BlockPos posBelow = getPosOfBlockBelow(level, pedestalPos, 1);
        List<ItemStack> recipeOutput = MowLibCompoundTagUtils.readItemStackListFromNBT(References.MODID, upgrade.getTag(), "_stackList");
        if (recipeOutput == null) { // cache the result using the neighbor below change code (which we rely on for future updates)
            actionOnNeighborBelowChange(pedestal, posBelow);
            recipeOutput = MowLibCompoundTagUtils.readItemStackListFromNBT(References.MODID, upgrade.getTag(), "_stackList");
        }
        return recipeOutput.get(0); // TODO: stop storing this as a List<ItemStack>.
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        ItemStack recipeResult = getGeneratorRecipeResult(level, pedestal, pedestalPos, coin);
        if (recipeResult.isEmpty()) {
            if (pedestal.canSpawnParticles()) {
                MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), 50, 50, 50));
            }
            return;
        }

        List<ItemStack> getCobbleGenOutputs = getItemsToGenerate(level, pedestal, pedestalPos, recipeResult);
        if (getCobbleGenOutputs.isEmpty()) {
            return;
        }

        FluidStack getFluidStackNeeded = MowLibCompoundTagUtils.readFluidStackFromNBT(References.MODID,coin.getTag(),"_fluidStack");
        int getEnergyNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_energyNeeded");
        int getExperienceNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_xpNeeded");
        DustMagic getDustNeeded = DustMagic.getDustMagicInTag(coin.getTag());
        boolean needsFuel = needsFuel(getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded);
        boolean damage = false;
        int modifier = getCobbleGenSpawnRate(coin);

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

        for (int i=0; i < getCobbleGenOutputs.size(); i++)
        {
            if(getCobbleGenOutputs.get(i).isEmpty()) continue;
            ItemStack stacked = getCobbleGenOutputs.get(i);
            boolean hadSpacePreModifier = pedestal.hasSpaceForItem(stacked);
            if(hadSpacePreModifier)
            {
                ItemStack stackedCopy = stacked.copy();
                int modifierAmount = stacked.getCount() * modifier;
                if(modifier>1) { stackedCopy.setCount(modifierAmount); }
                ItemStack testStack = pedestal.addItemStack(stackedCopy, true);
                if(testStack.isEmpty())
                {
                    if(needsFuel)
                    {
                        if(hasEnoughFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,damage,modifier))
                        {
                            if(removeFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,damage,modifier))
                            {
                                pedestal.addItem(stackedCopy, false);
                            }
                        }
                    }
                    else
                    {
                        pedestal.addItem(stackedCopy, false);
                    }
                }
                else if(testStack.getCount()>0)
                {
                    stackedCopy.shrink(testStack.getCount());
                    if(needsFuel)
                    {
                        if(hasEnoughFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,damage,modifier))
                        {
                            if(removeFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,damage,modifier))
                            {
                                pedestal.addItem(stackedCopy, false);
                            }
                        }
                    }
                    else
                    {
                        pedestal.addItem(stackedCopy, false);
                    }
                }
            }
        }
    }

}
