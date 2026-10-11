package com.gimr12.towers_attack.entity;

import com.gimr12.towers_attack.entity.custom.GoblinEntity;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, towers_attack.MOD_ID);

    public static final Supplier<EntityType<GoblinEntity>> GOBLIN =
            ENTITY_TYPES.register("goblin", () -> EntityType.Builder.of(GoblinEntity::new, MobCategory.CREATURE)
                    .sized(0.75f, 2f).build("goblin"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}