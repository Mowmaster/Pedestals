package com.mowmaster.pedestals.EventHandlers;

import com.mowmaster.pedestals.Items.Tools.Linking.LinkingTool;
import com.mowmaster.pedestals.Items.Tools.Linking.LinkingToolBackwards;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;


@Mod.EventBusSubscriber
public class ParticleDiffuserCrafting
{
    @SubscribeEvent()
    public static void ParticleDiffuserUpgrade(PlayerInteractEvent.RightClickBlock event)
    {
        //Added to keep fake players from canning this every time?
        if(!(event.getEntity() instanceof FakePlayer))
        {
            Level worldIn = event.getLevel();
            InteractionHand hand = event.getHand();
            BlockState state = worldIn.getBlockState(event.getPos());
            Player player = event.getEntity();
            BlockPos pos = event.getPos();

            int posX = event.getPos().getX();
            int posY = event.getPos().getY();
            int posZ = event.getPos().getZ();

            int paper=0;

            if(!worldIn.isClientSide()) {
                if ((player.getItemInHand(hand) != null)) {
                    if (player.getItemInHand(hand).getItem() instanceof LinkingTool || player.getItemInHand(hand).getItem() instanceof LinkingToolBackwards) {
                        //List<EntityItem> item = player.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(posX-1, posY-1, posZ-1, posX+1, posY+1, posZ+1));
                        List<ItemEntity> items = player.level().getEntitiesOfClass(ItemEntity.class, new AABB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
                        List<Sheep> sheepList = player.level().getEntitiesOfClass(Sheep.class, new AABB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
                        List<Parrot> parrotsList = player.level().getEntitiesOfClass(Parrot.class, new AABB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
                        List<Chicken> cockList = player.level().getEntitiesOfClass(Chicken.class, new AABB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));

                        if(worldIn.isRaining() && player.isInWaterRainOrBubble() && !(sheepList.size()>0) && !(parrotsList.size()>0) && !(cockList.size()>0))
                        {
                            for (ItemEntity item : items) {
                                ItemStack stack = item.getItem();

                                if(stack.getItem().equals(Items.PAPER))
                                {
                                    paper +=stack.getCount();
                                    item.remove(Entity.RemovalReason.DISCARDED);
                                }
                            }

                            if(paper > 0)
                            {
                                worldIn.explode(new ItemEntity(worldIn, posX, posY, posZ,new ItemStack(Items.PAPER)),(DamageSource)null,new EntityBasedExplosionDamageCalculator(player), posX + 0.5, posY + 2.0, posZ + 0.25, 0.0F,false, Level.ExplosionInteraction.NONE);

                                /*float rainValue = (worldIn.rainingStrength>1f)?(worldIn.rainingStrength):(1f);
                                if((rainValue-(float) paper)>0)worldIn.setRainStrength(rainValue);
                                else worldIn.getWorldInfo().setRaining(false);*/


                                //WIll make number of upgrades = to amount of paper available
                                ItemEntity itemEn = new ItemEntity(worldIn,posX,posY+1,posZ,new ItemStack(DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get(),paper));
                                itemEn.setInvulnerable(true);
                                worldIn.addFreshEntity(itemEn);
                                worldIn.getLevelData().setRaining(false);
                                paper=0;

                                if(paper>0)
                                {
                                    ItemEntity itemEn2 = new ItemEntity(worldIn,posX,posY+1,posZ,new ItemStack(Items.PAPER,paper));
                                    itemEn2.setInvulnerable(true);
                                    worldIn.addFreshEntity(itemEn2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
