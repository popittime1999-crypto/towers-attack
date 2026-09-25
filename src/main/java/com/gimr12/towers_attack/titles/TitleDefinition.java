package com.gimr12.towers_attack.titles;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A data-driven title definition loaded from config/gimrstowerattack/titles/*.json. */
public record TitleDefinition(
        String id,
        String name,
        TitleRarity rarity,
        boolean commandOnly,
        int minimumPlayerLevel,
        double levelScaling,
        Map<String, Integer> killEntities,
        Map<String, Integer> killTags,
        Map<String, Double> stats,
        String description,
        Map<String, Integer> mineBlocks,
        Map<String, Integer> craftItems,
        Map<String, Integer> customStats,
        String color
) {
    public static TitleDefinition fromJson(String json, String fallbackId) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        String id = root.has("id") ? root.get("id").getAsString() : fallbackId;
        String name = root.has("name") ? root.get("name").getAsString() : id;
        TitleRarity rarity = TitleRarity.fromString(root.has("rarity") ? root.get("rarity").getAsString() : "common");
        String description = root.has("description") ? root.get("description").getAsString() : "";
        String color = root.has("color") ? root.get("color").getAsString() : "#FFFFFF";
        boolean commandOnly = root.has("command_only") && root.get("command_only").getAsBoolean();
        int minimumLevel = root.has("minimum_player_level") ? Math.max(1, root.get("minimum_player_level").getAsInt()) : 1;
        double levelScaling = root.has("level_scaling") ? Math.max(0.0, root.get("level_scaling").getAsDouble()) : 0.01;

        Map<String, Integer> killEntities = new LinkedHashMap<>();
        Map<String, Integer> killTags = new LinkedHashMap<>();
        Map<String, Double> stats = new LinkedHashMap<>();

        JsonObject requirements = root.has("requirements") && root.get("requirements").isJsonObject()
                ? root.getAsJsonObject("requirements") : new JsonObject();
        readIntMap(requirements, "kill_entity", killEntities);
        readIntMap(requirements, "kill_tag", killTags);
        Map<String, Integer> mineBlocks = new LinkedHashMap<>();
        Map<String, Integer> craftItems = new LinkedHashMap<>();
        Map<String, Integer> customStats = new LinkedHashMap<>();
        readIntMap(requirements, "mine_block", mineBlocks);
        readIntMap(requirements, "craft_item", craftItems);
        readIntMap(requirements, "custom_stat", customStats);

        if (root.has("stats") && root.get("stats").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("stats").entrySet()) {
                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                    stats.put(entry.getKey().toLowerCase(), entry.getValue().getAsDouble());
                }
            }
        }

        // Demi God and God are intentionally command-only, even if somebody forgets
        // command_only in a custom file. This keeps the two top rarities protected.
        if (rarity == TitleRarity.DEMI_GOD || rarity == TitleRarity.GOD) {
            commandOnly = true;
        }

        return new TitleDefinition(id, name, rarity, commandOnly, minimumLevel, levelScaling,
                Map.copyOf(killEntities), Map.copyOf(killTags), Map.copyOf(stats), description,
                Map.copyOf(mineBlocks), Map.copyOf(craftItems), Map.copyOf(customStats), color);
    }

    private static void readIntMap(JsonObject parent, String key, Map<String, Integer> output) {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) return;
        for (Map.Entry<String, JsonElement> entry : parent.getAsJsonObject(key).entrySet()) {
            if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                output.put(entry.getKey(), Math.max(1, entry.getValue().getAsInt()));
            }
        }
    }

    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("name", name);
        root.addProperty("rarity", rarity.name().toLowerCase());
        root.addProperty("command_only", commandOnly);
        root.addProperty("minimum_player_level", minimumPlayerLevel);
        root.addProperty("level_scaling", levelScaling);
        root.addProperty("description", description);
        root.addProperty("color", color);

        JsonObject requirements = new JsonObject();
        JsonObject entities = new JsonObject();
        killEntities.forEach(entities::addProperty);
        JsonObject tags = new JsonObject();
        killTags.forEach(tags::addProperty);
        requirements.add("kill_entity", entities);
        requirements.add("kill_tag", tags);
        JsonObject mine = new JsonObject();
        mineBlocks.forEach(mine::addProperty);
        requirements.add("mine_block", mine);
        JsonObject craft = new JsonObject();
        craftItems.forEach(craft::addProperty);
        requirements.add("craft_item", craft);
        JsonObject customStat = new JsonObject();
        customStats.forEach(customStat::addProperty);
        requirements.add("custom_stat", customStat);
        root.add("requirements", requirements);

        JsonObject statsObject = new JsonObject();
        stats.forEach(statsObject::addProperty);
        root.add("stats", statsObject);
        return root.toString();
    }

    public boolean hasRequirements() {
        return !killEntities.isEmpty() || !killTags.isEmpty()
                || !mineBlocks.isEmpty() || !craftItems.isEmpty() || !customStats.isEmpty();
    }

    /** Parses the "color" field (a "#RRGGBB" hex string) into a packed RGB int for TextColor.fromRgb(). */
    public int colorRgb() {
        try {
            String hex = color.startsWith("#") ? color.substring(1) : color;
            return Integer.parseInt(hex, 16);
        } catch (Exception ignored) {
            return 0xFFFFFF;
        }
    }

    public int requiredKills(int base, int playerLevel) {
        double multiplier = rarity.defaultDifficultyMultiplier() * (1.0 + levelScaling * Math.max(0, playerLevel - 1));
        return Math.max(base, (int) Math.ceil(base * multiplier));
    }

    public boolean matchesEntityId(LivingEntityRef entity) {
        return killEntities.containsKey(entity.id());
    }

    public record LivingEntityRef(String id) {}
}
