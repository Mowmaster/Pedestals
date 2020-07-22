package com.mowmaster.pedestals.item.pedestalUpgrades;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEffect extends ItemUpgradeBaseMachine
{
    public ItemUpgradeEffect(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    public int getRangeWidth(ItemStack stack)
    {
        int rangeWidth = 0;
        int rW = getRangeModifier(stack);
        rangeWidth = ((rW)+1);
        return  rangeWidth;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        int speed = getOperationSpeed(coinInPedestal);
        if(!world.isBlockPowered(pedestalPos))
        {
            if (tick%speed == 0) {
                upgradeAction(world, itemInPedestal, coinInPedestal, pedestalPos);
            }
        }
    }

    public List<EffectInstance> getEffectFromPedestal(ItemStack itemInPedestal,int modifier)
    {
        List<EffectInstance> effectInstance = new ArrayList<>();
        if(!itemInPedestal.isEmpty() && PotionUtils.getEffectsFromStack(itemInPedestal).size() > 0)
        {
            for(int i=0; i<PotionUtils.getEffectsFromStack(itemInPedestal).size(); i++)
            {
                if(!PotionUtils.getEffectsFromStack(itemInPedestal).get(i).getPotion().isInstant())
                {
                    Effect getEffect = PotionUtils.getEffectsFromStack(itemInPedestal).get(0).getPotion();
                    int getAmp = PotionUtils.getEffectsFromStack(itemInPedestal).get(0).getAmplifier() * modifier;
                    int getDuration = PotionUtils.getEffectsFromStack(itemInPedestal).get(0).getDuration() * modifier;

                    effectInstance.add(new EffectInstance(getEffect,getDuration,getAmp));
                }
            }
        }
        return effectInstance;
    }

    public boolean hasPotionEffect(LivingEntity entityIn, List<EffectInstance> effectIn)
    {
        if(entityIn instanceof LivingEntity)
        {
            if(effectIn.size() > 0)
            {
                for(int i = 0;i < effectIn.size(); i++)
                {
                    if(entityIn.getActivePotionEffect(effectIn.get(i).getPotion()) != null)
                    {
                        if(entityIn.getActivePotionEffect(effectIn.get(i).getPotion()).getAmplifier() >= effectIn.get(i).getAmplifier() && entityIn.getActivePotionEffect(effectIn.get(i).getPotion()).getDuration() >= 100)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public Block getBaseBlockBelow(World world, BlockPos pedestalPos)
    {
        Block block = world.getBlockState(getPosOfBlockBelow(world,pedestalPos,1)).getBlock();
        ItemStack stack = new ItemStack((Item)BLOCK_TO_ITEM.getOrDefault(block, Items.AIR));

        //Netherite
        if(block.equals(Blocks.field_235397_ng_)) return Blocks.field_235397_ng_;
        if(stack.getItem().getTags().toString().contains("forge:storage_blocks/emerald")) return Blocks.EMERALD_BLOCK;//Players
        if(stack.getItem().getTags().toString().contains("forge:storage_blocks/diamond")) return Blocks.DIAMOND_BLOCK;//All Mobs
        if(stack.getItem().getTags().toString().contains("forge:storage_blocks/gold")) return Blocks.GOLD_BLOCK;//All Animals
        if(stack.getItem().getTags().toString().contains("forge:storage_blocks/iron")) return Blocks.IRON_BLOCK;//All Creatures

        return block;
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int width = getRangeWidth(coinInPedestal);
        int height = (2*width)+1;
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

        //Before doing anything, check for a proper item
        if(getEffectFromPedestal(itemInPedestal,1).size() > 0)
        {
            List<LivingEntity> itemList = world.getEntitiesWithinAABB(LivingEntity.class,getBox);
            for(LivingEntity getEntityFromList : itemList)
            {
                List<EffectInstance> instance = getEffectFromPedestal(itemInPedestal,1);

                if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.field_235397_ng_))
                {
                    instance = getEffectFromPedestal(itemInPedestal,2);
                }

                if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.EMERALD_BLOCK))
                {
                    if(getEntityFromList instanceof PlayerEntity && !hasPotionEffect(getEntityFromList,instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            getEntityFromList.addPotionEffect(instance.get(i));
                        }
                    }
                }
                else if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.DIAMOND_BLOCK))
                {
                    if(getEntityFromList instanceof MonsterEntity && !hasPotionEffect(getEntityFromList,instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            getEntityFromList.addPotionEffect(instance.get(i));
                        }
                    }
                }
                else if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.GOLD_BLOCK))
                {
                    if(getEntityFromList instanceof AnimalEntity && !hasPotionEffect(getEntityFromList,instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            getEntityFromList.addPotionEffect(instance.get(i));
                        }
                    }
                }
                else if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.IRON_BLOCK))
                {
                    if(getEntityFromList instanceof CreatureEntity && !hasPotionEffect(getEntityFromList,instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            getEntityFromList.addPotionEffect(instance.get(i));
                        }
                    }
                }
                else
                {
                    if(!hasPotionEffect(getEntityFromList,instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            getEntityFromList.addPotionEffect(instance.get(i));
                        }
                    }
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int s3 = getRangeWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        area.func_240699_a_(TextFormatting.WHITE);
        speed.func_240699_a_(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(speed);
    }

    public static final Item EFFECT = new ItemUpgradeEffect(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/effect"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(EFFECT);
    }


}
