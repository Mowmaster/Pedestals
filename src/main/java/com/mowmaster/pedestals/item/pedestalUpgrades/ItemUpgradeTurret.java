package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeTurret extends ItemUpgradeBase
{
    public ItemUpgradeTurret(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptArea() {
        return true;
    }

    @Override
    public boolean canAcceptRange() {
        return true;
    }

    @Override
    public boolean canAcceptAdvanced() { return true; }

    public int getAreaWidth(ItemStack stack)
    {
        int areaWidth = 0;
        int aW = getAreaModifier(stack);
        areaWidth = ((aW)+1);
        return  areaWidth;
    }

    public int getRangeHeight(ItemStack stack)
    {
        return getHeight(stack);
    }

    public int getHeight(ItemStack stack)
    {
        return  getRangeTiny(stack);
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{getRangeHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            BlockPos pedestalPos = pedestal.getPos();
            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
                ItemStack itemInPedestal = pedestal.getItemInPedestal();

                int speed = getOperationSpeed(coinInPedestal);
                if(itemInPedestal.getItem() instanceof TippedArrowItem)speed=20;
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal);
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        //ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack toolInPedestal = pedestal.getToolOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();
        int width = getAreaWidth(coinInPedestal);
        int height = getRangeHeight(coinInPedestal);
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);
        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);
        if(!hasFilterBlock(coinInPedestal)) {writeFilterBlockToNBT(pedestal);}
        Block filterBlock = readFilterBlockFromNBT(coinInPedestal);

        List<LivingEntity> itemList = world.getEntitiesWithinAABB(LivingEntity.class,getBox);
        for(LivingEntity getEntityFromList : itemList)
        {
            LivingEntity selectedEntity = getTargetEntity(filterBlock,getEntityFromList);

            if(selectedEntity != null)
            {
                FakePlayer fakePlayer = fakePedestalPlayer(pedestal).get();
                if(fakePlayer !=null)
                {
                    fakePlayer.setSilent(true);
                    if(!fakePlayer.getPosition().equals(new BlockPos(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ()))) {fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());}
                    if (pedestal.hasTool() && !fakePlayer.getHeldItemMainhand().equals(toolInPedestal)) {fakePlayer.setItemStackToSlot(EquipmentSlotType.MAINHAND, toolInPedestal);}
                    if (toolInPedestal.isEmpty() && !fakePlayer.getHeldItemMainhand().equals(toolInPedestal)) {fakePlayer.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);}
                    //Using the custom pedestal one this should work fine now...

                    attackEntityWithRangedAttack(pedestal,fakePlayer,selectedEntity,10.0f);
                    //ADVANCED OPTION: BowItem --> public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft)
                    //Make it so it emulates a player shooting a bow, which means any modded bow with effects could take advantage of it >:)
                }
            }
        }
    }



    public void attackEntityWithRangedAttack(PedestalTileEntity pedestal,FakePlayer fakePlayer, LivingEntity target, float distanceFactor) {
        World world = pedestal.getWorld();
        ItemStack tool = pedestal.getToolOnPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        double[] doubleBlockAdd = getPedTopPos(pedestal, 1.0);
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        Block filterBlock = readFilterBlockFromNBT(coinInPedestal);
        ArrowEntity arrowEntity = new ArrowEntity(world,pedestalPos.getX()+doubleBlockAdd[0],pedestalPos.getY()+doubleBlockAdd[1],pedestalPos.getZ()+doubleBlockAdd[2]);
        if(itemInPedestal.getItem() instanceof TippedArrowItem)
        {
            arrowEntity.setPotionEffect(itemInPedestal);
            arrowEntity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
        }
        double d0 = target.getPosYEye() - (double)1.1F;
        double d1 = target.getPosX() - pedestalPos.getX();
        double d2 = d0 - arrowEntity.getPosY();
        double d3 = target.getPosZ() - pedestalPos.getZ();
        float f = MathHelper.sqrt(d1 * d1 + d3 * d3) * 0.2F;
        arrowEntity.shoot(d1, d2 + (double)f, d3, 1.6F, 12.0F);

        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, tool);
        if (j > 0) {
            arrowEntity.setDamage(arrowEntity.getDamage() + (double)j * 0.5D + 0.5D);
        }

        int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, tool);
        if (k > 0) {
            arrowEntity.setKnockbackStrength(k);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, tool) > 0) {
            arrowEntity.setFire(100);
        }

        if(filterBlock.equals(Blocks.NETHERITE_BLOCK)) {arrowEntity.setDamage(arrowEntity.getDamage() * 2.0D);}

        if(itemInPedestal.getItem() instanceof TippedArrowItem)
        {
            Potion potion = PotionUtils.getPotionFromItem(itemInPedestal);
            List<EffectInstance> customPotionEffects = potion.getEffects();

            int count = 0;
            for(EffectInstance effectinstance : customPotionEffects) {
                if(target.getActivePotionEffect(effectinstance.getPotion())!=null)count++;
            }
            if(count != customPotionEffects.size()){
                if(world.addEntity(arrowEntity))
                {
                    if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.HOSTILE, 0.25F, 1.0F);
                    pedestal.removeItem(1);
                }
            }
        }
        else {
            if (world.addEntity(arrowEntity))if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.HOSTILE, 0.25F, 1.0F);
        }
    }

    //Just update the block, whatever it is. genrally this wont be changing much anyway so we'll take the hit when it does change.
    @Override
    public void onPedestalBelowNeighborChanged(PedestalTileEntity pedestal, BlockState blockChanged, BlockPos blockChangedPos)
    {
        BlockPos blockBelow = getPosOfBlockBelow(pedestal.getWorld(),pedestal.getPos(),1);
        if(blockBelow.equals(blockChangedPos))
        {
            writeFilterBlockToNBT(pedestal);
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();
        Block filterBlock = (hasFilterBlock(stack))?(readFilterBlockFromNBT(stack)):(Blocks.AIR);

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("" + getRangeHeight(stack) + "");
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);

        ItemStack toolStack = (pedestal.hasTool())?(pedestal.getToolOnPedestal()):(ItemStack.EMPTY);
        if(!toolStack.isEmpty())
        {
            TranslationTextComponent tool = new TranslationTextComponent(getTranslationKey() + ".chat_tool");
            tool.append(toolStack.getDisplayName());
            tool.mergeStyle(TextFormatting.BLUE);
            player.sendMessage(tool,Util.DUMMY_UUID);
        }

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments((pedestal.hasTool())?(pedestal.getToolOnPedestal()):(stack));
        //Not Used Yet
        /*if(hasAdvancedInventoryTargeting(stack))
        {
            map.put(EnchantmentRegistry.ADVANCED,1);
        }*/
        if(map.size() > 0 && getNumNonPedestalEnchants(map)>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_enchants");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.DUMMY_UUID);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants,Util.DUMMY_UUID);
                }
            }
        }

        TranslationTextComponent entityType = new TranslationTextComponent(getTranslationKey() + ".chat_entity");
        entityType.appendString(getTargetEntity(filterBlock));
        entityType.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(entityType,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("" + getRangeHeight(stack) + "");
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + (int)((getCapacityModifier(stack) + 2.0F)) + "");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        rate.mergeStyle(TextFormatting.GRAY);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item TURRET = new ItemUpgradeTurret(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/turret"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(TURRET);
    }


}
