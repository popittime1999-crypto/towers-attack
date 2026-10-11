package com.gimr12.towers_attack.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    public static final FoodProperties PEACH = new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f).build();

    public static final FoodProperties GOLD_PEACH = new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 240, 2), 1f)
            .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 240, 3), 1f)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 240, 2), 1f).build();
}