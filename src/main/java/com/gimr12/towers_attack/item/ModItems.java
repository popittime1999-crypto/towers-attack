package com.gimr12.towers_attack.item;

import com.gimr12.towers_attack.towers_attack;
import com.gimr12.towers_attack.item.custom.LowXpScrollItem; // שורת ה-Import החשובה שמתקנת את השגיאה
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(towers_attack.MOD_ID);

    public static final DeferredItem<LowXpScrollItem> LOWXPSCROLL = ITEMS.register("lowxpscroll",
            () -> new LowXpScrollItem(new Item.Properties().stacksTo(64), 50));

    public static final DeferredItem<LowXpScrollItem> MIDXPSCROLL = ITEMS.register("midxpscroll",
            () -> new LowXpScrollItem(new Item.Properties().stacksTo(64), 150));

    public static final DeferredItem<LowXpScrollItem> HIGHXPSCROLL = ITEMS.register("highxpscroll",
            () -> new LowXpScrollItem(new Item.Properties().stacksTo(64), 400));

    public static final DeferredItem<LowXpScrollItem> GODXPSCROLL = ITEMS.register("godxpscroll",
            () -> new LowXpScrollItem(new Item.Properties().stacksTo(64), 1000));
}
