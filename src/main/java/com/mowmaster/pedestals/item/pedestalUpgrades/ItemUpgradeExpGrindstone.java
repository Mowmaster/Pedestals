package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeExpGrindstone extends ItemUpgradeBaseExp
{

    public ItemUpgradeExpGrindstone(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (tick%speed == 0) {
                    upgradeAction(world, coinInPedestal, pedestalPos);
                    upgradeActionSendExp(world, coinInPedestal,pedestalPos);
                }
            }
        }
    }

    public int getItemsExpDisenchantAmount(ItemStack stack)
    {
        int exp = 0;
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer integer = entry.getValue();

            exp += enchantment.getMinEnchantability(integer.intValue());
        }
        return exp;
    }

    public void upgradeAction(World world, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        if(!hasMaxXpSet(coinInPedestal)) {setMaxXP(coinInPedestal,getExpCountByLevel(getExpBuffer(coinInPedestal)));}
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
                        TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                        if(pedestalInv instanceof TilePedestal) {
                            if(!((TilePedestal) pedestalInv).hasItem())
                            {
                                if(itemFromInv.isEnchanted())
                                {
                                    int maxXp = readMaxXpFromNBT(coinInPedestal);
                                    int currentlyStoredExp = getXPStored(coinInPedestal);
                                    int xpDisenchant = getItemsExpDisenchantAmount(itemFromInv);
                                    if(maxXp - currentlyStoredExp >= xpDisenchant)
                                    {
                                        //Code Here
                                        Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                        ItemStack stackToReturn = itemFromInv.copy();
                                        stackToReturn.setCount(1);
                                        EnchantmentHelper.setEnchantments(enchantsNone,stackToReturn);
                                        if(!stackToReturn.isEmpty())
                                        {
                                            int getExpLeftInPedestal = currentlyStoredExp + xpDisenchant;
                                            setXPStored(coinInPedestal,getExpLeftInPedestal);
                                            handler.extractItem(i,stackToReturn.getCount() ,false );
                                            world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
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

    public int getExpBuffer(ItemStack stack)
    {
        return  30;
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

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,player.getUniqueID());

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

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(speed);
    }

    public static final Item XPGRINDSTONE = new ItemUpgradeExpGrindstone(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xpgrindstone"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPGRINDSTONE);
    }


}
