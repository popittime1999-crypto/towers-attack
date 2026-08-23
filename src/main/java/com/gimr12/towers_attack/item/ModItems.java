package com.gimr12.towers_attack.item;

import com.gimr12.towers_attack.item.custom.ChiselItem;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(towers_attack.MOD_ID);

    public static final DeferredItem<Item> BADCRYSTAL = ITEMS.register("badcrystal",
            () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final DeferredItem<Item> POORCRYSTAL = ITEMS.register("poorcrystal",
            () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final DeferredItem<Item> BADCRYSTAL_BAR = ITEMS.register("badcrystal_bar",
            () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final DeferredItem<Item> POORCRYSTAL_BAR = ITEMS.register("poorcrystal_bar",
            () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final DeferredItem<Item> FINECRYSTAL = ITEMS.register("finecrystal",
            () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final DeferredItem<Item> FINECRYSTAL_BAR = ITEMS.register("finecrystal_bar",
            () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final DeferredItem<Item>  BLOCK_CHISEL = ITEMS.register("block_chisel",
            () -> new ChiselItem(new Item.Properties().durability(32)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
