package com.gimr12.towers_attack.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    public static final FoodProperties PEACH = new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f)
            .alwaysEdible().effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 60), 0.2f).build();

}