package com.gimr12.towers_attack.item;

import com.gimr12.towers_attack.block.ModBlocks;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, towers_attack.MOD_ID);

    public static final Supplier<CreativeModeTab> XP_SCROLL = CREATIVE_MODE_TAB.register("xp_scroll",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.LOWXPSCROLL.get()))
                    .title(Component.translatable("creative_tab.gimrstowerattack.xp_scroll"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.LOWXPSCROLL.get());
                        output.accept(ModItems.MIDXPSCROLL.get());
                        output.accept(ModItems.HIGHXPSCROLL.get());
                        output.accept(ModItems.GODXPSCROLL.get());
                    }).build());

    public static final Supplier<CreativeModeTab> PEACHES = CREATIVE_MODE_TAB.register("peaches",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PEACH.get()))
                    .title(Component.translatable("creative_tab.gimrstowerattack.peaches"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.PEACH.get());
                        output.accept(ModItems.GOLDEN_PEACH.get());
                        output.accept(ModBlocks.PEACH_LOG.get());
                        output.accept(ModBlocks.STRIPPED_PEACH_LOG.get());
                        output.accept(ModBlocks.PEACH_WOOD.get());
                        output.accept(ModBlocks.STRIPPED_PEACH_WOOD.get());
                        output.accept(ModBlocks.PEACH_PLANKS.get());
                        output.accept(ModBlocks.PEACH_LEAVES.get());
                        output.accept(ModBlocks.PEACH_SAPLING.get());

                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
