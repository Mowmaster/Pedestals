package com.mowmaster.pedestals.item;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeTool extends Item {

    private static final BlockPos defaultPos = new BlockPos(0,-2000,0);
    public BlockPos storedPosition = defaultPos;

    public ItemUpgradeTool() {
        super(new Properties().maxStackSize(1).containerItem(UPGRADE).group(PEDESTALS_TAB));
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(this.getItem());
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World worldIn = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getPos();

        if(!worldIn.isRemote)
        {
            BlockState getBlockState = worldIn.getBlockState(pos);
            if(player.isCrouching())
            {
                if(getBlockState.getBlock() instanceof BlockPedestalTE) {
                    TileEntity tile = worldIn.getTileEntity(pos);
                    if(tile instanceof PedestalTileEntity)
                    {
                        PedestalTileEntity ped = ((PedestalTileEntity)worldIn.getTileEntity(pos));
                        if(ped.hasCoin())
                        {
                            ItemStack coin = ped.getCoinOnPedestal();
                            if(coin.getItem() instanceof ItemUpgradeBase)
                            {
                                int workX = ((ItemUpgradeBase)coin.getItem()).getWorkAreaX(worldIn,pos,coin);
                                int workY = ((ItemUpgradeBase)coin.getItem()).getWorkAreaY(worldIn,pos,coin)[0];
                                int workZ = ((ItemUpgradeBase)coin.getItem()).getWorkAreaZ(worldIn,pos,coin);
                                int workSingle = ((ItemUpgradeBase)coin.getItem()).getWorkAreaY(worldIn,pos,coin)[1];
                                if(workX+workY+workZ > 0)
                                {
                                    //System.out.println("X: "+workX);
                                    //System.out.println("Y: "+workY);
                                    //System.out.println("Z: "+workZ);
                                    if (player.getHeldItemMainhand().isEnchanted() == false) {
                                        //Gets Pedestal Clicked on Pos
                                        //System.out.println(pos);
                                        this.storedPosition = pos;
                                        //System.out.println(storedPosition);
                                        //Writes to NBT
                                        writePosToNBT(player.getHeldItemMainhand());
                                        writeWorkPosToNBT(player.getHeldItemMainhand(),workX,workY,workZ,workSingle);
                                        //Applies effect to wrench in hand
                                        if (player.getHeldItemMainhand().getItem() instanceof ItemUpgradeTool) {
                                            player.getHeldItemMainhand().addEnchantment(Enchantments.UNBREAKING, -1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    this.storedPosition = defaultPos;
                    writePosToNBT(player.getHeldItemMainhand());
                    writeWorkPosToNBT(player.getHeldItemMainhand(),0,0,0,0);
                    if(player.getHeldItemMainhand().getItem() instanceof ItemUpgradeTool)
                    {
                        if(player.getHeldItemMainhand().getItem() instanceof ItemUpgradeTool)
                        {
                            if(player.getHeldItemMainhand().isEnchanted())
                            {
                                Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                EnchantmentHelper.setEnchantments(enchantsNone,player.getHeldItemMainhand());
                            }
                        }
                    }
                }
            }
            else
            {
                if(worldIn.getBlockState(pos).getBlock() instanceof BlockPedestalTE) {
                    //Checks Tile at location to make sure its a PedestalTileEntity
                    TileEntity tileEntity = worldIn.getTileEntity(pos);
                    if (tileEntity instanceof PedestalTileEntity) {
                        PedestalTileEntity tilePedestal = (PedestalTileEntity) tileEntity;
                        if(tilePedestal.hasCoin())
                        {
                            Item coinInPed = tilePedestal.getCoinOnPedestal().getItem();
                            if(coinInPed instanceof ItemUpgradeBase)
                            {
                                ((ItemUpgradeBase) coinInPed).chatDetails(player, tilePedestal);
                            }
                        }
                    }
                }
            }
        }

        return super.onItemUse(context);
    }

    int ticker=0;

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        //super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

        if(entityIn instanceof PlayerEntity) {

            PlayerEntity player = ((PlayerEntity) entityIn);
            if (stack.isEnchanted() && isSelected || player.getHeldItemOffhand().getItem() instanceof ItemUpgradeTool)
            {
                if (stack.hasTag()) {
                    this.getPosFromNBT(stack);
                    BlockPos pos = this.getStoredPosition(stack);
                    int[] getWorkArea = this.getWorkPosFromNBT(stack);
                    Random rand = new Random();

                    BlockPos negNums = pos;
                    BlockPos posNums = pos;

                    if(worldIn.isAreaLoaded(pos,1))
                    {
                        if(worldIn.getTileEntity(pos) instanceof PedestalTileEntity)
                        {
                            negNums = getNegRangePos(worldIn,pos,getWorkArea[0],getWorkArea[1]);
                            posNums = getPosRangePos(worldIn,pos,getWorkArea[0],getWorkArea[1]);
                        }
                    }

                    if(storedPosition!=defaultPos)
                    {
                        if(isSelected)
                        {
                            //System.out.println("Tick: "+pos);
                            if(worldIn.isRemote)
                            {
                                ticker++;

                                if(getWorkArea[3]<=0)
                                {
                                    if(ticker>30)
                                    {
                                        //Test to see what location is stored in the wrench System.out.println(this.getStoredPosition(stack));
                                        for (int x = negNums.getX(); x <= posNums.getX(); x++) {
                                            for (int z = negNums.getZ(); z <= posNums.getZ(); z++) {
                                                for (int y = negNums.getY(); y <= posNums.getY(); y++) {
                                                    BlockPos blockToParti = new BlockPos(x, y, z);
                                                    worldIn.addParticle(ParticleTypes.ASH,true,blockToParti.getX()+0.5f,blockToParti.getY()+0.5f,blockToParti.getZ()+0.5f, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D);
                                                }
                                            }
                                        }
                                        ticker=0;
                                    }
                                }
                                else
                                {
                                    BlockPos blockToParti = new BlockPos(getWorkArea[0], getWorkArea[1], getWorkArea[2]);
                                    spawnParticleAroundPedestalBase(worldIn,ticker,blockToParti,0.0f,0.0f,0.0f,1.0f);
                                }

                            }
                        }
                    }

                }
            }
        }
    }

    public BlockPos getStoredPosition(ItemStack getWrenchItem)
    {
        getPosFromNBT(getWrenchItem);
        return storedPosition;
    }

    public void writePosToNBT(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }
        compound.putInt("stored_x",this.storedPosition.getX());
        compound.putInt("stored_y",this.storedPosition.getY());
        compound.putInt("stored_z",this.storedPosition.getZ());
        stack.setTag(compound);
    }

    public void writeWorkPosToNBT(ItemStack stack,int x, int y, int z, int single)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }
        compound.putInt("work_x",x);
        compound.putInt("work_y",y);
        compound.putInt("work_z",z);
        compound.putInt("work_single",single);
        stack.setTag(compound);
    }

    public void getPosFromNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            int x = getCompound.getInt("stored_x");
            int y = getCompound.getInt("stored_y");
            int z = getCompound.getInt("stored_z");
            this.storedPosition = new BlockPos(x,y,z);
        }
    }

    public int[] getWorkPosFromNBT(ItemStack stack)
    {
        int x=0;
        int y=0;
        int z=0;
        int single=0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            x = getCompound.getInt("work_x");
            y = getCompound.getInt("work_y");
            z = getCompound.getInt("work_z");
            single = getCompound.getInt("work_single");
        }
        return new int[]{x,y,z,single};
    }

    public BlockPos getNegRangePos(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(-intWidth,0,-intWidth);
            case DOWN:
                return blockBelow.add(-intWidth,-intHeight,-intWidth);
            case NORTH:
                return blockBelow.add(-intWidth,-intWidth,-intHeight);
            case SOUTH:
                return blockBelow.add(-intWidth,-intWidth,0);
            case EAST:
                return blockBelow.add(0,-intWidth,-intWidth);
            case WEST:
                return blockBelow.add(-intHeight,-intWidth,-intWidth);
            default:
                return blockBelow;
        }
    }

    public BlockPos getPosRangePos(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(intWidth,intHeight,intWidth);
            case DOWN:
                return blockBelow.add(intWidth,0,intWidth);
            case NORTH:
                return blockBelow.add(intWidth,intWidth,0);
            case SOUTH:
                return blockBelow.add(intWidth,intWidth,intHeight);
            case EAST:
                return blockBelow.add(intHeight,intWidth,intWidth);
            case WEST:
                return blockBelow.add(0,intWidth,intWidth);
            default:
                return blockBelow;
        }
    }

    public void spawnParticleAroundPedestalBase(World world,int tick, BlockPos pos, float r, float g, float b, float alpha)
    {
        double dx = (double)pos.getX();
        double dy = (double)pos.getY();
        double dz = (double)pos.getZ();

        BlockState state = world.getBlockState(pos);
        Direction enumfacing = Direction.UP;
        if(state.getBlock() instanceof BlockPedestalTE)
        {
            enumfacing = state.get(FACING);
        }
        RedstoneParticleData parti = new RedstoneParticleData(r, g, b, alpha);
        switch (enumfacing)
        {
            case UP:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.5D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.5D, dz+ 0.75D,0, 0, 0);
                return;
            case DOWN:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.5D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.5D, dz+ 0.75D,0, 0, 0);
                return;
            case NORTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+0.5D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+0.75D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+0.5D,0, 0, 0);
                return;
            case SOUTH:
                if (tick%20 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.75D, dz+0.5D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.25D, dz+0.75D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+0.5D,0, 0, 0);
                return;
            case EAST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.25D, dy+ 0.25D, dz+0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.5D, dy+ 0.25D, dz+0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.75D, dy+ 0.75D, dz+0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.5D, dy+ 0.75D, dz+0.75D,0, 0, 0);
                return;
            case WEST:
                if (tick%20 == 0) world.addParticle(parti, dx+0.25D, dy+0.25D, dz+ 0.25D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+0.5D, dy+0.25D, dz+ 0.75D,0, 0, 0);
                if (tick%15 == 0) world.addParticle(parti, dx+0.75D, dy+0.75D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+0.5D, dy+0.75D, dz+ 0.75D,0, 0, 0);
                return;
            default:
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.25D, dz+ 0.25D,0, 0, 0);
                if (tick%35 == 0) world.addParticle(parti, dx+ 0.25D, dy+0.5D, dz+ 0.75D,0, 0, 0);
                if (tick%25 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.75D, dz+ 0.25D,0, 0, 0);
                if (tick%30 == 0) world.addParticle(parti, dx+ 0.75D, dy+0.5D, dz+ 0.75D,0, 0, 0);
                return;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        //new TranslationTextComponent(getTranslationKey() + ".tool_speed", tilePedestal.getSpeed()).mergeStyle(TextFormatting.RED)
        TranslationTextComponent selected = new TranslationTextComponent(getTranslationKey() + ".tool_block_selected");
        TranslationTextComponent unselected = new TranslationTextComponent(getTranslationKey() + ".tool_block_unselected");
        TranslationTextComponent cordX = new TranslationTextComponent(getTranslationKey() + ".tool_X");
        TranslationTextComponent cordY = new TranslationTextComponent(getTranslationKey() + ".tool_Y");
        TranslationTextComponent cordZ = new TranslationTextComponent(getTranslationKey() + ".tool_Z");
        if(stack.getItem() instanceof ItemUpgradeTool) {
            if (stack.hasTag()) {
                if (stack.isEnchanted()) {
                    selected.appendString(cordX.getString());
                    selected.appendString("" + this.getStoredPosition(stack).getX() + "");
                    selected.appendString(cordY.getString());
                    selected.appendString("" + this.getStoredPosition(stack).getY() + "");
                    selected.appendString(cordZ.getString());
                    selected.appendString("" + this.getStoredPosition(stack).getZ() + "");
                    tooltip.add(selected);
                } else tooltip.add(unselected);
            } else tooltip.add(unselected);
        }
    }

    public static final Item UPGRADE = new ItemUpgradeTool().setRegistryName(new ResourceLocation(MODID, "upgradetool"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(UPGRADE);
    }




}
