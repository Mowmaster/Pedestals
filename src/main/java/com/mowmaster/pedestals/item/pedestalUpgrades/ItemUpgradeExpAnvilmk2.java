package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
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

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeExpAnvilmk2 extends ItemUpgradeBaseExp
{

    public ItemUpgradeExpAnvilmk2(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

    //Should be scaled based on vanilla's repair modifier. May also go back to requiring 'Mending' enchant to make this work properly
    //Need to insure that this is still based somewhat on mendings abilities
    public int getRepairRate(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = (capacityOver*10);
        return  (overEnchanted>maxStored)?(maxStored):(overEnchanted);
    }

    /*
    this needs changed up to check an area, and cache this area to some sort of "memory"
    then we can have however many pedestals in the area be allowed to work?

    or maybe make an upgrade that checks an area, and links itself automatically to the closest anvil???
    might have to make something that fires when its pulled out of the anvil though...
     */
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
            if(pedestal instanceof PedestalBlock)
            {
                around +=1;
            }
        }

        return around;
    }

    /*
    if we had pedestals that autolink themselves, we could just keep a queue going in the anvil, and the pedestals around it could send updates when their inv changes
    then only do a final check before the actual process???
     */
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
                if(tile instanceof PedestalTileEntity)
                {
                    PedestalTileEntity tilePedestal = (PedestalTileEntity)tile;
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
                if(tile instanceof PedestalTileEntity)
                {
                    PedestalTileEntity tilePedestal = (PedestalTileEntity)tile;
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


    public void getEnchantFromResourceLocationName()
    {
        //ListNBT listnbt = p_82781_0_.getItem() == Items.ENCHANTED_BOOK?EnchantedBookItem.getEnchantments(p_82781_0_):p_82781_0_.getEnchantmentTagList();
        //NEED SOME WAY TO GET AN ENCHANT FROM A STRING NAME!!!
    }

    /*
    ascension details
    every 5 levels after lvl10 of an enchantment should require a base material cost based on the enchantment(default cost for any enchant not specified, will probably use diamonds here)
    Honestly it might be best if i get into world gen of plants and also custom mob drops based off specific interactions

Other materials: Phantom brains, netherite, totems of undying, elytra, dragon breath bottles, chorus plants, dragon heads, netherstars, sponge
gueadian drops, fish, coral, notalisk shells, or orb of the seas.

https://youtu.be/BwwtnhWm480?t=24

genshin impact uses a boss or guild reward as 1 item, an inworkd material as the second, and a mob drop as the third
weapons however use dungeon loot + 2 mob loots

sooooo, maybe add boss drops based on death conditions,
added wandering villager trades
also a way to combine materials, so maybe an inworld crafter???

in worldgen for custom flowers, and bush plants that are biome specific.

added mob drops, that have secondary uses so theyre not 'worthless' maybe look into reliquary???


PROTECTION: leather or turtle shells
SHARPNESS:  inst damage potions???
EFFICIENCY: Redstone blocks???
POWER:
PIERCING:   Arrows?

FIRE_PROTECTION:
FEATHER_FALLING:
PROJECTILE_PROTECTION:
SMITE:
BANE_OF_ARTHROPODS:
KNOCKBACK:
UNBREAKING:
LOYALTY:
QUICK_CHARGE:

BLAST_PROTECTION:
RESPIRATION:
AQUA_AFFINITY:
DEPTH_STRIDER:
FROST_WALKER:
FIRE_ASPECT:
LOOTING:    Rabbits feet
SWEEPING:
FORTUNE:    Rabbits feet
PUNCH:
FLAME:
LUCK_OF_THE_SEA:
LURE:
IMPALING:
RIPTIDE:
MULTISHOT:
MENDING:

THORNS:
BINDING_CURSE:
SOUL_SPEED:
SILK_TOUCH:
INFINITY:
CHANNELING:
VANISHING_CURSE:

  but basically i need to make a special "display" slot for the pedestal that i can force update when needed, this way i can ask for specific materials in
  specific pedestals
     */
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
        ArrayList<ItemStack> stackToCombine = doSorroundingPedestalsHaveItemsToCombine(world,posOfPedestal);
        String strNameToChangeTo = "";
        Map<Enchantment, Integer> enchantsMap = Maps.<Enchantment, Integer>newLinkedHashMap();
        int overCombine = getCrystals(stackToCombine);
        int overCombineCopy = overCombine;
        int crystalsToRemoveCount = 0;

        int intExpInCoin = getXPStored(coinInPedestal);
        int intRepairRate = getRepairRate(coinInPedestal);
        int intLevelCostToCombine = 0;

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(hasAdvancedInventoryTargeting(coinInPedestal))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(!isInventoryEmpty(cap))
        {
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                if(invToPullFrom instanceof PedestalTileEntity) {
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
                                PedestalTileEntity tilePedestal = (PedestalTileEntity) pedestalInv;

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
                                                            intLevelCostToCombine +=Math.round(2.0F*(integer+1));
                                                            break;
                                                        case UNCOMMON:
                                                            intLevelCostToCombine +=Math.round(4.0F*(integer+1));
                                                            break;
                                                        case RARE:
                                                            intLevelCostToCombine +=Math.round(6.0F*(integer+1));
                                                            break;
                                                        case VERY_RARE:
                                                            intLevelCostToCombine +=Math.round(8.0F*(integer+1));
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

        TranslationTextComponent sorround = new TranslationTextComponent(getTranslationKey() + ".chat_sorround");
        sorround.appendString(""+ correctlyPlacedSorroundingPedestals(pedestal.getWorld(),pedestal.getPos()) +"");
        sorround.mergeStyle(TextFormatting.AQUA);
        player.sendMessage(sorround,Util.DUMMY_UUID);

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

    public static final Item XPANVIL = new ItemUpgradeExpAnvilmk2(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xpanvil"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPANVIL);
    }


}
