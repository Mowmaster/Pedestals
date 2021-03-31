package com.mowmaster.pedestals.util;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by KingMowmaster on 7/19/2020.
 */
public class PedestalUtils {

    public static void spawnItemStackInWorld(World worldIn,BlockPos pos,ItemStack stack) {

        Random RANDOM = new Random();
        double d0 = (double) EntityType.ITEM.getWidth();
        double d1 = 1.0D - d0;
        double d2 = d0 / 2.0D;
        double d3 = Math.floor(pos.getX()) + RANDOM.nextDouble() * d1 + d2;
        double d4 = Math.floor(pos.getY()) + RANDOM.nextDouble() * d1;
        double d5 = Math.floor(pos.getZ()) + RANDOM.nextDouble() * d1 + d2;

        while(!stack.isEmpty()) {
            ItemEntity itementity = new ItemEntity(worldIn, d3, d4, d5, stack.split(RANDOM.nextInt(21) + 10));
            float f = 0.05F;
            itementity.setMotion(RANDOM.nextGaussian() * 0.05000000074505806D, RANDOM.nextGaussian() * 0.05000000074505806D + 0.20000000298023224D, RANDOM.nextGaussian() * 0.05000000074505806D);
            worldIn.addEntity(itementity);
        }
    }
}
