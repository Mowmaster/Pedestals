package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IItemMode 
{
    ItemTransferMode getItemTransportMode(ItemStack stackIn);

    public static enum ItemTransferMode {
        ITEMS,
        FLUIDS,
        ENERGY,
        EXPERIENCE,
        DUST;

        private ItemTransferMode() { }

        public boolean modeItems() { return this == ITEMS; }

        public boolean modeFluids() {
            return this == FLUIDS;
        }

        public boolean modeEnergy() {
            return this == ENERGY;
        }

        public boolean modeExperience() {
            return this == EXPERIENCE;
        }

        public boolean modeDust() {
            return this == DUST;
        }

        public Component componentTransferMode()
        {
            if(this.modeItems())return Component.translatable(References.MODID + ".enum.filtertransfermode_item");
            if(this.modeFluids())return Component.translatable(References.MODID + ".enum.filtertransfermode_fluid");
            if(this.modeEnergy())return Component.translatable(References.MODID + ".enum.filtertransfermode_energy");
            if(this.modeExperience())return Component.translatable(References.MODID + ".enum.filtertransfermode_experience");
            if(this.modeDust())return Component.translatable(References.MODID + ".enum.filtertransfermode_dust");
            return Component.translatable(References.MODID + ".enum.filtertransfermode_default");
        }

        public String stringTransferMode()
        {
            switch (this)
            {
                case ITEMS: return "item";
                case FLUIDS: return "fluid";
                case ENERGY: return "energy";
                case EXPERIENCE: return "experience";
                case DUST: return "dust";
                default: return "default";
            }
        }

        public void iterateTransferMode(ItemStack stackIn)
        {
            int newValue = this.ordinal()+1;
            if(newValue >= ItemTransferMode.values().length)newValue=0;
            serializeTransferMode(stackIn, ItemTransferMode.values()[newValue]);
        }

        public void serializeTransferMode(ItemStack stackIn, ItemTransferMode modeToSet)
        {
            CompoundTag nbt = new CompoundTag();
            if(stackIn.hasTag()) nbt = stackIn.getTag();

            nbt.putInt(References.MODID + "_enum_itemtransfermode", modeToSet.ordinal());
            stackIn.setTag(nbt);
        }

        public void serializeTransferMode(ItemStack stackIn)
        {
            CompoundTag nbt = new CompoundTag();
            if(stackIn.hasTag()) nbt = stackIn.getTag();

            nbt.putInt(References.MODID + "_enum_itemtransfermode", this.ordinal());
            stackIn.setTag(nbt);
        }

        public ItemTransferMode getTransferModeFromStack(ItemStack stackIn)
        {
            if(stackIn.hasTag())
            {
                if(stackIn.getTag().contains(References.MODID + "_enum_itemtransfermode"))
                {
                    return ItemTransferMode.values()[stackIn.getTag().getInt(References.MODID + "_enum_itemtransfermode")];
                }
            }

            serializeTransferMode(stackIn, ITEMS);
            return ITEMS;
        }

        public static int getTransferModeIntFromStack(ItemStack stackIn)
        {
            if(stackIn.hasTag())
            {
                if(stackIn.getTag().contains(References.MODID + "_enum_itemtransfermode"))
                {
                    return stackIn.getTag().getInt(References.MODID + "_enum_itemtransfermode");
                }
            }

            return 0;
        }

        public ChatFormatting getModeColorFormat()
        {
            ChatFormatting color;
            switch (this)
            {
                case ITEMS: color = ChatFormatting.GOLD; break;
                case FLUIDS: color = ChatFormatting.BLUE; break;
                case ENERGY: color = ChatFormatting.RED; break;
                case EXPERIENCE: color = ChatFormatting.GREEN; break;
                case DUST: color = ChatFormatting.LIGHT_PURPLE; break;
                default: color = ChatFormatting.WHITE; break;
            }

            return color;
        }
    }
    
}
