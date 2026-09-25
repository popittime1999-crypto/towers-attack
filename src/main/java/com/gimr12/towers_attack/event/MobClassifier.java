package com.gimr12.towers_attack.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;

import java.util.Set;

/**
 * Classifies a killed entity into a MobClass for XP (GP) reward purposes.
 * See GPEventHandler for how each class's modifier is applied.
 */
public class MobClassifier {

    public enum MobClass {
        PASSIVE,
        NORMAL,
        ELITE,
        BOSS
    }

    // Tougher-than-normal mobs that aren't full bosses but clearly deserve more
    // than a standard kill. Not requested explicitly - added as a suggestion,
    // easy to trim or extend.
    private static final Set<EntityType<?>> ELITE_TYPES = Set.of(
            EntityType.ELDER_GUARDIAN,
            EntityType.WARDEN,
            EntityType.RAVAGER,
            EntityType.EVOKER,
            EntityType.BREEZE
    );

    private static final Set<EntityType<?>> BOSS_TYPES = Set.of(
            EntityType.WITHER,
            EntityType.ENDER_DRAGON
    );

    public static MobClass classify(LivingEntity entity) {
        EntityType<?> type = entity.getType();

        if (BOSS_TYPES.contains(type)) {
            return MobClass.BOSS;
        }
        if (ELITE_TYPES.contains(type)) {
            return MobClass.ELITE;
        }
        // Anything that isn't vanilla's own MONSTER category (animals, ambient
        // mobs, water creatures, golems/villagers, etc.) counts as passive.
        if (type.getCategory() != MobCategory.MONSTER) {
            return MobClass.PASSIVE;
        }
        return MobClass.NORMAL;
    }

    public static boolean isBoss(LivingEntity entity) {
        return BOSS_TYPES.contains(entity.getType());
    }
}
