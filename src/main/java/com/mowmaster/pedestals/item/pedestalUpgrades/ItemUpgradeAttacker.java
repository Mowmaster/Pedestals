package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.util.PedestalFakePlayer;
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
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeAttacker extends ItemUpgradeBase
{
    public ItemUpgradeAttacker(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    //For damage
    @Override
    public boolean canAcceptCapacity() {return true;}

    @Override
    public boolean canAcceptArea() {
        return true;
    }

    @Override
    public boolean canAcceptRange() {
        return true;
    }

    @Override
    public boolean canAcceptMagnet() {
        return true;
    }

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

    public float getSwordDamage(LivingEntity entityIn, ItemStack toolInPedestal)
    {
        float damage = 2.0f;

        //By defalut accounts for any enchants that add attack damage (like sharpness)
        Multimap<Attribute, AttributeModifier> attributes = toolInPedestal.getItem().getAttributeModifiers(EquipmentSlotType.MAINHAND,toolInPedestal);
        if(attributes.containsKey(Attributes.ATTACK_DAMAGE))
        {
            if(attributes.get(Attributes.ATTACK_DAMAGE).size()>0)
            {
                AttributeModifier collected = attributes.get(Attributes.ATTACK_DAMAGE).iterator().next();
                if(collected.getAmount() > damage)damage += collected.getAmount();
            }

        }

        //Adds damage for enchants on the tool that effect mobs (vanilla enchants supported only)
        if(entityIn instanceof SpiderEntity && EnchantmentHelper.getEnchantments(toolInPedestal).containsKey(Enchantments.BANE_OF_ARTHROPODS))
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS,toolInPedestal);
            damage += 1+(lvl*0.5);
        }
        if(entityIn instanceof ZombieEntity || entityIn instanceof SkeletonEntity && EnchantmentHelper.getEnchantments(toolInPedestal).containsKey(Enchantments.SMITE))
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.SMITE,toolInPedestal);
            damage += 1+(lvl*0.5);
        }

        return  damage;
    }

    public float getAttackDamage(LivingEntity entityIn, ItemStack toolInPedestal, ItemStack coinInPedestal)
    {
        float damage = getCapacityModifier(coinInPedestal)*2 + 2.0F;
        damage += getSwordDamage(entityIn,toolInPedestal);
        return damage;
    }

    public float getMostlyDamage(PedestalTileEntity pedestal)
    {
        ItemStack inPedestal = pedestal.getToolOnPedestal();
        float damage = getCapacityModifier(pedestal.getCoinOnPedestal()) + 2.0f;
        float damage2 = 2.0f;
        if(inPedestal.getItem() instanceof SwordItem){
            SwordItem sword = (SwordItem)inPedestal.getItem();
            if(sword.getAttackDamage() > damage2){
                damage += (sword.getAttackDamage()/2);
            }
        }
        else if(inPedestal.getItem() instanceof ToolItem){
            ToolItem tool = (ToolItem)inPedestal.getItem();
            if(tool.getAttackDamage() > damage2){
                damage += (tool.getAttackDamage()/2);
            }
        }
        if(EnchantmentHelper.getEnchantments(inPedestal).containsKey(Enchantments.SHARPNESS))
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS,inPedestal);
            damage += Math.round((1+(lvl*0.5))/2);
        }

        return damage;
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

                int width = getAreaWidth(coinInPedestal);
                int height = getRangeHeight(coinInPedestal);
                if(hasMagnetEnchant(coinInPedestal))
                {
                    BlockPos negBlockPos = getNegRangePosEntity(world,pedestalPos,width,height);
                    BlockPos posBlockPos = getPosRangePosEntity(world,pedestalPos,width,height);
                    AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);
                    List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);
                    if(itemList.size()>0)
                    {
                        upgradeActionMagnet(pedestal, world, itemList, itemInPedestal, pedestalPos);
                    }
                }

                int speed = getOperationSpeed(coinInPedestal);
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
            List<String> list = Arrays.asList("pedestal1", "pedestal2", "pedestal3", "pedestal4", "pedestal5", "pedestal6", "pedestal7", "pedestal8", "pedestal9", "pedestal10", "pedestal11", "pedestal12");
            Random rn = new Random();

            LivingEntity selectedEntity = getTargetEntity(filterBlock,getEntityFromList);

            if(selectedEntity != null)
            {
                FakePlayer fakePlayer = fakePedestalPlayer(pedestal).get();
                if(!fakePlayer.getPosition().equals(new BlockPos(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ()))) {fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());}
                if (pedestal.hasTool() && !fakePlayer.getHeldItemMainhand().equals(toolInPedestal)) {fakePlayer.setHeldItem(Hand.MAIN_HAND, toolInPedestal);}
                if (toolInPedestal.isEmpty() && !fakePlayer.getHeldItemMainhand().equals(toolInPedestal)) {fakePlayer.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);}
                //Using the custom pedestal one this should work fine now...
                DamageSource sourceE = new EntityDamageSource(list.get(rn.nextInt(list.size())),fakePlayer);
                //DamageSource sourceE = (selectedEntity instanceof AbstractRaiderEntity && ((AbstractRaiderEntity) selectedEntity).isLeader())?(new EntityDamageSource(list.get(rn.nextInt(list.size())),null)):(new EntityDamageSource(list.get(rn.nextInt(list.size())),fakePlayer));
                float damage = getAttackDamage(getEntityFromList,toolInPedestal,coinInPedestal);

                if(filterBlock.equals(Blocks.NETHERITE_BLOCK)) {damage *= 2.0f;}

                selectedEntity.attackEntityFrom(sourceE,damage);
            }
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

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString("" + getMostlyDamage(pedestal) + "");
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

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

    public static final Item ATTACK = new ItemUpgradeAttacker(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/attack"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ATTACK);
    }


}
