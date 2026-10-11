package com.gimr12.towers_attack.worldgen.tree;

import com.gimr12.towers_attack.towers_attack;
import com.gimr12.towers_attack.worldgen.ModConfiguredFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class ModTreeGrowers {

    public static final TreeGrower PEACHWOOD = new TreeGrower(towers_attack.MOD_ID + ":peachwood",
            Optional.empty(), Optional.of(ModConfiguredFeatures.PEACH_KEY), Optional.empty());
}
