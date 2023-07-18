package com.mowmaster.pedestals.eventhandlers;


import com.mowmaster.pedestals.items.tools.linking.LinkingTool;
import com.mowmaster.pedestals.items.tools.linking.LinkingToolBackwards;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;


@Mod.EventBusSubscriber
public class RoundRobinCrafting
{
    @SubscribeEvent()
    public static void RoundRobinUpgrade(PlayerInteractEvent.RightClickBlock event)
    {
        //Added to keep fake players from canning this every time?
        if(!(event.getEntity() instanceof FakePlayer))
        {
            Level worldIn = event.getLevel();
            InteractionHand hand = event.getHand();
            Player player = event.getEntity();

            int posX = event.getPos().getX();
            int posY = event.getPos().getY();
            int posZ = event.getPos().getZ();

            int paper=0;

            if(!worldIn.isClientSide) {
                player.getItemInHand(hand);
                if (player.getItemInHand(hand).getItem() instanceof LinkingTool || player.getItemInHand(hand).getItem() instanceof LinkingToolBackwards) {
                    //List<EntityItem> item = player.level.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(posX-1, posY-1, posZ-1, posX+1, posY+1, posZ+1));
                    List<ItemEntity> items = player.level.getEntitiesOfClass(ItemEntity.class, new AABB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
                    List<Parrot> parrotsList = player.level.getEntitiesOfClass(Parrot.class, new AABB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
                    //Tyler489 approved!
                    List<Chicken> cockList = player.level.getEntitiesOfClass(Chicken.class, new AABB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
                    List<Sheep> sheepList = player.level.getEntitiesOfClass(Sheep.class, new AABB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));


                    if (parrotsList.size() > 0 || cockList.size() > 0 && !(sheepList.size() > 0)) {
                        for (ItemEntity item : items) {
                            ItemStack stack = item.getItem();

                            if (stack.getItem().equals(Items.PAPER)) {
                                paper += stack.getCount();
                                item.remove(Entity.RemovalReason.DISCARDED);
                            }
                        }

                        if (paper > 0) {
                            worldIn.explode(new ItemEntity(worldIn, posX, posY, posZ, new ItemStack(Items.PAPER)), null, new EntityBasedExplosionDamageCalculator(player), posX + 0.5, posY + 2.0, posZ + 0.25, 0.0F, false, Explosion.BlockInteraction.NONE);
                            //NEED TO ADD ANOTHER TAG TO ITEM TO MAKE IT NOT USEABLE IN COMBINING AGAIN!!!
                            ItemStack stacked = new ItemStack(DeferredRegisterItems.AUGMENT_PEDESTAL_ROUNDROBIN.get(), paper);

                            ItemEntity itemEn = new ItemEntity(worldIn, posX, posY + 1, posZ, stacked);
                            itemEn.setInvulnerable(true);
                            worldIn.addFreshEntity(itemEn);
                        }
                    }
                }
            }
        }
    }
}
