package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
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
import java.util.*;

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
        List<EffectInstance> effectInstance = PotionUtils.getEffectsFromStack(itemInPedestal);
        List<EffectInstance> effectInstanceReturner = new ArrayList<>();
        if(!itemInPedestal.isEmpty() && effectInstance.size() > 0)
        {
            for(int i=0; i<effectInstance.size(); i++)
            {
                if(!effectInstance.get(i).getPotion().isInstant())
                {
                    Effect getEffect = PotionUtils.getEffectsFromStack(itemInPedestal).get(i).getPotion();
                    int getAmp = PotionUtils.getEffectsFromStack(itemInPedestal).get(i).getAmplifier() * modifier;
                    int getDuration = PotionUtils.getEffectsFromStack(itemInPedestal).get(i).getDuration() * modifier;

                    effectInstanceReturner.add(new EffectInstance(getEffect,getDuration,getAmp,true,true));
                }
            }
        }
        return effectInstanceReturner;
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

        List<EffectInstance> instance = getEffectFromPedestal(itemInPedestal,1);
        if(instance.size() > 0)
        {
            List<LivingEntity> entityList = world.getEntitiesWithinAABB(LivingEntity.class,getBox);
            for(LivingEntity getEntityFromList : entityList)
            {
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
                            if(getEntityFromList.addPotionEffect(instance.get(i)))
                            {
                                if(removeFuel(world,posOfPedestal,1,true) > -1)
                                {
                                    removeFuel(world,posOfPedestal,1,false);
                                }
                                else
                                {
                                    removeFromPedestal(world,posOfPedestal,1);
                                }
                            }
                        }
                    }
                }
                else if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.DIAMOND_BLOCK))
                {
                    if(getEntityFromList instanceof MonsterEntity && !hasPotionEffect(getEntityFromList,instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            if(getEntityFromList.addPotionEffect(instance.get(i)))
                            {
                                if(removeFuel(world,posOfPedestal,1,true) > -1)
                                {
                                    removeFuel(world,posOfPedestal,1,false);
                                }
                                else
                                {
                                    removeFromPedestal(world,posOfPedestal,1);
                                }
                            }
                        }
                    }
                }
                else if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.GOLD_BLOCK))
                {
                    if(getEntityFromList instanceof AnimalEntity && !hasPotionEffect(getEntityFromList,instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            if(getEntityFromList.addPotionEffect(instance.get(i)))
                            {
                                if(removeFuel(world,posOfPedestal,1,true) > -1)
                                {
                                    removeFuel(world,posOfPedestal,1,false);
                                }
                                else
                                {
                                    removeFromPedestal(world,posOfPedestal,1);
                                }
                            }
                        }
                    }
                }
                else if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.IRON_BLOCK))
                {
                    if(getEntityFromList instanceof CreatureEntity && !hasPotionEffect(getEntityFromList,instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            if(getEntityFromList.addPotionEffect(instance.get(i)))
                            {
                                if(removeFuel(world,posOfPedestal,1,true) > -1)
                                {
                                    removeFuel(world,posOfPedestal,1,false);
                                }
                                else
                                {
                                    removeFromPedestal(world,posOfPedestal,1);
                                }
                            }
                        }
                    }
                }
                else
                {
                    if(!hasPotionEffect(getEntityFromList,instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            if(getEntityFromList.addPotionEffect(instance.get(i)))
                            {
                                if(removeFuel(world,posOfPedestal,1,true) > -1)
                                {
                                    removeFuel(world,posOfPedestal,1,false);
                                }
                                else
                                {
                                    removeFromPedestal(world,posOfPedestal,1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            if(getItemStack.getItem().equals(Items.BLAZE_POWDER))
            {
                int CurrentBurnTime = tilePedestal.getStoredValueForUpgrades();
                int getBurnTimeForStack = 4 * getItemStack.getCount();
                tilePedestal.setStoredValueForUpgrades(CurrentBurnTime + getBurnTimeForStack);

                entityIn.remove();
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(TilePedestal pedestal, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            int fuelValue = pedestal.getStoredValueForUpgrades();

            double d0 = (double)getPosOfBlockBelow(world,pos,-1 ).getX() + 0.55D - (double)(rand.nextFloat() * 0.1F);
            double d1 = (double)getPosOfBlockBelow(world,pos,-1 ).getY() + 0.0D - (double)(rand.nextFloat() * 0.1F);
            double d2 = (double)getPosOfBlockBelow(world,pos,-1 ).getZ() + 0.55D - (double)(rand.nextFloat() * 0.1F);
            double d3 = (double)(0.4F - (rand.nextFloat() + rand.nextFloat()) * 0.4F);

            if(fuelValue > 0)
            {
                world.addParticle(ParticleTypes.EFFECT, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.0D, (double)pos.getZ() + 0.5D,0, 0, 0);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();
        int s3 = getRangeWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        area.func_240699_a_(TextFormatting.WHITE);
        speed.func_240699_a_(TextFormatting.RED);

        player.sendMessage(area,player.getUniqueID());


        //Display Fuel Left
        int fuelLeft = pedestal.getStoredValueForUpgrades();
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.func_240702_b_("" + fuelLeft + "");
        fuel.func_240699_a_(TextFormatting.GREEN);
        player.sendMessage(fuel,player.getUniqueID());

        //Displays what effects are in pedestal
        List<EffectInstance> instance = getEffectFromPedestal(pedestal.getItemInPedestal(),1);
        TranslationTextComponent effect = new TranslationTextComponent(getTranslationKey() + ".chat_effect");
        effect.func_240699_a_(TextFormatting.AQUA);
        player.sendMessage(effect,player.getUniqueID());
        for(int i = 0; i < instance.size();i++)
        {
            TranslationTextComponent effects = new TranslationTextComponent(instance.get(i).getPotion().getDisplayName().getString());
            effects.func_240699_a_(TextFormatting.GRAY);
            player.sendMessage(effects,player.getUniqueID());
        }

        //Display Speed Last Like on Tooltips
        player.sendMessage(speed,player.getUniqueID());
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
