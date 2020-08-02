package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeCobbleGen extends ItemUpgradeBase
{
    private int maxStored = Integer.MAX_VALUE;

    public ItemUpgradeCobbleGen(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    public int getCobbleGenSpawnRate(ItemStack stack)
    {
        int intCobbleSpawned = 1;
        switch (getCapacityModifier(stack))
        {
            case 0:
                intCobbleSpawned = 1;
                break;
            case 1:
                intCobbleSpawned=4;
                break;
            case 2:
                intCobbleSpawned = 8;
                break;
            case 3:
                intCobbleSpawned = 16;
                break;
            case 4:
                intCobbleSpawned = 32;
                break;
            case 5:
                intCobbleSpawned=64;
                break;
            default: intCobbleSpawned=1;
        }

        return  intCobbleSpawned;
    }

    public Item getItemToSpawn(ItemStack coinInPedestal)
    {
        Item getItem = new ItemStack(Blocks.COBBLESTONE).getItem();
        if(coinInPedestal.isEnchanted())
        {
            if(EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH,coinInPedestal)> 0)
            {
                getItem = new ItemStack(Blocks.STONE).getItem();
            }
        }
        return getItem;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);

            if(!world.isBlockPowered(pedestalPos))
            {
                //Keep Pedestal Full at all times
                fillPedestalAction(world,itemInPedestal,coinInPedestal,pedestalPos);
                //Cobble Gen Only Works So Fast
                if (tick%speed == 0) {
                    upgradeAction(world,itemInPedestal,coinInPedestal,pedestalPos);
                }
            }
        }
    }

    public void fillPedestalAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        int intSpace = intSpaceLeftInStack(itemInPedestal);
        TileEntity tileCheckForPedestal = world.getTileEntity(pedestalPos);
        ItemStack stackSpawnedItem = new ItemStack(getItemToSpawn(coinInPedestal));
        stackSpawnedItem.setCount(intSpace);
        if(tileCheckForPedestal instanceof TilePedestal)
        {
            TilePedestal tilePedestal = ((TilePedestal)tileCheckForPedestal);
            int intGetStored = tilePedestal.getStoredValueForUpgrades();
            if(intSpace>0)
            {
                if(intGetStored >= intSpace)
                {
                    int intNewStored = intGetStored - intSpace;
                    tilePedestal.setStoredValueForUpgrades(intNewStored);
                    tilePedestal.addItem(stackSpawnedItem);
                }
                else
                {
                    int intNewStored = 0;
                    stackSpawnedItem.setCount(intGetStored);
                    tilePedestal.setStoredValueForUpgrades(intNewStored);
                    tilePedestal.addItem(stackSpawnedItem);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        TileEntity tileCheckForPedestal = world.getTileEntity(pedestalPos);
        int intSpawnRate = getCobbleGenSpawnRate(coinInPedestal);
        if(tileCheckForPedestal instanceof TilePedestal)
        {
            TilePedestal tilePedestal = ((TilePedestal)tileCheckForPedestal);
            int intGetStored = tilePedestal.getStoredValueForUpgrades();
            int intNewStored = intGetStored + intSpawnRate;
            if(intGetStored <= (maxStored - intSpawnRate))
            {
                tilePedestal.setStoredValueForUpgrades(intNewStored);
            }
        }
    }


    @Override
    public void actionOnCollideWithBlock(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack stackPedestal = tilePedestal.getItemInPedestal();
            ItemStack stackCollidedItem = ((ItemEntity) entityIn).getItem();
            int intCurrentlyStored = tilePedestal.getStoredValueForUpgrades();
            if(doItemsMatch(stackPedestal,stackCollidedItem))
            {
                if(intSpaceLeftInStack(stackPedestal) >= stackCollidedItem.getCount())
                {
                    ItemStack stackCollidedItemCopy = stackCollidedItem.copy();
                    entityIn.remove();
                    tilePedestal.addItem(stackCollidedItemCopy);
                }
                else
                {
                    ItemStack stackCollidedItemCopy = stackCollidedItem.copy();
                    stackCollidedItemCopy.setCount(intSpaceLeftInStack(stackPedestal));
                    int intCountDifference = stackCollidedItem.getCount() - intSpaceLeftInStack(stackPedestal);
                    if((intCurrentlyStored+intCountDifference) < maxStored)
                    {
                        tilePedestal.setStoredValueForUpgrades((intCurrentlyStored+intCountDifference));
                    }
                    entityIn.remove();
                    tilePedestal.addItem(stackCollidedItemCopy);
                }
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.func_240699_a_(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());


        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.func_240702_b_("" +  getItemTransferRate(stack) + "");
        rate.func_240699_a_(TextFormatting.GRAY);
        player.sendMessage(rate,player.getUniqueID());

        TranslationTextComponent stored = new TranslationTextComponent(getTranslationKey() + ".chat_stored");
        stored.func_240702_b_("" +  (pedestal.getStoredValueForUpgrades()+pedestal.getItemInPedestal().getCount()) + "");
        stored.func_240699_a_(TextFormatting.GREEN);
        player.sendMessage(stored,player.getUniqueID());

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        if(map.size() > 0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_enchants");
            enchant.func_240699_a_(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,player.getUniqueID());

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.func_240699_a_(TextFormatting.GRAY);
                    player.sendMessage(enchants,player.getUniqueID());
                }
            }
        }

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));
        speed.func_240699_a_(TextFormatting.RED);
        player.sendMessage(speed,player.getUniqueID());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.func_240702_b_("" + getCobbleGenSpawnRate(stack) + "");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        rate.func_240699_a_(TextFormatting.GRAY);
        tooltip.add(rate);

        speed.func_240699_a_(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item COBBLE = new ItemUpgradeCobbleGen(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/cobble"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(COBBLE);
    }


}
