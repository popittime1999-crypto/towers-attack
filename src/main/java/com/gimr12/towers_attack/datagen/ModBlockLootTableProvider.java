package com.gimr12.towers_attack.datagen;

import com.gimr12.towers_attack.block.ModBlocks;
import com.gimr12.towers_attack.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlocks.PEACH_LOG.get());
        this.dropSelf(ModBlocks.STRIPPED_PEACH_LOG.get());
        this.dropSelf(ModBlocks.PEACH_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_PEACH_WOOD.get());
        this.dropSelf(ModBlocks.PEACH_PLANKS.get());
        this.dropSelf(ModBlocks.PEACH_SAPLING.get());

        // Leaves drop saplings (normal chance) AND peaches (5% chance)
        this.add(ModBlocks.PEACH_LEAVES.get(), block ->
                createLeavesDrops(block, ModBlocks.PEACH_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES)
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemRandomChanceCondition.randomChance(0.05f))
                                .add(LootItem.lootTableItem(ModItems.PEACH.get()))
                        ));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}