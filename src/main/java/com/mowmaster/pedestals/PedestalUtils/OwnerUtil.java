package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class OwnerUtil
{
    public static void removePlayerFromStack(ItemStack stack)
    {
        if(hasPlayerSet(stack))
        {
            CompoundTag compound = new CompoundTag();
            if(stack.hasTag())
            {
                compound = stack.getTag();
                if(compound.contains(MODID + "_player"))
                {
                    compound.remove(MODID + "_player");
                }

                if(compound.contains(MODID + "_playername"))
                {
                    compound.remove(MODID + "_playername");
                }

                stack.setTag(compound);
            }
        }
    }

    //
    //UUID HANDLING
    //

    public static UUID getPlayerFromStack(ItemStack stack)
    {
        if(hasPlayerSet(stack))
        {
            UUID playerID = readUUIDFromNBT(stack);
            if(playerID !=null)
            {
                return playerID;
            }
        }
        return Util.NIL_UUID;
    }

    public static void setPlayerStack(ItemStack stack, Player player)
    {
        writeUUIDToNBT(stack,player.getUUID());
        writeNameToStackNBT(stack,player.getDisplayName().getString());
    }

    public static boolean hasPlayerSet(ItemStack stack)
    {
        boolean returner = false;
        CompoundTag compound = new CompoundTag();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains(MODID + "_player"))
            {
                if(readUUIDFromNBT(stack) !=null)
                {
                    returner = true;
                }
            }
        }
        return returner;
    }

    public static void writeUUIDToNBT(ItemStack stack, UUID uuidIn)
    {
        CompoundTag compound = new CompoundTag();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putUUID(MODID + "_player",uuidIn);
        stack.setTag(compound);
    }

    public static UUID readUUIDFromNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundTag getCompound = stack.getTag();
            return getCompound.getUUID(MODID + "_player");
        }

        return null;
    }

    //
    //PLAYER NAME HANDLING
    //

    public static String getPlayerNameFromStack(ItemStack stack)
    {
        if(hasPlayerNameSet(stack))
        {
            return readNameFromStackNBT(stack);
        }
        return null;
    }

    public static boolean hasPlayerNameSet(ItemStack stack)
    {
        boolean returner = false;
        CompoundTag compound = new CompoundTag();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains(MODID + "_playername"))
            {
                if(readNameFromStackNBT(stack) !=null)
                {
                    returner = true;
                }
            }
        }
        return returner;
    }

    public static void writeNameToStackNBT(ItemStack stack, String name)
    {
        CompoundTag compound = new CompoundTag();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putString(MODID + "_playername",name);
        stack.setTag(compound);
    }

    public static String readNameFromStackNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundTag getCompound = stack.getTag();
            return getCompound.getString(MODID + "_playername");
        }

        return null;
    }

    public static CompoundTag writeNameToNBT(CompoundTag tag, String name)
    {
        CompoundTag compound = tag;

        compound.putString(MODID + "_playername",name);
        return compound;
    }

    public static String readNameFromNBT(CompoundTag tag)
    {
        if(tag.contains(MODID + "_playername"))
        {
            return tag.getString(MODID + "_playername");
        }

        return "";
    }
}
