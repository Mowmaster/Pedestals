package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Augments.IPedestalAugment;
import com.mowmaster.pedestals.Items.ISelectableArea;
import com.mowmaster.pedestals.Items.ISelectablePoints;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.IHasModeTypes;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class WorkCardBase extends Item implements IPedestalWorkCard
{

    public WorkCardBase(Properties p_41383_) {
        super(p_41383_);
    }

    public int getWorkCardType()
    {
        return -1;
    }

    //ToDo: Add to mowlib and remove from here
    public static void saveStringToNBT(ItemStack upgrade, String nbtTag, String string)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        compound.putString(MODID+nbtTag, string);
        upgrade.setTag(compound);
    }

    //ToDo: Add to mowlib and remove from here
    //returns true for an add, false for a remove.
    public static boolean addBlockPosToList(ItemStack upgrade, BlockPos posOfBlock)
    {
        List<BlockPos> currentList = readBlockPosListFromNBT(upgrade);
        if(currentList.contains(posOfBlock))
        {
            currentList.remove(posOfBlock);
            saveBlockPosListToNBT(upgrade,currentList);
            return false;
        }
        else
        {
            currentList.add(posOfBlock);
            saveBlockPosListToNBT(upgrade,currentList);
            return true;
        }
    }

    //ToDo: Add to mowlib and remove from here
    public static void saveBlockPosListToNBT(ItemStack upgrade, List<BlockPos> posListToSave)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        List<Integer> storedX = new ArrayList<Integer>();
        List<Integer> storedY = new ArrayList<Integer>();
        List<Integer> storedZ = new ArrayList<Integer>();

        for(int i=0;i<posListToSave.size();i++)
        {
            storedX.add(posListToSave.get(i).getX());
            storedY.add(posListToSave.get(i).getY());
            storedZ.add(posListToSave.get(i).getZ());
        }

        compound.putIntArray(MODID+"_intArrayXPos",storedX);
        compound.putIntArray(MODID+"_intArrayYPos",storedY);
        compound.putIntArray(MODID+"_intArrayZPos",storedZ);
        upgrade.setTag(compound);
    }

    //ToDo: Add to mowlib and remove from here
    public static List<BlockPos> readBlockPosListFromNBT(ItemStack upgrade) {
        List<BlockPos> posList = new ArrayList<>();
        if(upgrade.hasTag())
        {
            String tagX = MODID+"_intArrayXPos";
            String tagY = MODID+"_intArrayYPos";
            String tagZ = MODID+"_intArrayZPos";
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tagX) && upgrade.getTag().contains(tagY) && upgrade.getTag().contains(tagZ))
            {
                int[] storedIX = getCompound.getIntArray(tagX);
                int[] storedIY = getCompound.getIntArray(tagY);
                int[] storedIZ = getCompound.getIntArray(tagZ);

                for(int i=0;i<storedIX.length;i++)
                {
                    BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
                    posList.add(gotPos);
                }
            }
        }
        return posList;
    }

    public static void saveBlockPosListCustomToNBT(ItemStack upgrade, String tagGenericName, List<BlockPos> posListToSave)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        List<Integer> storedX = new ArrayList<Integer>();
        List<Integer> storedY = new ArrayList<Integer>();
        List<Integer> storedZ = new ArrayList<Integer>();

        for(int i=0;i<posListToSave.size();i++)
        {
            storedX.add(posListToSave.get(i).getX());
            storedY.add(posListToSave.get(i).getY());
            storedZ.add(posListToSave.get(i).getZ());
        }

        compound.putIntArray(MODID+tagGenericName+"_X",storedX);
        compound.putIntArray(MODID+tagGenericName+"_Y",storedY);
        compound.putIntArray(MODID+tagGenericName+"_Z",storedZ);
        upgrade.setTag(compound);
    }

    public static List<BlockPos> readBlockPosListCustomFromNBT(ItemStack upgrade, String tagGenericName) {
        List<BlockPos> posList = new ArrayList<>();
        if(upgrade.hasTag())
        {
            String tagX = MODID+tagGenericName+"_X";
            String tagY = MODID+tagGenericName+"_Y";
            String tagZ = MODID+tagGenericName+"_Z";
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tagX) && upgrade.getTag().contains(tagY) && upgrade.getTag().contains(tagZ))
            {
                int[] storedIX = getCompound.getIntArray(tagX);
                int[] storedIY = getCompound.getIntArray(tagY);
                int[] storedIZ = getCompound.getIntArray(tagZ);

                for(int i=0;i<storedIX.length;i++)
                {
                    BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
                    posList.add(gotPos);
                }
            }
        }
        return posList;
    }

    public void removeBlockListCustomNBTTags(ItemStack upgrade, String tagGenericName)
    {
        String tagX = MODID+tagGenericName+"_X";
        String tagY = MODID+tagGenericName+"_Y";
        String tagZ = MODID+tagGenericName+"_Z";
        CompoundTag getTags = upgrade.getTag();
        if(getTags.contains(tagX))getTags.remove(tagX);
        if(getTags.contains(tagY))getTags.remove(tagY);
        if(getTags.contains(tagZ))getTags.remove(tagZ);
        upgrade.setTag(getTags);
    }

    public boolean hasBlockListCustomNBTTags(ItemStack upgrade, String tagGenericName)
    {
        String tagX = MODID+tagGenericName+"_X";
        String tagY = MODID+tagGenericName+"_Y";
        String tagZ = MODID+tagGenericName+"_Z";
        CompoundTag getTags = upgrade.getTag();

        return getTags.contains(tagX) && getTags.contains(tagY) && getTags.contains(tagZ);
    }

    //ToDo: Add to mowlib and remove from here
    public static void saveBlockPosToNBT(ItemStack upgrade, int num, BlockPos posToSave)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        List<Integer> listed = new ArrayList<>();
        listed.add(posToSave.getX());
        listed.add(posToSave.getY());
        listed.add(posToSave.getZ());
        compound.putIntArray(MODID+"_upgrade_blockpos_"+num, listed);
        upgrade.setTag(compound);
    }

    //ToDo: Add to mowlib and remove from here
    public static BlockPos readBlockPosFromNBT(ItemStack upgrade, int num) {
        if(upgrade.hasTag())
        {
            String tag = MODID+"_upgrade_blockpos_"+num;
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tag))
            {
                int[] listed = getCompound.getIntArray(tag);
                if(listed.length>=3)return new BlockPos(listed[0],listed[1],listed[2]);
            }
        }
        return BlockPos.ZERO;
    }

    //ToDo: Add to mowlib and remove from here
    public static BlockPos getBlockPosOnUpgrade(ItemStack stack, int num) {

        return readBlockPosFromNBT(stack,num);
    }

    //ToDo: Add to mowlib and remove from here
    public boolean hasOneBlockPos(ItemStack stack) {
        return !readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO) || !readBlockPosFromNBT(stack,2).equals(BlockPos.ZERO);
    }


    public static BlockPos getExistingSingleBlockPos(ItemStack stack) {
        return (!readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO))?(readBlockPosFromNBT(stack,1)):(readBlockPosFromNBT(stack,2));
    }


    public boolean isNewBlockPosSmallerThanExisting(ItemStack stack, BlockPos posTwo) {
        BlockPos posOne = getExistingSingleBlockPos(stack);
        BlockPos toCompare = new BlockPos(Math.min(posOne.getX(), posTwo.getX()),Math.min(posOne.getY(), posTwo.getY()),Math.min(posOne.getZ(), posTwo.getZ()));

        return (posTwo.equals(toCompare))?(true):(false);
    }


    public boolean hasTwoPointsSelected(ItemStack stack)
    {
        return !readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO) && !readBlockPosFromNBT(stack,2).equals(BlockPos.ZERO);
    }

    public AABB getAABBonUpgrade(ItemStack stack) {
        if (stack.is(DeferredRegisterItems.WORKCARD_AREA.get()) && hasTwoPointsSelected(stack)) {
            return new AABB(readBlockPosFromNBT(stack, 1), readBlockPosFromNBT(stack, 2)).expandTowards(1D, 1D, 1D);
        } else {
            return new AABB(BlockPos.ZERO);
        }
    }

    //ToDo: Add to mowlib and remove from here

    public boolean isSelectionInRange(BasePedestalBlockEntity pedestalCurrent, BlockPos currentSelectedPoint)
    {
        int range = PedestalConfig.COMMON.upgrades_baseSelectionRange.get();
        if(pedestalCurrent.getCoinOnPedestal().getItem() instanceof ItemUpgradeBase upgrade)
        {
            range += upgrade.getRangeIncrease(pedestalCurrent.getCoinOnPedestal());
        }
        int x = currentSelectedPoint.getX();
        int y = currentSelectedPoint.getY();
        int z = currentSelectedPoint.getZ();
        int x1 = pedestalCurrent.getPos().getX();
        int y1 = pedestalCurrent.getPos().getY();
        int z1 = pedestalCurrent.getPos().getZ();
        int xF = Math.abs(Math.subtractExact(x,x1));
        int yF = Math.abs(Math.subtractExact(y,y1));
        int zF = Math.abs(Math.subtractExact(z,z1));

        if(xF>range || yF>range || zF>range)
        {
            return false;
        }
        else return true;
    }

    public boolean selectedAreaWithinRange(BasePedestalBlockEntity pedestal)
    {
        if(isSelectionInRange(pedestal, readBlockPosFromNBT(pedestal.getWorkCardInPedestal(),1)) && isSelectionInRange(pedestal, readBlockPosFromNBT(pedestal.getWorkCardInPedestal(),2)))
        {
            return true;
        }

        return false;
    }

    //ToDo: Add to mowlib and remove from here
    public boolean selectedPointWithinRange(BasePedestalBlockEntity pedestal, BlockPos posPoint)
    {
        if(isSelectionInRange(pedestal, posPoint))
        {
            return true;
        }

        return false;
    }

    public BlockPos getHigherByFacing(BlockPos atLocation, Direction facing)
    {
        //west north down +XYZ
        //east +YZ
        //south +XY
        //up +XZ
        BlockPos higherPos = atLocation;
        switch(facing)
        {
            case NORTH:
            default:
                higherPos = atLocation.offset(1,1,1);
                break;
            case EAST:
                higherPos = atLocation.offset(0,1,1);
                break;
            case SOUTH:
                higherPos = atLocation.offset(1,1,0);
                break;
            case UP:
                higherPos = atLocation.offset(1,0,1);
                break;
        }

        return higherPos;
    }

    public BlockPos getLowerByFacing(BlockPos atLocation, Direction facing)
    {
        //west north down +-0
        //east -x
        //south -z
        //up -y
        BlockPos lowerPos = atLocation;
        switch(facing)
        {
            case NORTH:
            default:
                break;
            case EAST:
                lowerPos = atLocation.offset(-1,0,0);
                break;
            case SOUTH:
                lowerPos = atLocation.offset(0,0,-1);
                break;
            case UP:
                lowerPos = atLocation.offset(0,-1,0);
                break;
        }

        return lowerPos;
    }

    /*
    INTERACTIONS

    MODE CHANGE:
    - (Offhand)Crouch Right Click = Mode change
    TYPE CHANGE:
    - (Offhand)Right Click = Type Change



    GET BLOCK POS FOR AREA:
    main hand crouch right click to start it and right click to end it???
     */
    public Direction getLastClickedDirectionFromUpgrade(ItemStack stack)
    {
        Direction dir = Direction.UP;
        if(stack.hasTag())
        {
            if(stack.getTag().contains(MODID + "_string_last_clicked_direction"))
            {
                String direction = stack.getTag().getString(MODID + "_string_last_clicked_direction");
                if(direction == "down")return Direction.DOWN;
                else if(direction == "up")return Direction.UP;
                else if(direction == "north")return Direction.NORTH;
                else if(direction == "south")return Direction.SOUTH;
                else if(direction == "west")return Direction.WEST;
                else if(direction == "east")return Direction.EAST;
            }
        }

        return dir;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        saveBlockPosToNBT(stack,10,context.getClickedPos());
        saveStringToNBT(stack,"_string_last_clicked_direction",context.getClickedFace().toString());
        return super.onItemUseFirst(stack, context);
    }

    //If toggled in config, this is the max allowed size of selectable area
    public int getUpgradeSelectableAreaSize(BasePedestalBlockEntity pedestal)
    {
        //For a default 3x3x3 area the value is 2
        int returner = 2;
        if(pedestal.getCoinOnPedestal().getItem() instanceof ItemUpgradeBase upgrade)
        {
            returner += upgrade.getAreaIncrease(pedestal.getCoinOnPedestal());
        }

        return returner;
    }

    public int getDistanceBetweenPoints(BlockPos pointOne, BlockPos posToCompare)
    {
        int x = pointOne.getX();
        int y = pointOne.getY();
        int z = pointOne.getZ();
        int x1 = posToCompare.getX();
        int y1 = posToCompare.getY();
        int z1 = posToCompare.getZ();
        int xF = Math.abs(Math.subtractExact(x,x1));
        int yF = Math.abs(Math.subtractExact(y,y1));
        int zF = Math.abs(Math.subtractExact(z,z1));

        return Math.max(Math.max(xF,yF),zF);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level level = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack itemInOffhand = player.getOffhandItem();
        HitResult result = player.pick(5,player.getEyeHeight(),false);
        BlockPos atLocation = readBlockPosFromNBT(itemInHand,10);
        Direction facing = getLastClickedDirectionFromUpgrade(itemInHand);

        if(itemInHand.getItem() instanceof WorkCardBase)
        {
            if(hand.equals(InteractionHand.MAIN_HAND) && !player.isShiftKeyDown() && itemInHand.getItem() instanceof WorkCardBase && itemInHand.getItem() instanceof ISelectablePoints)
            {
                if(result.getType().equals(HitResult.Type.BLOCK))
                {
                    boolean added = addBlockPosToList(itemInHand,atLocation);
                    player.setItemInHand(hand,itemInHand);
                    MowLibMessageUtils.messagePopup(player,(added)?(ChatFormatting.WHITE):(ChatFormatting.BLACK),(added)?(MODID + ".upgrade_blockpos_added"):(MODID + ".upgrade_blockpos_removed"));
                    MowLibPacketHandler.sendToNearby(p_41432_,player.getOnPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,atLocation.getX(),atLocation.getY()+1.0D,atLocation.getZ(),0,(added)?(200):(0),0));

                }
                else if(result.getType().equals(HitResult.Type.MISS) && readBlockPosListFromNBT(itemInHand).size()>0)
                {
                    itemInHand.setTag(new CompoundTag());
                    //saveBlockPosListToNBT(itemInHand, new ArrayList<>());
                    MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_clear");
                }
            }

            if(hand.equals(InteractionHand.MAIN_HAND) && player.isShiftKeyDown() && itemInHand.getItem() instanceof WorkCardBase && itemInHand.getItem() instanceof ISelectableArea)
            {
                if(result.getType().equals(HitResult.Type.BLOCK))
                {

                    Boolean hasOnePointAlready = hasOneBlockPos(itemInHand);
                    Boolean hasTwoPointsAlready = hasTwoPointsSelected(itemInHand);

                    if(hasOnePointAlready && !hasTwoPointsAlready)
                    {
                        if(PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get())
                        {
                            //Currently no limit to work area selection, will need some way to show it on an upgrade though
                            saveBlockPosToNBT(itemInHand,2,atLocation);
                            player.setItemInHand(hand,itemInHand);
                            MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_second");

                            /*if(getDistanceBetweenPoints(readBlockPosFromNBT(itemInHand,1),atLocation) <= getUpgradeSelectableAreaSize(itemInHand))
                            {
                                saveBlockPosToNBT(itemInHand,2,atLocation);
                                player.setItemInHand(hand,itemInHand);
                                MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_second");
                            }
                            else
                            {
                                MowLibMessageUtils.messagePopup(player,ChatFormatting.RED,MODID + ".upgrade_blockpos_point_out_of_range");
                            }*/
                        }
                        else
                        {
                            saveBlockPosToNBT(itemInHand,2,atLocation);
                            player.setItemInHand(hand,itemInHand);
                            MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_second");
                        }
                    }
                    else if(!hasTwoPointsAlready)
                    {
                        saveBlockPosToNBT(itemInHand,1,atLocation);
                        player.setItemInHand(hand,itemInHand);
                        MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_first");
                    }
                }
                else if(result.getType().equals(HitResult.Type.MISS) && hasOneBlockPos(itemInHand))
                {
                    itemInHand.setTag(new CompoundTag());
                    //saveBlockPosToNBT(itemInHand,1,BlockPos.ZERO);
                    //saveBlockPosToNBT(itemInHand,2,BlockPos.ZERO);
                    MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_clear");
                }
            }
        }

        return InteractionResultHolder.fail(p_41433_.getItemInHand(p_41434_));
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        /*=====================================
        =======================================
        =====================================*/

        if(Screen.hasShiftDown() && Screen.hasAltDown())
        {
            //Add a new Line if both are present
            p_41423_.add(Component.literal(""));
        }

        if(p_41421_.getItem() instanceof WorkCardBase && p_41421_.getItem() instanceof ISelectableArea)
        {
            if(hasOneBlockPos(p_41421_))
            {
                if (!Screen.hasShiftDown()) {
                    MutableComponent base = Component.translatable(MODID + ".upgrade_description_shift");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".upgrade_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".tooltip_separator");
                    p_41423_.add(separator);

                    MutableComponent posOne = Component.translatable(MODID + ".upgrade_tooltip_blockpos_one");
                    BlockPos blockPosOne = readBlockPosFromNBT(p_41421_,1);
                    MutableComponent posOnePos = Component.literal(blockPosOne.getX() + "x " + blockPosOne.getY() + "y " + blockPosOne.getZ()+ "z");
                    posOnePos.withStyle(ChatFormatting.GRAY);
                    posOne.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                    posOne.append(posOnePos);

                    p_41423_.add(posOne);

                    MutableComponent posTwo = Component.translatable(MODID + ".upgrade_tooltip_blockpos_two");
                    BlockPos blockPosTwo = readBlockPosFromNBT(p_41421_,2);
                    MutableComponent posTwoPos = Component.literal(blockPosTwo.getX() + "x " + blockPosTwo.getY() + "y " + blockPosTwo.getZ()+ "z");
                    posTwoPos.withStyle(ChatFormatting.GRAY);
                    posTwo.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                    posTwo.append(posTwoPos);
                    p_41423_.add(posTwo);
                }
            }
        }

        if(p_41421_.getItem() instanceof WorkCardBase && p_41421_.getItem() instanceof ISelectablePoints && !hasTwoPointsSelected(p_41421_))
        {
            List<BlockPos> getList = readBlockPosListFromNBT(p_41421_);
            if(getList.size()>0)
            {
                if (!Screen.hasShiftDown()) {
                    MutableComponent base = Component.translatable(MODID + ".upgrade_description_shift");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".upgrade_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".tooltip_separator");
                    p_41423_.add(separator);

                    for (BlockPos pos : getList) {
                        MutableComponent posOnePos = Component.literal(pos.getX() + "x " + pos.getY() + "y " + pos.getZ()+ "z");
                        posOnePos.withStyle(ChatFormatting.GRAY);
                        p_41423_.add(posOnePos);
                    }
                }
            }
        }
    }
}
