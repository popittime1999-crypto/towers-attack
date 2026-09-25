package com.gimr12.towers_attack.leveling;

import com.gimr12.towers_attack.towers_attack;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class StatAttributes {

    private static ResourceLocation modifierId(StatType type) {
        return ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, type.name().toLowerCase() + "_stat_bonus");
    }

    public static double perPointValue(StatType type) {
        return switch (type) {
            case STRENGTH -> 0.1;    // Attack Damage
            case HP -> 1.0;          // Max Health
            case STAMINA -> 0.005;   // Movement Speed
            case DEF -> 0.5;        // Armor
            case LUCK -> 0.1;        // Luck
        };
    }

    /** Public so other systems (e.g. titles) can reuse the same attribute mapping instead of duplicating it. */
    public static Holder<Attribute> vanillaAttribute(StatType type) {
        return switch (type) {
            case STRENGTH -> Attributes.ATTACK_DAMAGE;
            case HP -> Attributes.MAX_HEALTH;
            case STAMINA -> Attributes.MOVEMENT_SPEED;
            case DEF -> Attributes.ARMOR;
            case LUCK -> Attributes.LUCK;
        };
    }

    public static void apply(Player player, StatType type, int points) {
        AttributeInstance instance = player.getAttribute(vanillaAttribute(type));
        if (instance == null) return; // shouldn't happen for players, but don't crash if it does

        ResourceLocation id = modifierId(type);
        double amount = points * perPointValue(type);

        if (amount == 0) {
            instance.removeModifier(id);
        } else {
            instance.addOrReplacePermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void applyAll(Player player) {
        for (StatType type : StatType.values()) {
            apply(player, type, PlayerStats.getStat(player, type));
        }
    }
}
