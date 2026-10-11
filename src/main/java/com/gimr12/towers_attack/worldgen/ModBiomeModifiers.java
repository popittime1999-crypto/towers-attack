package com.gimr12.towers_attack.worldgen;

import com.gimr12.towers_attack.entity.ModEntities;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class ModBiomeModifiers {

    public static final ResourceKey<BiomeModifier> ADD_TREE_PEACH = registerKey("add_tree_peach");
    public static final ResourceKey<BiomeModifier> SPAWN_GOBLIN = registerKey("spawn_goblin");


    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        context.register(ADD_TREE_PEACH, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.FLOWER_FOREST), biomes.getOrThrow(Biomes.FOREST), biomes.getOrThrow(Biomes.PLAINS)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.PEACH_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(SPAWN_GOBLIN, new BiomeModifiers.AddSpawnsBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.FLOWER_FOREST), biomes.getOrThrow(Biomes.FOREST), biomes.getOrThrow(Biomes.PLAINS)),
                List.of(new MobSpawnSettings.SpawnerData(ModEntities.GOBLIN.get(), 20, 2, 4))));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, name));
    }
}
