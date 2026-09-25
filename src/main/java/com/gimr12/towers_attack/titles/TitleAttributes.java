package com.gimr12.towers_attack.titles;

import com.gimr12.towers_attack.leveling.StatAttributes;
import com.gimr12.towers_attack.leveling.StatType;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

/**
 * Converts a title's configured "stats" (from its JSON, using the same
 * strength/hp/stamina/def/luck keys as the player-allocated stats) into
 * real vanilla AttributeModifiers, using the SAME per-point conversion
 * factors and attribute mapping as StatAttributes - so title bonuses
 * always stay proportional to regular stat points, even if those get
 * rebalanced later. Nothing here duplicates a number StatAttributes
 * already owns.
 */
public final class TitleAttributes {
    private TitleAttributes() {}

    public static void apply(Player player, TitleDefinition title) {
        for (StatType type : StatType.values()) {
            String key = type.name().toLowerCase();

            AttributeInstance instance = player.getAttribute(StatAttributes.vanillaAttribute(type));
            if (instance == null) continue;

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "title_" + key);
            instance.removeModifier(id);
            if (title == null) continue;

            double configuredPoints = title.stats().getOrDefault(key, 0.0);
            if (configuredPoints == 0.0) continue;

            // A title's "stats" entry means "as if you had this many extra stat
            // points", so it's converted through the exact same per-point value
            // StatAttributes uses for player-allocated points.
            double amount = configuredPoints * StatAttributes.perPointValue(type);
            instance.addOrReplacePermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
