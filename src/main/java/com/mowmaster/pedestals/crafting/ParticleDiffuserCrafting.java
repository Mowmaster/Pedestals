package com.mowmaster.pedestals.crafting;

import com.mowmaster.pedestals.item.ItemLinkingTool;
import com.mowmaster.pedestals.item.augments.ItemPedestalUpgrades;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;
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
        if(!(event.getPlayer() instanceof FakePlayer))
        {
            World worldIn = event.getWorld();
            Hand hand = event.getHand();
            BlockState state = worldIn.getBlockState(event.getPos());
            PlayerEntity player = event.getPlayer();
            BlockPos pos = event.getPos();

            int posX = event.getPos().getX();
            int posY = event.getPos().getY();
            int posZ = event.getPos().getZ();

            int paper=0;

            if(!worldIn.isRemote) {
                if ((player.getHeldItem(hand) != null)) {
                    if (player.getHeldItem(hand).getItem() instanceof ItemLinkingTool) {
                        //List<EntityItem> item = player.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(posX-1, posY-1, posZ-1, posX+1, posY+1, posZ+1));
                        List<ItemEntity> items = player.getEntityWorld().getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
                        List<SheepEntity> sheepList = player.getEntityWorld().getEntitiesWithinAABB(SheepEntity.class, new AxisAlignedBB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
                        List<ParrotEntity> parrotsList = player.getEntityWorld().getEntitiesWithinAABB(ParrotEntity.class, new AxisAlignedBB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
                        List<ChickenEntity> cockList = player.getEntityWorld().getEntitiesWithinAABB(ChickenEntity.class, new AxisAlignedBB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));

                        if(worldIn.isRaining() && player.isWet() && !(sheepList.size()>0) && !(parrotsList.size()>0) && !(cockList.size()>0))
                        {
                            for (ItemEntity item : items) {
                                ItemStack stack = item.getItem();

                                if(stack.getItem().equals(Items.PAPER))
                                {
                                    paper +=stack.getCount();
                                    item.remove();
                                }
                            }

                            if(paper > 0)
                            {
                                worldIn.createExplosion(new ItemEntity(worldIn, posX, posY, posZ),(DamageSource)null,(ExplosionContext)null, posX + 0.5, posY + 2.0, posZ + 0.25, 0.0F,false, Explosion.Mode.NONE);

                                /*float rainValue = (worldIn.rainingStrength>1f)?(worldIn.rainingStrength):(1f);
                                if((rainValue-(float) paper)>0)worldIn.setRainStrength(rainValue);
                                else worldIn.getWorldInfo().setRaining(false);*/

                                //WIll make number of upgrades = to amount of paper available
                                ItemEntity itemEn = new ItemEntity(worldIn,posX,posY+1,posZ,new ItemStack(ItemPedestalUpgrades.PARTICLEDIFFUSER,paper));
                                itemEn.setInvulnerable(true);
                                worldIn.addEntity(itemEn);
                                worldIn.getWorldInfo().setRaining(false);
                                paper=0;

                                if(paper>0)
                                {
                                    ItemEntity itemEn2 = new ItemEntity(worldIn,posX,posY+1,posZ,new ItemStack(Items.PAPER,paper));
                                    itemEn2.setInvulnerable(true);
                                    worldIn.addEntity(itemEn2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}