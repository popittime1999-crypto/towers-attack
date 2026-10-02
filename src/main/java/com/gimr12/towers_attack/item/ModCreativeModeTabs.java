package com.gimr12.towers_attack.item;

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

    public static final Supplier<CreativeModeTab> XP_SCROLL = CREATIVE_MODE_TAB.register("xp_scroll",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.LOWXPSCROLL.get()))
                    .title(Component.translatable("creative_tab.gimrstowerattack.xp_scroll"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.LOWXPSCROLL);
                        output.accept(ModItems.MIDXPSCROLL);
                        output.accept(ModItems.HIGHXPSCROLL);
                        output.accept(ModItems.GODXPSCROLL);
                        output.accept(ModItems.PEACH);

                    }).build());


    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }

}
