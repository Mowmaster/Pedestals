package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.mowlib.Recipes.BaseBlockEntityFilter;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardArea;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeAttacker extends ItemUpgradeBase
{
    public ItemUpgradeAttacker(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyDamageCapacity(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyRange(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyArea(ItemStack upgradeItemStack) {
        return PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get();
    }

    @Override
    public boolean needsWorkCard(ItemStack upgradeItemStack) { return true; }

    @Override
    public int getWorkCardType() { return 1; }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_attacker_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_attacker_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_attacker_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_attacker_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_attacker_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_attacker_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_attacker_dustColor.get(),PedestalConfig.COMMON.upgrade_attacker_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_attacker_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_attacker_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_attacker_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_attacker_selectedMultiplier.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {

        List<String> messages = super.getUpgradeHUD(pedestal);

        if(messages.size()<=0)
        {
            if(baseEnergyCostPerDistance()>0)
            {
                if(pedestal.getStoredEnergy()<baseEnergyCostPerDistance())
                {
                    messages.add(ChatFormatting.RED + "Needs Energy");
                    messages.add(ChatFormatting.RED + "To Operate");
                }
            }
            if(baseXpCostPerDistance()>0)
            {
                if(pedestal.getStoredExperience()<baseXpCostPerDistance())
                {
                    messages.add(ChatFormatting.GREEN + "Needs Experience");
                    messages.add(ChatFormatting.GREEN + "To Operate");
                }
            }
            if(baseDustCostPerDistance().getDustAmount()>0)
            {
                if(pedestal.getStoredEnergy()<baseEnergyCostPerDistance())
                {
                    messages.add(ChatFormatting.LIGHT_PURPLE + "Needs Dust");
                    messages.add(ChatFormatting.LIGHT_PURPLE + "To Operate");
                }
            }
            if(PedestalConfig.COMMON.attacker_RequireTools.get())
            {
                if(pedestal.getActualToolStack().isEmpty())
                {
                    messages.add(ChatFormatting.BLACK + "Needs Tool");
                }
            }
            if(PedestalConfig.COMMON.attacker_DamageTools.get())
            {

                if(pedestal.hasTool() && pedestal.getDurabilityRemainingOnInsertedTool()<=1)
                {
                    messages.add(ChatFormatting.BLACK + "Inserted Tool");
                    messages.add(ChatFormatting.RED + "Is Broken");
                }
            }
        }

        return messages;
    }

    @Override
    public ItemStack getUpgradeDefaultTool() {
        if(PedestalConfig.COMMON.attacker_RequireTools.get())
        {
            return ItemStack.EMPTY;
        }
        return new ItemStack(Items.STICK);
    }

    @Nullable
    protected BaseBlockEntityFilter getRecipeFilterBlock(Level level, ItemStack stackIn) {
        Container container = MowLibContainerUtils.getContainer(1);
        container.setItem(-1,stackIn);
        List<BaseBlockEntityFilter> recipes = level.getRecipeManager().getRecipesFor(BaseBlockEntityFilter.Type.INSTANCE,container,level);
        return level != null ? (recipes.size() > 0)?(recipes.stream().findFirst().get()):(null) : null;
    }

    protected String getProcessResultFilterBlock(BaseBlockEntityFilter recipe) {
        return (recipe == null)?(""):(recipe.getResultEntityString());
    }

    protected int getProcessResultFilterBlockMobType(BaseBlockEntityFilter recipe) {
        return (recipe == null)?(1):(recipe.getResultMobType());
    }

    protected boolean getProcessResultFilterBlockIsBaby(BaseBlockEntityFilter recipe) {
        return (recipe == null)?(false):(recipe.getResultBaby());
    }

    public boolean hasBaseBlock(ItemStack coinInPedestal)
    {
        CompoundTag tag = new CompoundTag();
        if(coinInPedestal.hasTag())tag = coinInPedestal.getTag();
        ItemStack getBaseBlockList = MowLibCompoundTagUtils.readItemStackFromNBT(MODID, tag, "_baseblockStack");
        if(!getBaseBlockList.isEmpty())
        {
            String entityString = MowLibCompoundTagUtils.readStringFromNBT(MODID, coinInPedestal.getTag(), "_entityString");

            if(entityString != "")
            {
                return true;
            }
        }

        return false;
    }

    public boolean allowEntity(ItemStack coinInPedestal, Entity entityIn)
    {
        ItemStack baseBlock = MowLibCompoundTagUtils.readItemStackFromNBT(MODID, coinInPedestal.getTag(), "_baseBlockStack");
        if(!baseBlock.isEmpty())
        {
            String entityString = MowLibCompoundTagUtils.readStringFromNBT(MODID, coinInPedestal.getTag(), "_entityString");
            if(entityString != "")
            {
                int entityTypeNum = MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coinInPedestal.getTag(), "_entityType");
                boolean isBaby = MowLibCompoundTagUtils.readBooleanFromNBT(MODID, coinInPedestal.getTag(), "_entityIsBaby");
                if(entityTypeNum==0)
                {
                    if(entityIn.getClassification(false).equals(MobCategory.byName(entityString)))
                    {
                        if(isBaby && entityIn instanceof LivingEntity)
                        {
                            LivingEntity entity = ((LivingEntity)entityIn);
                            if (entity.isBaby())
                            {
                                return true;
                            }

                            return false;
                        }

                        return true;
                    }
                }
                else if(entityTypeNum==1)
                {
                    if(!EntityType.byString(entityString).isPresent())
                    {
                        //System.out.println(entityString +" is Not a Valid Entity Type");
                        return false;
                    }
                    else if(entityIn.getType().equals(EntityType.byString(entityString).get()))
                    {
                        if(isBaby && entityIn instanceof LivingEntity)
                        {
                            LivingEntity entity = ((LivingEntity)entityIn);
                            if (entity.isBaby())
                            {
                                return true;
                            }

                            return false;
                        }

                        return true;
                    }
                }

                return false;
            }
        }

        return true;
    }

    public float getToolDamage(ItemStack stack)
    {
        if(stack.getItem() instanceof SwordItem sward)
        {
            return sward.getDamage();
        }
        else if(stack.getItem() instanceof DiggerItem digdug)
        {
            return digdug.getAttackDamage();
        }
        else if(stack.getItem() instanceof Tier tiered)
        {
            return tiered.getAttackDamageBonus();
        }

        return 0.0F;
    }


    //Used a custom one since the normal one was returning 0.208 for damage...
    //ToDo: make Modification for damage delt
    public void doAttack(BasePedestalBlockEntity pedestal, Player player, Entity toAttack) {
        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(player, toAttack)) return;
        if (toAttack.isAttackable()) {
            if (!toAttack.skipAttackInteraction(player)) {
                float damageFloat = 1.0F + (getToolDamage(pedestal.getToolStack()));
                float f1;
                if (toAttack instanceof LivingEntity) {
                    f1 = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), ((LivingEntity)toAttack).getMobType());
                } else {
                    f1 = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED);
                }

                damageFloat += f1;

                if (damageFloat > 0.0F || f1 > 0.0F) {
                    boolean flag = true;
                    boolean flag1 = false;
                    float i = (float)player.getAttributeValue(Attributes.ATTACK_KNOCKBACK); // Forge: Initialize player value to the attack knockback attribute of the player, which is by default 0
                    i += EnchantmentHelper.getKnockbackBonus(player);
                    if (player.isSprinting() && flag) {
                        player.level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
                        ++i;
                        flag1 = true;
                    }

                    boolean flag2 = flag && player.fallDistance > 0.0F && !player.isOnGround() && !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger() && toAttack instanceof LivingEntity;
                    flag2 = flag2 && !player.isSprinting();
                    net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks.getCriticalHit(player, toAttack, flag2, flag2 ? 1.5F : 1.0F);
                    flag2 = hitResult != null;
                    if (flag2) {
                        damageFloat *= hitResult.getDamageModifier();
                    }

                    damageFloat += f1;
                    boolean flag3 = false;
                    double d0 = (double)(player.walkDist - player.walkDistO);
                    if (flag && !flag2 && !flag1 && player.isOnGround() && d0 < (double)player.getSpeed()) {
                        ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);
                        flag3 = itemstack.canPerformAction(net.minecraftforge.common.ToolActions.SWORD_SWEEP);
                    }

                    float f4 = 0.0F;
                    boolean flag4 = false;
                    int j = EnchantmentHelper.getFireAspect(player);
                    if (toAttack instanceof LivingEntity) {
                        f4 = ((LivingEntity)toAttack).getHealth();
                        if (j > 0 && !toAttack.isOnFire()) {
                            flag4 = true;
                            toAttack.setSecondsOnFire(1);
                        }
                    }

                    Vec3 vec3 = toAttack.getDeltaMovement();
                    List<String> list = Arrays.asList("pedestal1", "pedestal2", "pedestal3", "pedestal4", "pedestal5", "pedestal6", "pedestal7", "pedestal8", "pedestal9", "pedestal10", "pedestal11", "pedestal12");
                    Random rn = new Random();
                    DamageSource source = new EntityDamageSource(list.get(rn.nextInt(list.size())), player);
                    boolean flag5 = toAttack.hurt(source, damageFloat+(getDamageCapacityIncrease(pedestal.getCoinOnPedestal()) * 1.0F));
                    if (flag5) {
                        if (i > 0) {
                            if (toAttack instanceof LivingEntity) {
                                ((LivingEntity)toAttack).knockback((double)((float)i * 0.5F), (double) Mth.sin(player.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(player.getYRot() * ((float)Math.PI / 180F))));
                            } else {
                                toAttack.push((double)(-Mth.sin(player.getYRot() * ((float)Math.PI / 180F)) * (float)i * 0.5F), 0.1D, (double)(Mth.cos(player.getYRot() * ((float)Math.PI / 180F)) * (float)i * 0.5F));
                            }

                            player.setDeltaMovement(player.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                            player.setSprinting(false);
                        }

                        if (flag3) {
                            float f3 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * damageFloat;

                            for(LivingEntity livingentity : player.level.getEntitiesOfClass(LivingEntity.class, player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, toAttack))) {
                                if (livingentity != player && livingentity != toAttack && !player.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand) || !((ArmorStand)livingentity).isMarker()) && player.canHit(livingentity, 0)) { // Original check was dist < 3, range is 3, so vanilla used padding=0
                                    livingentity.knockback((double)0.4F, (double)Mth.sin(player.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(player.getYRot() * ((float)Math.PI / 180F))));
                                    livingentity.hurt(DamageSource.playerAttack(player), f3+(getDamageCapacityIncrease(pedestal.getCoinOnPedestal()) * 0.5F));
                                }
                            }

                            player.level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                            player.sweepAttack();
                        }

                        if (toAttack instanceof ServerPlayer && toAttack.hurtMarked) {
                            ((ServerPlayer)toAttack).connection.send(new ClientboundSetEntityMotionPacket(toAttack));
                            toAttack.hurtMarked = false;
                            toAttack.setDeltaMovement(vec3);
                        }

                        if (flag2) {
                            player.level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
                            player.crit(toAttack);
                        }

                        if (!flag2 && !flag3) {
                            if (flag) {
                                player.level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 1.0F, 1.0F);
                            } else {
                                player.level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, player.getSoundSource(), 1.0F, 1.0F);
                            }
                        }

                        if (f1 > 0.0F) {
                            player.magicCrit(toAttack);
                        }

                        player.setLastHurtMob(toAttack);
                        if (toAttack instanceof LivingEntity) {
                            EnchantmentHelper.doPostHurtEffects((LivingEntity)toAttack, player);
                        }

                        EnchantmentHelper.doPostDamageEffects(player, toAttack);
                        ItemStack itemstack1 = player.getMainHandItem();
                        Entity entity = toAttack;
                        if (toAttack instanceof net.minecraftforge.entity.PartEntity) {
                            entity = ((net.minecraftforge.entity.PartEntity<?>) toAttack).getParent();
                        }

                        if (!player.level.isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = itemstack1.copy();
                            itemstack1.hurtEnemy((LivingEntity)entity, player);
                            if (itemstack1.isEmpty()) {
                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copy, InteractionHand.MAIN_HAND);
                                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (toAttack instanceof LivingEntity) {
                            float f5 = f4 - ((LivingEntity)toAttack).getHealth();
                            player.awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                            if (j > 0) {
                                toAttack.setSecondsOnFire(j * 4);
                            }

                            if (player.level instanceof ServerLevel && f5 > 2.0F) {
                                int k = (int)((double)f5 * 0.5D);
                                ((ServerLevel)player.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, toAttack.getX(), toAttack.getY(0.5D), toAttack.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        //player.causeFoodExhaustion(0.1F);
                    } else {
                        player.level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
                        if (flag4) {
                            toAttack.clearFire();
                        }
                    }
                }
                player.resetAttackStrengthTicker(); // FORGE: Moved from beginning of attack() so that getAttackStrengthScale() returns an accurate value during all attack events
            }
        }
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        attackerAction(level, pedestal, pedestalPos, coin);
    }

    public boolean allowRun(BasePedestalBlockEntity pedestal, boolean damage)
    {
        if(PedestalConfig.COMMON.attacker_RequireTools.get())
        {
            if(pedestal.hasTool())
            {
                if(damage)
                {
                    return pedestal.damageInsertedTool(1,true);
                }
                else return true;
            }
            else return false;
        }

        return true;
    }

    public void attackerAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
        if (fakePlayerReference != null && fakePlayerReference.get() != null) {
            FakePlayer fakePlayer = fakePlayerReference.get();
            ItemStack workCardItemStack = pedestal.getWorkCardInPedestal();
            if (workCardItemStack.getItem() instanceof WorkCardArea) {
                List<LivingEntity> entities = WorkCardArea.getEntitiesInRangeOfUpgrade(level, LivingEntity.class, workCardItemStack, pedestal);

                boolean damage = canDamageTool(level, pedestal, PedestalConfig.COMMON.attacker_DamageTools.get());
                boolean canRun = allowRun(pedestal, PedestalConfig.COMMON.attacker_DamageTools.get());

                if(removeFuelForAction(pedestal, 0, true))
                {
                    ItemStack toolStack = pedestal.getToolStack().copy();
                    fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, toolStack);

                    if(PedestalConfig.COMMON.attacker_DamageTools.get())
                    {
                        if(pedestal.hasTool())
                        {
                            if(pedestal.getDurabilityRemainingOnInsertedTool()>0)
                            {
                                if(pedestal.damageInsertedTool(1,true))
                                {
                                    damage = true;
                                }
                                else
                                {
                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                                    canRun = false;
                                }
                            }
                            else
                            {
                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                                canRun = false;
                            }
                        }
                    }

                    if(canRun)
                    {
                        for (LivingEntity getEntity : entities)
                        {
                            if(getEntity == null)continue;
                            //BlockPos getEntityPos = getEntity.getOnPos();

                            if(allowEntity(coin,getEntity))
                            {
                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),pedestalPos), false))
                                {
                                    doAttack(pedestal, fakePlayer, getEntity);
                                    if(damage)pedestal.damageInsertedTool(1,false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void actionOnAddedToPedestal(Player player, BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnAddedToPedestal(player, pedestal, coinInPedestal);
        Level level = pedestal.getLevel();
        CompoundTag tagCoin = new CompoundTag();
        if(coinInPedestal.hasTag()) { tagCoin = coinInPedestal.getTag(); }
        ItemStack getBaseBlock = getBaseBlock(pedestal);
        BaseBlockEntityFilter filter = getRecipeFilterBlock(level,getBaseBlock);
        tagCoin = MowLibCompoundTagUtils.writeItemStackToNBT(MODID, tagCoin, getBaseBlock, "_baseBlockStack");
        tagCoin = MowLibCompoundTagUtils.writeStringToNBT(MODID, tagCoin, getProcessResultFilterBlock(filter), "_entityString");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(MODID, tagCoin, getProcessResultFilterBlockMobType(filter), "_entityType");
        tagCoin = MowLibCompoundTagUtils.writeBooleanToNBT(MODID, tagCoin, getProcessResultFilterBlockIsBaby(filter), "_entityIsBaby");
        coinInPedestal.setTag(tagCoin);
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        //remove NBT saved on upgrade here
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_baseBlockStack");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_entityString");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_entityType");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_entityIsBaby");
    }

    public ItemStack getBaseBlock(BasePedestalBlockEntity pedestal)
    {
        return new ItemStack(pedestal.getLevel().getBlockState(getPosOfBlockBelow(pedestal.getLevel(),pedestal.getPos(),1)).getBlock().asItem());
    }

    @Override
    public void actionOnNeighborBelowChange(BasePedestalBlockEntity pedestal, BlockPos belowBlock) {
        Level level = pedestal.getLevel();
        CompoundTag tagCoin = new CompoundTag();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        if(coinInPedestal.hasTag()) { tagCoin = coinInPedestal.getTag(); }
        ItemStack getBaseBlock = getBaseBlock(pedestal);
        BaseBlockEntityFilter filter = getRecipeFilterBlock(level,getBaseBlock);
        tagCoin = MowLibCompoundTagUtils.writeItemStackToNBT(MODID, tagCoin, getBaseBlock, "_baseBlockStack");
        tagCoin = MowLibCompoundTagUtils.writeStringToNBT(MODID, tagCoin, getProcessResultFilterBlock(filter), "_entityString");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(MODID, tagCoin, getProcessResultFilterBlockMobType(filter), "_entityType");
        tagCoin = MowLibCompoundTagUtils.writeBooleanToNBT(MODID, tagCoin, getProcessResultFilterBlockIsBaby(filter), "_entityIsBaby");
        coinInPedestal.setTag(tagCoin);
    }
}
