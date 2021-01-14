package com.mowmaster.pedestals.item.pedestalUpgrades;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
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
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeFan extends ItemUpgradeBase
{
    public ItemUpgradeFan(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {return true;}

    public int getAreaWidth(ItemStack stack)
    {
        return  getAreaModifier(stack);
    }

    public int getHeight(ItemStack stack)
    {
        return getRangeMedium(stack);
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{getHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    protected void useFanOnEntities(PedestalTileEntity pedestal,Block filterBlock, double speed, AxisAlignedBB getBox) {
        World world = pedestal.getWorld();
        BlockPos posOfPedestal = pedestal.getPos();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        List<LivingEntity> entityList = world.getEntitiesWithinAABB(LivingEntity.class, getBox);
        if(entityList.size()==0)writeStoredIntToNBT(coinInPedestal,0);
        if(entityList.size()>0)
        {
            BlockState state = world.getBlockState(posOfPedestal);
            Direction enumfacing = state.get(FACING);
            for (LivingEntity entity : entityList) {
                LivingEntity getEntity = getTargetEntity(filterBlock,entity);
                if(getEntity != null)
                {
                    if(getEntity instanceof PlayerEntity)
                    {
                        if(!((PlayerEntity) getEntity).abilities.isFlying && !((PlayerEntity) getEntity).isCrouching())
                        {
                            addMotion(world,posOfPedestal,speed,enumfacing,getEntity);
                            writeStoredIntToNBT(coinInPedestal,1);
                        }
                    }
                    else
                    {
                        addMotion(world,posOfPedestal,speed,enumfacing,getEntity);
                        writeStoredIntToNBT(coinInPedestal,1);
                    }
                    if (enumfacing == Direction.UP) {
                        getEntity.fallDistance = 0;
                    }
                }
            }
        }
    }

    protected void useFanOnEntitiesAdvanced(PedestalTileEntity pedestal,Block filterBlock, double speed, AxisAlignedBB getBox) {
        World world = pedestal.getWorld();
        BlockPos posOfPedestal = pedestal.getPos();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        List<Entity> entityList = world.getEntitiesWithinAABB(Entity.class, getBox);
        if(entityList.size()==0)writeStoredIntToNBT(coinInPedestal,0);
        if(entityList.size()>0)
        {
            BlockState state = world.getBlockState(posOfPedestal);
            Direction enumfacing = state.get(FACING);
            for (Entity entity : entityList) {
                Entity getEntity = getTargetEntityAdvanced(filterBlock, entity);
                if(getEntity != null)
                {
                    if(getEntity instanceof PlayerEntity)
                    {
                        if(!((PlayerEntity) getEntity).abilities.isFlying && !((PlayerEntity) getEntity).isCrouching())
                        {
                            addMotionAdvanced(world,posOfPedestal,speed,enumfacing,getEntity);
                            writeStoredIntToNBT(coinInPedestal,1);
                        }
                    }
                    else
                    {
                        addMotionAdvanced(world,posOfPedestal,speed,enumfacing,getEntity);
                        writeStoredIntToNBT(coinInPedestal,1);
                    }
                    if (enumfacing == Direction.UP) {
                        getEntity.fallDistance = 0;
                    }
                }
            }
        }
    }

    protected void addMotion(World world, BlockPos posOfPedestal, double speed, Direction enumfacing, LivingEntity entity) {

        switch (enumfacing) {
            case DOWN:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y - speed, entity.getMotion().z);
                break;
            case UP:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y + speed, entity.getMotion().z);
                break;
            case NORTH:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y, entity.getMotion().z - speed);
                break;
            case SOUTH:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y, entity.getMotion().z + speed);
                break;
            case WEST:
                entity.setMotion(entity.getMotion().x - speed, entity.getMotion().y, entity.getMotion().z);
                break;
            case EAST:
                entity.setMotion(entity.getMotion().x + speed, entity.getMotion().y, entity.getMotion().z);
                break;
        }
    }

    protected void addMotionAdvanced(World world, BlockPos posOfPedestal, double speed, Direction enumfacing, Entity entity) {

        switch (enumfacing) {
            case DOWN:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y - speed, entity.getMotion().z);
                break;
            case UP:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y + speed, entity.getMotion().z);
                break;
            case NORTH:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y, entity.getMotion().z - speed);
                break;
            case SOUTH:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y, entity.getMotion().z + speed);
                break;
            case WEST:
                entity.setMotion(entity.getMotion().x - speed, entity.getMotion().y, entity.getMotion().z);
                break;
            case EAST:
                entity.setMotion(entity.getMotion().x + speed, entity.getMotion().y, entity.getMotion().z);
                break;
        }
    }

    public double getFanSpeed(ItemStack stack)
    {
        double intOperationalSpeed = 0.25;
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                intOperationalSpeed = 0.1;//normal speed
                break;
            case 1:
                intOperationalSpeed=0.2;//2x faster
                break;
            case 2:
                intOperationalSpeed = 0.4;//4x faster
                break;
            case 3:
                intOperationalSpeed = 0.6;//6x faster
                break;
            case 4:
                intOperationalSpeed = 1.0;//10x faster
                break;
            case 5:
                intOperationalSpeed=2.0;//20x faster
                break;
            default: intOperationalSpeed=0.1;
        }

        return  intOperationalSpeed;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isBlockPowered(pedestalPos))
        {
            upgradeAction(pedestal);
            if(readStoredIntFromNBT(coinInPedestal) > 0)
            {
                int speedSound = getOperationSpeed(coinInPedestal);
                if (world.getGameTime()%speedSound == 0) {
                    world.playSound((PlayerEntity) null, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.BLOCKS, 0.25F, 1.0F);
                }
            }
        }
    }


    public void upgradeAction(PedestalTileEntity pedestal)
    {
        /*
        THIS F'ING FAN NEEDS BOTH CLIENT AND SERVER FOR PLAYER CLIENT TO UPDATE (VISUALLY)
        IF SOMETHING DOESNT WORK, MAKE SURE BOTH THE CLIENT AND SERVER CAN ACCESS THE SAME SORT OF INFO
        USE THE filterBlock AS AN EXAMPLE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         */
        World world = pedestal.getWorld();
        BlockPos posOfPedestal = pedestal.getPos();
        ItemStack coin = pedestal.getCoinOnPedestal();
        int width = getAreaWidth(coin);
        int height = getHeight(coin);
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);
        double speed = getFanSpeed(coin);
        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);
        if(!hasFilterBlock(coin)) {writeFilterBlockToNBT(pedestal);}
        Block filterBlock = (!world.isRemote)?(readFilterBlockFromNBT(coin)):(world.getBlockState(getPosOfBlockBelow(world,posOfPedestal,1)).getBlock());

        if(filterBlock.equals(Blocks.NETHERITE_BLOCK)) {speed *= 2;}
        if(hasAdvancedInventoryTargeting(coin)) {useFanOnEntitiesAdvanced(pedestal,filterBlock,speed,getBox);}
        else {useFanOnEntities(pedestal, filterBlock,speed,getBox);}
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

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        int s3 = getAreaWidth(stack);
        int s4 = getHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("" + s4 + "");
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
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
        if(hasAdvancedInventoryTargeting(stack))
        {
            entityType.appendString(getTargetEntityAdvanced(filterBlock));
        }
        else
        {
            entityType.appendString(getTargetEntity(filterBlock));
        }
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
        int s4 = getHeight(stack);

        String tr = "" + (s3+s3+1) + "";
        String trr = "" + (s4) + "";

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);

        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item FAN = new ItemUpgradeFan(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fan"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FAN);
    }

}
