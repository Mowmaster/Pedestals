package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeExpEnchanter extends ItemUpgradeBaseExp
{
    public ItemUpgradeExpEnchanter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    @Override
    public int getExpBuffer(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = (capacityOver*15)+30;
        return  (overEnchanted>maxLVLStored)?(maxLVLStored):(overEnchanted);
    }

    public float getEnchantmentPowerFromSorroundings(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        float enchantPower = 0;

        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (i > -2 && i < 2 && j == -1) {
                    j = 2;
                }
                for (int k = 0; k <= 2; ++k) {
                    BlockPos blockpos = posOfPedestal.add(i, k, j);
                    BlockState blockNearBy = world.getBlockState(blockpos);
                    if (blockNearBy.getBlock().getEnchantPowerBonus(blockNearBy, world, blockpos)>0)
                    {
                        enchantPower +=blockNearBy.getBlock().getEnchantPowerBonus(blockNearBy, world, blockpos);
                    }
                }
            }
        }

        return enchantPower;
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
                    upgradeAction(world, itemInPedestal, coinInPedestal, pedestalPos);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int getMaxXpValue = getExpCountByLevel(getExpBuffer(coinInPedestal));
        if(!hasMaxXpSet(coinInPedestal) || readMaxXpFromNBT(coinInPedestal) != getMaxXpValue) {setMaxXP(coinInPedestal, getMaxXpValue);}
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        ItemStack itemFromInv = ItemStack.EMPTY;

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(hasAdvancedInventoryTargeting(coinInPedestal))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(!isInventoryEmpty(cap))
        {
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                if (((hasAdvancedInventoryTargeting(coinInPedestal) && invToPullFrom instanceof PedestalTileEntity)||!(invToPullFrom instanceof PedestalTileEntity))?(false):(true)) {
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
                            TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                            if(pedestalInv instanceof PedestalTileEntity) {
                                if(!((PedestalTileEntity) pedestalInv).hasItem())
                                {
                                    if(itemFromInv.isEnchantable() || itemFromInv.getItem().equals(Items.BOOK))
                                    {
                                        //This is Book Shelf Enchanting level, not enchantment level (15 bookshelfves = 30 levels of enchantability)
                                        float level = getEnchantmentPowerFromSorroundings(world,posOfPedestal,coinInPedestal);
                                        //Need to charge at min 1 level for an enchant
                                        int actualEnchantingLevel = ((level * 2)<1)?(1):((int)(level * 2));
                                        int currentlyStoredExp = getXPStored(coinInPedestal);
                                        int currentLevelFromStoredXp = getExpLevelFromCount(currentlyStoredExp);
                                        int xpLevelsNeeded = (actualEnchantingLevel/10);
                                        int xpAtEnchantingLevel = getExpCountByLevel(actualEnchantingLevel);
                                        //since this is the number we subtract, if we need at least 1 level then make this 0
                                        int xpAtLevelsBelowRequired = getExpCountByLevel(((actualEnchantingLevel-xpLevelsNeeded)<1)?(0):((actualEnchantingLevel-xpLevelsNeeded)));
                                        int expNeeded = (xpAtEnchantingLevel-xpAtLevelsBelowRequired<7)?(7):(xpAtEnchantingLevel-xpAtLevelsBelowRequired);
                                        if(currentlyStoredExp >= expNeeded && currentLevelFromStoredXp >= actualEnchantingLevel)
                                        {
                                            //Enchanting Code Here
                                            Random rand = new Random();
                                            ItemStack itemToEnchant = itemFromInv.copy();
                                            itemToEnchant.setCount(1);
                                            //the boolean at the end controls if treasure enchants are allowed.EnchantmentHelper.
                                            ItemStack stackToReturn = EnchantmentHelper.addRandomEnchantment(rand,itemToEnchant ,actualEnchantingLevel ,true );
                                            if(!stackToReturn.isEmpty() && stackToReturn.isEnchanted() || stackToReturn.getItem().equals(Items.ENCHANTED_BOOK))
                                            {
                                                int getExpLeftInPedestal = currentlyStoredExp - expNeeded;
                                                setXPStored(coinInPedestal,getExpLeftInPedestal);
                                                handler.extractItem(i,stackToReturn.getCount() ,false );
                                                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.35F, 1.0F);
                                                ((PedestalTileEntity) pedestalInv).addItem(stackToReturn);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        ItemStack toReturn = itemFromInv.copy();
                                        handler.extractItem(i,toReturn.getCount() ,false );
                                        ((PedestalTileEntity) pedestalInv).addItem(toReturn);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*public static ItemStack addRandomEnchantment(Random p_77504_0_, ItemStack p_77504_1_, int p_77504_2_, boolean p_77504_3_) {
        List list = buildEnchantmentList(p_77504_0_, p_77504_1_, p_77504_2_, p_77504_3_);
        System.out.println(list.size());
        boolean flag = p_77504_1_.getItem() == Items.BOOK;
        if(flag) {
            p_77504_1_ = new ItemStack(Items.ENCHANTED_BOOK);
        }

        Iterator var6 = list.iterator();

        while(var6.hasNext()) {
            EnchantmentData enchantmentdata = (EnchantmentData)var6.next();
            if(flag) {
                EnchantedBookItem.addEnchantment(p_77504_1_, enchantmentdata);
            } else {
                p_77504_1_.addEnchantment(enchantmentdata.enchantment, enchantmentdata.enchantmentLevel);
                System.out.println(enchantmentdata.enchantment);
            }
        }

        return p_77504_1_;
    }

    public static List<EnchantmentData> buildEnchantmentList(Random p_77513_0_, ItemStack p_77513_1_, int p_77513_2_, boolean p_77513_3_) {
        ArrayList list = Lists.newArrayList();
        Item item = p_77513_1_.getItem();
        int i = p_77513_1_.getItemEnchantability();
        if(i <= 0) {
            return list;
        } else {
            p_77513_2_ = p_77513_2_ + 1 + p_77513_0_.nextInt(i / 4 + 1) + p_77513_0_.nextInt(i / 4 + 1);
            float f = (p_77513_0_.nextFloat() + p_77513_0_.nextFloat() - 1.0F) * 0.15F;
            p_77513_2_ = MathHelper.clamp(Math.round((float)p_77513_2_ + (float)p_77513_2_ * f), 1, 2147483647);
            List list1 = getEnchantmentDatas(p_77513_2_, p_77513_1_, p_77513_3_);
            if(!list1.isEmpty()) {
                list.add(WeightedRandom.getRandomItem(p_77513_0_, list1));

                while(p_77513_0_.nextInt(50) <= p_77513_2_) {
                    EnchantmentHelper.removeIncompatible(list1, (EnchantmentData)Util.getLast(list));
                    if(list1.isEmpty()) {
                        break;
                    }

                    list.add(WeightedRandom.getRandomItem(p_77513_0_, list1));
                    p_77513_2_ /= 2;
                }
            }

            return list;
        }
    }

    public static List<EnchantmentData> getEnchantmentDatas(int p_185291_0_, ItemStack p_185291_1_, boolean p_185291_2_) {
        ArrayList list = Lists.newArrayList();
        Item item = p_185291_1_.getItem();
        boolean flag = p_185291_1_.getItem() == Items.BOOK;
        Iterator var6 = Registry.ENCHANTMENT.iterator();

        while(true) {
            while(true) {
                Enchantment enchantment;
                do {
                    do {
                        do {
                            if(!var6.hasNext()) {
                                return list;
                            }

                            enchantment = (Enchantment)var6.next();
                            //if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("");

                            if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("isTreasure: "+ enchantment.isTreasureEnchantment());
                            if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("isTreasureAllowed: "+ !p_185291_2_);
                            if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("isTreasureANDAllowed: "+ (enchantment.isTreasureEnchantment() && !p_185291_2_));
                            if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("isTreasure: "+ enchantment.isTreasureEnchantment());
                            if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("isTreasureAllowed: "+ !p_185291_2_);
                            if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("isTreasureANDAllowed: "+ (enchantment.isTreasureEnchantment() && !p_185291_2_));
                        } while(enchantment.isTreasureEnchantment() && !p_185291_2_);
                        if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("canGenInLoot: "+ !enchantment.canGenerateInLoot());
                        if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("canGenInLoot: "+ !enchantment.canGenerateInLoot());
                    } while(!enchantment.canGenerateInLoot());
                    if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("canApplyInTable: "+ !enchantment.canApplyAtEnchantingTable(p_185291_1_));
                    if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("isAllowedOnBooks: "+ (!flag || !enchantment.isAllowedOnBooks()));
                    if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("canApplyInTableANDisAllowedOnBooks: "+ (!enchantment.canApplyAtEnchantingTable(p_185291_1_) && (!flag || !enchantment.isAllowedOnBooks())));
                    if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("canApplyInTable: "+ !enchantment.canApplyAtEnchantingTable(p_185291_1_));
                    if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("isAllowedOnBooks: "+ (!flag || !enchantment.isAllowedOnBooks()));
                    if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("canApplyInTableANDisAllowedOnBooks: "+ (!enchantment.canApplyAtEnchantingTable(p_185291_1_) && (!flag || !enchantment.isAllowedOnBooks())));

                } while(!enchantment.canApplyAtEnchantingTable(p_185291_1_) && (!flag || !enchantment.isAllowedOnBooks()));

                if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("MIN LEVEL: "+ enchantment.getMinLevel());
                if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("MIN LEVEL: "+ enchantment.getMinLevel());
                if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("MAX LEVEL: "+ enchantment.getMaxLevel());
                if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("MAX LEVEL: "+ enchantment.getMaxLevel());

                for(int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                    if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("MIN ENCHANTABLITY: "+ enchantment.getMinEnchantability(i));
                    if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("MIN ENCHANTABLITY: "+ enchantment.getMinEnchantability(i));
                    if(enchantment.equals(EnchantmentRegistry.ADVANCED))System.out.println("MAX ENCHANTABLITY: "+ enchantment.getMaxEnchantability(i));
                    if(enchantment.equals(EnchantmentRegistry.CAPACITY))System.out.println("MAX ENCHANTABLITY: "+ enchantment.getMaxEnchantability(i));
                    if(p_185291_0_ >= enchantment.getMinEnchantability(i) && p_185291_0_ <= enchantment.getMaxEnchantability(i)) {
                        list.add(new EnchantmentData(enchantment, i));
                        break;
                    }
                }
            }
        }
    }*/

    @Override
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        float level = getEnchantmentPowerFromSorroundings(world,pos,coin);
        //Need to charge at min 1 level for an enchant
        int actualEnchantingLevel = ((level * 2)<1)?(1):((int)(level * 2));
        int currentlyStoredExp = getXPStored(coin);
        int currentLevelFromStoredXp = getExpLevelFromCount(currentlyStoredExp);
        int xpLevelsNeeded = (actualEnchantingLevel/10);
        int xpAtEnchantingLevel = getExpCountByLevel(actualEnchantingLevel);
        //since this is the number we subtract, if we need at least 1 level then make this 0
        int xpAtLevelsBelowRequired = getExpCountByLevel(((actualEnchantingLevel-xpLevelsNeeded)<1)?(0):((actualEnchantingLevel-xpLevelsNeeded)));
        int expNeeded = (xpAtEnchantingLevel-xpAtLevelsBelowRequired<7)?(7):(xpAtEnchantingLevel-xpAtLevelsBelowRequired);


        if(!world.isBlockPowered(pos))
        {
            for (int i = -2; i <= 2; ++i)
            {
                for (int j = -2; j <= 2; ++j)
                {
                    if (i > -2 && i < 2 && j == -1)
                    {
                        j = 2;
                    }

                    if (rand.nextInt(16) == 0)
                    {
                        for (int k = 0; k <= 2; ++k)
                        {
                            BlockPos blockpos = pos.add(i, k, j);

                            if (world.getBlockState(blockpos).getEnchantPowerBonus(world, pos) > 0) {
                                if (!world.isAirBlock(pos.add(i / 2, 0, j / 2))) {
                                    break;
                                }

                                world.addParticle(ParticleTypes.ENCHANT, (double)pos.getX() + 0.5D, (double)pos.getY() + 2.0D, (double)pos.getZ() + 0.5D, (double)((float)i + rand.nextFloat()) - 0.5D, (double)((float)k - rand.nextFloat() - 1.0F), (double)((float)j + rand.nextFloat()) - 0.5D);
                            }
                        }
                    }
                }
            }

            if(getXPStored(pedestal.getCoinOnPedestal())>0)
            {
                spawnParticleAroundPedestalBase(world,tick,pos,0.1f,0.9f,0.1f,1.f);
            }

            //To show when the enchanting table has enough XP to enchant an item at the current level
            if(currentlyStoredExp >= expNeeded && currentLevelFromStoredXp >= actualEnchantingLevel)
            {
                BlockPos directionalPos = getPosOfBlockBelow(world,pos,0);
                spawnParticleAbovePedestal(world,directionalPos,0.94f,0.8f,0.95f,1.0f);
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

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_xp");
        xpstored.appendString(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.DUMMY_UUID);

        float enchanting = getEnchantmentPowerFromSorroundings(pedestal.getWorld(),pedestal.getPos(),pedestal.getCoinOnPedestal());
        TranslationTextComponent enchantlvl = new TranslationTextComponent(getTranslationKey() + ".chat_enchant");
        enchantlvl.appendString(""+ (int)(enchanting*2) +"");
        enchantlvl.mergeStyle(TextFormatting.LIGHT_PURPLE);
        player.sendMessage(enchantlvl,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(speed);
    }

    public static final Item XPENCHANTER = new ItemUpgradeExpEnchanter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xpenchanter"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPENCHANTER);
    }


}
