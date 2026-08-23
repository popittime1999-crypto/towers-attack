package com.gimr12.towers_attack.item;

import com.gimr12.towers_attack.block.ModBlocks;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, towers_attack.MOD_ID);

    public static final Supplier<CreativeModeTab> TOWER_ITMES_TAB = CREATIVE_MODE_TAB.register("tower_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BADCRYSTAL.get()))
                    .title(Component.translatable("creative_tab.gimrstowerattack.tower_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.BADCRYSTAL);
                        output.accept(ModItems.POORCRYSTAL);
                        output.accept(ModItems.FINECRYSTAL);
                        output.accept(ModItems.BADCRYSTAL_BAR);
                        output.accept(ModItems.POORCRYSTAL_BAR);
                        output.accept(ModItems.FINECRYSTAL_BAR);

                    }).build());


    public static final Supplier<CreativeModeTab> TOWER_BLOCKS_TAB = CREATIVE_MODE_TAB.register("tower_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.BADCRYSTAL_BLOCK))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "tower_items_tab"))
                    .title(Component.translatable("creative_tab.gimrstowerattack.tower_blocks"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.BADCRYSTAL_BLOCK);
                        output.accept(ModBlocks.BADCRYSTAL_BAR_BLOCK);
                        output.accept(ModBlocks.POORCRYSTAL_BLOCK);
                        output.accept(ModBlocks.POORCRYSTAL_BAR_BLOCK);
                        output.accept(ModBlocks.FINECRYSTAL_BLOCK);
                        output.accept(ModBlocks.FINECRYSTAL_BAR_BLOCK);
                        output.accept(ModItems.BLOCK_CHISEL);

                    }).build());



    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }

}
