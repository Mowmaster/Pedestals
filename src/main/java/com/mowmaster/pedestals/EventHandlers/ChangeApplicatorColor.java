package com.mowmaster.pedestals.EventHandlers;

import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.MowLibUtils.ColorReference;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber
public class ChangeApplicatorColor {

    @SubscribeEvent()
    public static void ColorApply(PlayerInteractEvent.RightClickBlock event)
    {
        if(!(event.getPlayer() instanceof FakePlayer) && event.getPlayer().isCreative())
        {
            Level world = event.getWorld();
            Player player = event.getPlayer();
            InteractionHand hand = event.getHand();
            BlockPos pos = event.getPos();

            int red = 0;
            int green = 0;
            int blue = 0;
            int black = 0;


            List<Item> RED_DYE = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("forge", "dyes/red"))).stream().toList();
            List<Item> GREEN_DYE = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("forge", "dyes/green"))).stream().toList();
            List<Item> BLUE_DYE = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("forge", "dyes/blue"))).stream().toList();
            List<Item> BLACK_DYE = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("forge", "dyes/black"))).stream().toList();


            /*
            ResourceLocation grabRed = new ResourceLocation("forge", "dyes/red");
            ResourceLocation grabGreen = new ResourceLocation("forge", "dyes/green");
            ResourceLocation grabBlue = new ResourceLocation("forge", "dyes/blue");
            ResourceLocation grabBlack = new ResourceLocation("forge", "dyes/black");

            Tag<Item> RED_DYE = ItemTags.getAllTags().getTag(grabRed);
            Tag<Item> GREEN_DYE = ItemTags.getAllTags().getTag(grabGreen);
            Tag<Item> BLUE_DYE = ItemTags.getAllTags().getTag(grabBlue);
            Tag<Item> BLACK_DYE = ItemTags.getAllTags().getTag(grabBlack);

// This is to get the values of a tag that is bound to items :
ForgeRegistries.ITEMS.tags().getTag([THE TAG YOU WANT]).stream().toList(); // Returns a List<Item>
//                                           ^
//                                Should be a TagKey<Item>


// To get a tag that is bound to blocks :
ForgeRegistries.BLOCKS.tags().getTag([THE TAG YOU WANT]).stream().toList(); // Returns a List<Block>
//                                            ^
//                                Should be a TagKey<Block>

            */

            if(!world.isClientSide())
            {
                if(player.getItemInHand(hand).getItem() instanceof ColorApplicator)
                {
                    AABB aabb = (new AABB(pos)).inflate(3.0).expandTowards(0.0D, (double)world.getHeight(), 0.0D);
                    List<ItemEntity> list = world.getEntitiesOfClass(ItemEntity.class, aabb);

                    for (ItemEntity item : list) {
                        ItemStack stack = item.getItem();

                        if(RED_DYE.contains(stack.getItem()))
                        {
                            red+=stack.getCount();
                            item.remove(Entity.RemovalReason.DISCARDED);
                        }
                        else if(GREEN_DYE.contains(stack.getItem()))
                        {
                            green+=stack.getCount();
                            item.remove(Entity.RemovalReason.DISCARDED);
                        }
                        else if(BLUE_DYE.contains(stack.getItem()))
                        {
                            blue+=stack.getCount();
                            item.remove(Entity.RemovalReason.DISCARDED);
                        }
                        else if(BLACK_DYE.contains(stack.getItem()))
                        {
                            black+=stack.getCount();
                            item.remove(Entity.RemovalReason.DISCARDED);
                        }
                    }
                }
                if(red>3)red/=3;
                if(green>3)red/=3;
                if(blue>3)red/=3;

                List<Integer> list = ColorApplicator.getColorList(player.getItemInHand(hand));
                if(black>0)
                {
                    world.explode(player,null,null,pos.getX()+0.5,pos.getY()+2.0,pos.getZ()+0.5,0.0f,false, Explosion.BlockInteraction.NONE);
                    ItemStack newStack = ColorReference.addColorToItemStack(player.getItemInHand(hand).copy(),0,0,0);
                    ColorApplicator.saveColorList(newStack,ColorApplicator.addSavedColor(player.getItemInHand(hand),ColorReference.getColor(Arrays.asList(0,0,0))));
                    player.setItemInHand(hand,newStack);
                }
                else if(red>0 || green>0 || blue>0)
                {
                    world.explode(player,null,null,pos.getX()+0.5,pos.getY()+2.0,pos.getZ()+0.5,0.0f,false, Explosion.BlockInteraction.NONE);
                    ItemStack newStack = ColorReference.addColorToItemStack(player.getItemInHand(hand).copy(),red,green,blue);
                    ColorApplicator.saveColorList(newStack,ColorApplicator.addSavedColor(player.getItemInHand(hand),ColorReference.getColor(Arrays.asList(red,green,blue))));
                    player.setItemInHand(hand,newStack);
                }
            }

        }
    }
}
