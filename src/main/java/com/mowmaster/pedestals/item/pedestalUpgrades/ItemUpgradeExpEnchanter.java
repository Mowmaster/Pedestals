package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
        int value = 30;
        switch (getCapacityModifier(stack))
        {
            case 0:
                value = 30;//30
                break;
            case 1:
                value=44;//44
                break;
            case 2:
                value = 58;//58
                break;
            case 3:
                value = 72;//72
                break;
            case 4:
                value = 86;//86
                break;
            case 5:
                value=100;//100
                break;
            default: value=30;
        }

        return  value;
    }

    public float getEnchantmentPowerFromSorroundings(World world, BlockPos posOfPedestal)
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

    public float getEnchantmentPowerFromSorroundings(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {

        float enchantPower = 0;
        //int getMaxEnchantLevel = getExpBuffer(coinInPedestal);

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

        /*if((int)(enchantPower*2) > getMaxEnchantLevel)
        {
            enchantPower = (float)(getMaxEnchantLevel/2);
        }*/

        return enchantPower;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (tick%speed == 0) {
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

        //if(world.getTileEntity(posInventory) !=null)
        //{
        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(hasAdvancedInventoryTargeting(coinInPedestal))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

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
                    int i = getNextSlotWithItemsCap(cap ,getStackInPedestal(world,posOfPedestal));
                        if(i>=0)
                        {
                            itemFromInv = handler.getStackInSlot(i);
                            int slotCount = itemFromInv.getCount();
                            TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                            if(pedestalInv instanceof TilePedestal) {
                                if(!((TilePedestal) pedestalInv).hasItem())
                                {
                                    if(itemFromInv.isEnchantable() || itemFromInv.getItem().equals(Items.BOOK))
                                    {
                                        //This is Book Shelf Enchanting level, not enchantment level (15 bookshelfves = 30 levels of enchantability)
                                        float level = getEnchantmentPowerFromSorroundings(world,posOfPedestal,coinInPedestal);
                                        int actualEnchantingLevel = (int)(level * 2);
                                        int currentlyStoredExp = getXPStored(coinInPedestal);
                                        int currentLevelFromStoredXp = getExpLevelFromCount(currentlyStoredExp);
                                        int xpLevelsNeeded = Math.round(actualEnchantingLevel/10);
                                        int xpAtEnchantingLevel = getExpCountByLevel(actualEnchantingLevel);
                                        int xpAtLevelsBelowRequired = getExpCountByLevel((actualEnchantingLevel-xpLevelsNeeded));
                                        int expNeeded = xpAtEnchantingLevel-xpAtLevelsBelowRequired;

                                        if(currentlyStoredExp >= expNeeded && currentLevelFromStoredXp >= actualEnchantingLevel)
                                        {
                                            //Enchanting Code Here
                                            Random rand = new Random();
                                            ItemStack itemToEnchant = itemFromInv.copy();
                                            itemToEnchant.setCount(1);
                                            //the boolean at the end controls if treasure enchants are allowed.
                                            ItemStack stackToReturn = EnchantmentHelper.addRandomEnchantment(rand,itemToEnchant ,actualEnchantingLevel ,true );
                                            if(!stackToReturn.isEmpty() && stackToReturn.isEnchanted())
                                            {
                                                int getExpLeftInPedestal = currentlyStoredExp - expNeeded;
                                                setXPStored(coinInPedestal,getExpLeftInPedestal);
                                                handler.extractItem(i,stackToReturn.getCount() ,false );
                                                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.35F, 1.0F);
                                                ((TilePedestal) pedestalInv).addItem(stackToReturn);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        ItemStack toReturn = itemFromInv.copy();
                                        handler.extractItem(i,toReturn.getCount() ,false );
                                        ((TilePedestal) pedestalInv).addItem(toReturn);
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
    public void onRandomDisplayTick(TilePedestal pedestal,int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        float level = getEnchantmentPowerFromSorroundings(world,pos,coin);
        int actualEnchantingLevel = (int)(level * 2);
        int currentlyStoredExp = getXPStored(coin);
        int currentLevelFromStoredXp = getExpLevelFromCount(currentlyStoredExp);
        int xpLevelsNeeded = Math.round(actualEnchantingLevel/10);
        int xpAtEnchantingLevel = getExpCountByLevel(actualEnchantingLevel);
        int xpAtLevelsBelowRequired = getExpCountByLevel((actualEnchantingLevel-xpLevelsNeeded));
        int expNeeded = xpAtEnchantingLevel-xpAtLevelsBelowRequired;

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

            if(!world.isBlockPowered(pos))
            {
                if(getXPStored(pedestal.getCoinOnPedestal())>0)
                {
                    spawnParticleAroundPedestalBase(world,tick,pos,0.1f,0.9f,0.1f,1.f);
                }
            }

            //To show when the enchanting table has enough XP to enchant an item at the current level
            if(currentlyStoredExp >= expNeeded && currentLevelFromStoredXp >= actualEnchantingLevel)
            {
                spawnParticleAbovePedestal(world,pos,0.94f,0.8f,0.95f,1.0f);
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_xp");
        xpstored.appendString(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,player.getUniqueID());

        float enchanting = getEnchantmentPowerFromSorroundings(pedestal.getWorld(),pedestal.getPos(),pedestal.getCoinOnPedestal());
        TranslationTextComponent enchantlvl = new TranslationTextComponent(getTranslationKey() + ".chat_enchant");
        enchantlvl.appendString(""+ (int)(enchanting*2) +"");
        enchantlvl.mergeStyle(TextFormatting.LIGHT_PURPLE);
        player.sendMessage(enchantlvl,player.getUniqueID());

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,player.getUniqueID());
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
