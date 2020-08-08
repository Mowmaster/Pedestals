package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.references.Reference.MODID;
import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemUpgradeExpAnvil extends ItemUpgradeBaseExp
{

    public ItemUpgradeExpAnvil(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
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

    public int getRepairRate(ItemStack stack)
    {
        int value = 5;
        switch (getCapacityModifier(stack))
        {
            case 0:
                value = 5;//10 durability/ operation
                break;
            case 1:
                value=10;//20
                break;
            case 2:
                value = 15;//30
                break;
            case 3:
                value = 20;//40
                break;
            case 4:
                value = 25;//50
                break;
            case 5:
                value= 50;//100
                break;
            default: value=1;
        }

        return  value;
    }

    private int correctlyPlacedSorroundingPedestals(World world, BlockPos posPedestal)
    {
        int around = 0;
        ArrayList<BlockPos> posSorroundingPedestals = new ArrayList<BlockPos>();
        posSorroundingPedestals.add(posPedestal.add(2,0,0));
        posSorroundingPedestals.add(posPedestal.add(0,0,2));
        posSorroundingPedestals.add(posPedestal.add(-2,0,0));
        posSorroundingPedestals.add(posPedestal.add(0,0,-2));
        for(int i = 0;i<posSorroundingPedestals.size();i++)
        {
            Block pedestal = world.getBlockState(posSorroundingPedestals.get(i)).getBlock();
            if(pedestal instanceof BlockPedestalTE)
            {
                around +=1;
            }
        }

        return around;
    }

    private ArrayList<ItemStack> doSorroundingPedestalsHaveItemsToCombine(World world, BlockPos pedestalPos)
    {
        ArrayList<ItemStack> stackOnSorroundingPedestal = new ArrayList<ItemStack>();

        ArrayList<BlockPos> posSorroundingPedestals = new ArrayList<BlockPos>();
        posSorroundingPedestals.add(pedestalPos.add(2,0,0));
        posSorroundingPedestals.add(pedestalPos.add(0,0,2));
        posSorroundingPedestals.add(pedestalPos.add(-2,0,0));
        posSorroundingPedestals.add(pedestalPos.add(0,0,-2));

        for(int i=0; i< posSorroundingPedestals.size();i++)
        {
            if(!world.getBlockState(posSorroundingPedestals.get(i)).getBlock().equals(Blocks.AIR))
            {
                TileEntity tile = world.getTileEntity(posSorroundingPedestals.get(i));
                if(tile instanceof TilePedestal)
                {
                    TilePedestal tilePedestal = (TilePedestal)tile;
                    ItemStack stack = tilePedestal.getItemInPedestal();
                    // ToDo:CRYSTALS HERE
                    //  || stack.getItem().equals(Items.DIAMOND) we only need to combine if we have an enchanted item, book or nametag, crystals are not needed necessarily
                    if(stack.isEnchanted() || stack.getItem().equals(Items.ENCHANTED_BOOK) || stack.getItem().equals(Items.NAME_TAG) || stack.getItem().equals(Items.DIAMOND))
                    {
                        stackOnSorroundingPedestal.add(stack);
                    }
                }
            }
        }

        return stackOnSorroundingPedestal;
    }

    private void deleteItemsOnPedestals(World world, BlockPos pedestalPos, int crystals)
    {
        int crystalsToRemove = crystals;
        ArrayList<BlockPos> posSorroundingPedestals = new ArrayList<BlockPos>();
        posSorroundingPedestals.add(pedestalPos.add(2,0,0));
        posSorroundingPedestals.add(pedestalPos.add(0,0,2));
        posSorroundingPedestals.add(pedestalPos.add(-2,0,0));
        posSorroundingPedestals.add(pedestalPos.add(0,0,-2));

        for(int i=0; i< posSorroundingPedestals.size();i++)
        {
            if(!world.getBlockState(posSorroundingPedestals.get(i)).getBlock().equals(Blocks.AIR))
            {
                TileEntity tile = world.getTileEntity(posSorroundingPedestals.get(i));
                if(tile instanceof TilePedestal)
                {
                    TilePedestal tilePedestal = (TilePedestal)tile;
                    ItemStack stack = tilePedestal.getItemInPedestal();
                    if(stack.isEnchanted() || stack.getItem().equals(Items.ENCHANTED_BOOK) || stack.getItem().equals(Items.NAME_TAG))
                    {
                        tilePedestal.removeItem(1);
                    }
                    else if(stack.getItem().equals(Items.DIAMOND))
                    {
                        if(stack.getCount() >= crystalsToRemove)
                        {
                            tilePedestal.removeItem(crystalsToRemove);
                            crystalsToRemove = 0;
                        }
                        else
                        {
                            crystalsToRemove = crystalsToRemove - stack.getCount();
                            tilePedestal.removeItem();
                        }
                    }
                }
            }
        }
    }

    private int getCrystals(ArrayList<ItemStack> stackToCombine)
    {
        int crystals = 0;
        for(int i=0;i<stackToCombine.size();i++)
        {
            if(stackToCombine.get(i).getItem().equals(Items.DIAMOND))
            {
                crystals = crystals + stackToCombine.get(i).getCount();
            }
        }

        return crystals;
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
        ArrayList<ItemStack> stackToCombine = doSorroundingPedestalsHaveItemsToCombine(world,posOfPedestal);
        String strNameToChangeTo = "";
        Map<Enchantment, Integer> enchantsMap = Maps.<Enchantment, Integer>newLinkedHashMap();
        int overCombine = getCrystals(stackToCombine);
        int overCombineCopy = overCombine;
        int crystalsToRemoveCount = 0;

        int intExpInCoin = getXPStored(coinInPedestal);
        int intRepairRate = getRepairRate(coinInPedestal);
        int intLevelCostToCombine = 0;

        //if(world.getTileEntity(posInventory) !=null)
        //{
        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

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
                                TilePedestal tilePedestal = (TilePedestal) pedestalInv;

                                if(!tilePedestal.hasItem())
                                {
                                    //Repair first if possible
                                    if(itemFromInv.isDamaged())
                                    {
                                        if(itemFromInv.getItem().isRepairable(itemFromInv) && itemFromInv.getItem().getMaxDamage(itemFromInv) > 0)
                                        {
                                            if(intExpInCoin >= intRepairRate)
                                            {
                                                setXPStored(coinInPedestal,(intExpInCoin-intRepairRate));
                                                itemFromInv.setDamage(itemFromInv.getDamage() - (intRepairRate*2));
                                            }
                                        }
                                    }
                                    else if(stackToCombine.size() > 0)
                                    {
                                        //First check if other enchants exist, then we Need to add the item to enchantments list for combining
                                        stackToCombine.add(itemFromInv);

                                        for(int e=0;e<stackToCombine.size();e++)
                                        {
                                            if(stackToCombine.get(e).isEnchanted() || stackToCombine.get(e).getItem().equals(Items.ENCHANTED_BOOK))
                                            {
                                                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stackToCombine.get(e));

                                                for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                                                    Enchantment enchantment = entry.getKey();
                                                    Integer integer = entry.getValue();

                                                    switch(enchantment.getRarity())
                                                    {
                                                        case COMMON:
                                                            intLevelCostToCombine +=Math.round(1.0F*(integer+1));
                                                            break;
                                                        case UNCOMMON:
                                                            intLevelCostToCombine +=Math.round(2.0F*(integer+1));
                                                            break;
                                                        case RARE:
                                                            intLevelCostToCombine +=Math.round(3.0F*(integer+1));
                                                            break;
                                                        case VERY_RARE:
                                                            intLevelCostToCombine +=Math.round(4.0F*(integer+1));
                                                            break;
                                                    }

                                                    //Check if enchant already exists in list
                                                    if(enchantsMap.containsKey(enchantment))
                                                    {
                                                        //if it does then get the value of it
                                                        int intNewValue = 0;
                                                        int e1 = enchantsMap.get(enchantment).intValue();
                                                        int e2 = integer;
                                                        if(e1 == e2)
                                                        {
                                                            intNewValue = e2 + 1;
                                                            if(intNewValue > enchantment.getMaxLevel())
                                                            {
                                                                if((overCombine-crystalsToRemoveCount) >= intNewValue)
                                                                {
                                                                    crystalsToRemoveCount += intNewValue;
                                                                }
                                                                else
                                                                {
                                                                    intNewValue = enchantment.getMaxLevel();
                                                                }
                                                            }

                                                            enchantsMap.put(enchantment, intNewValue);
                                                        }
                                                        else if(e1 > e2)
                                                        {
                                                            //if existing enchant is better then the one being applied, then skip just this one
                                                            continue;
                                                        }
                                                        else
                                                        {
                                                            enchantsMap.put(enchantment, integer);
                                                        }
                                                    }
                                                    else
                                                    {
                                                        enchantsMap.put(enchantment, integer);
                                                    }

                                                }

                                            }
                                            else if(stackToCombine.get(e).getItem().equals(Items.NAME_TAG))
                                            {
                                                strNameToChangeTo = stackToCombine.get(e).getDisplayName().getString();
                                            }

                                        }

                                        //System.out.println("Level To Combine: "+ intLevelCostToCombine);
                                        int intExpCostToCombine = getExpCountByLevel(intLevelCostToCombine);
                                        //System.out.println("XP To Combine: "+ intExpCostToCombine);
                                        if(intExpInCoin >= intExpCostToCombine)
                                        {
                                            ItemStack itemFromInvCopy = itemFromInv.copy();
                                            itemFromInvCopy.setCount(1);
                                            //Charge Exp Cost
                                            setXPStored(coinInPedestal,(intExpInCoin-intExpCostToCombine));
                                            //Delete Items On Pedestals
                                            deleteItemsOnPedestals(world,posOfPedestal,crystalsToRemoveCount);
                                            EnchantmentHelper.setEnchantments(enchantsMap,itemFromInvCopy);
                                            if(strNameToChangeTo != "")
                                            {
                                                itemFromInvCopy.setDisplayName(new TranslationTextComponent(strNameToChangeTo));
                                            }
                                            handler.extractItem(i,itemFromInvCopy.getCount(),false);
                                            world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                            tilePedestal.addItem(itemFromInvCopy);
                                        }
                                    }
                                    else
                                    {
                                        ItemStack itemFromInvCopy = itemFromInv.copy();
                                        handler.extractItem(i,itemFromInvCopy.getCount(),false);
                                        tilePedestal.addItem(itemFromInvCopy);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        //}
    }

    //Particles come in from correctly placed pedestals???
    /*@Override
    public void onRandomDisplayTick(TilePedestal pedestal, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
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
        }
    }*/

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

        TranslationTextComponent sorround = new TranslationTextComponent(getTranslationKey() + ".chat_sorround");
        sorround.appendString(""+ correctlyPlacedSorroundingPedestals(pedestal.getWorld(),pedestal.getPos()) +"");
        sorround.mergeStyle(TextFormatting.AQUA);
        player.sendMessage(sorround,player.getUniqueID());

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

    public static final Item XPANVIL = new ItemUpgradeExpAnvil(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xpanvil"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPANVIL);
    }


}
