package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeAttacker extends ItemUpgradeBase
{
    public ItemUpgradeAttacker(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    //For damage
    @Override
    public Boolean canAcceptCapacity() {return true;}

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    public int getAreaWidth(ItemStack stack)
    {
        int areaWidth = 0;
        int aW = getAreaModifier(stack);
        areaWidth = ((aW)+1);
        return  areaWidth;
    }

    public float getSwordDamage(LivingEntity entityIn, ItemStack itemInPedestal)
    {
        float damage = 2.0f;
        if(itemInPedestal.getItem() instanceof SwordItem){
            SwordItem sword = (SwordItem)itemInPedestal.getItem();
            if(sword.getAttackDamage() > damage)damage += sword.getAttackDamage();
        }

        if(EnchantmentHelper.getEnchantments(itemInPedestal).containsKey(Enchantments.SHARPNESS))
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS,itemInPedestal);
            damage += 1+(lvl*0.5);
        }
        if(entityIn instanceof SpiderEntity && EnchantmentHelper.getEnchantments(itemInPedestal).containsKey(Enchantments.BANE_OF_ARTHROPODS))
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS,itemInPedestal);
            damage += 1+(lvl*0.5);
        }
        if(entityIn instanceof ZombieEntity || entityIn instanceof SkeletonEntity && EnchantmentHelper.getEnchantments(itemInPedestal).containsKey(Enchantments.BANE_OF_ARTHROPODS))
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS,itemInPedestal);
            damage += 1+(lvl*0.5);
        }

        return  damage;
    }

    public float getAttackDamage(LivingEntity entityIn, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        float damage = getCapacityModifier(coinInPedestal)*2 + 2.0F;
        damage += getSwordDamage(entityIn,itemInPedestal);
        return damage;
    }

    public float getMostlyDamage(TilePedestal pedestal)
    {
        ItemStack inPedestal = pedestal.getItemInPedestal();
        float damage = getCapacityModifier(pedestal.getCoinOnPedestal()) + 2.0f;
        float damage2 = 2.0f;
        if(inPedestal.getItem() instanceof SwordItem){
            SwordItem sword = (SwordItem)inPedestal.getItem();
            if(sword.getAttackDamage() > damage2){
                damage += (sword.getAttackDamage()/2);
            }
        }
        if(EnchantmentHelper.getEnchantments(inPedestal).containsKey(Enchantments.SHARPNESS))
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS,inPedestal);
            damage += Math.round((1+(lvl*0.5))/2);
        }

        return damage;
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
        int width = getAreaWidth(coinInPedestal);
        int height = (2*width)+1;
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

        List<LivingEntity> itemList = world.getEntitiesWithinAABB(LivingEntity.class,getBox);
        for(LivingEntity getEntityFromList : itemList)
        {
            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().func_241755_D_());
            fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());

            List<String> list = Arrays.asList("pedestal1", "pedestal2", "pedestal3", "pedestal4", "pedestal5", "pedestal6", "pedestal7", "pedestal8", "pedestal9", "pedestal10");
            Random rn = new Random();

            DamageSource sourceE = new EntityDamageSource(list.get(rn.nextInt(10)),fakePlayer);
            float damage = getAttackDamage(getEntityFromList,itemInPedestal,coinInPedestal);

            if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.field_235397_ng_))
            {
                damage *= 2.0f;
            }

            if(getTargetEntity(world,posOfPedestal,getEntityFromList) != null)
            {
                getTargetEntity(world,posOfPedestal,getEntityFromList).attackEntityFrom(sourceE,damage);
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();
        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.func_240699_a_(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        area.func_240699_a_(TextFormatting.WHITE);
        player.sendMessage(area,player.getUniqueID());

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.func_240702_b_("" + getMostlyDamage(pedestal) + "");
        rate.func_240699_a_(TextFormatting.GRAY);
        player.sendMessage(rate,player.getUniqueID());

        TranslationTextComponent entityType = new TranslationTextComponent(getTranslationKey() + ".chat_entity");
        entityType.func_240702_b_(getTargetEntity(pedestal.getWorld(),pedestal.getPos()));
        entityType.func_240699_a_(TextFormatting.YELLOW);
        player.sendMessage(entityType,player.getUniqueID());

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

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.func_240702_b_("" + (int)((getCapacityModifier(stack) + 2.0F)) + "");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        area.func_240699_a_(TextFormatting.WHITE);
        rate.func_240699_a_(TextFormatting.GRAY);
        speed.func_240699_a_(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item ATTACK = new ItemUpgradeAttacker(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/attack"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ATTACK);
    }


}
