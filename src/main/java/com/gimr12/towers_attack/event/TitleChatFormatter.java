package com.gimr12.towers_attack.event;

import com.gimr12.towers_attack.titles.TitleDefinition;
import com.gimr12.towers_attack.titles.TitleManager;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

/**
 * Replaces the vanilla "<Name> message" chat format with
 * "<Name the titlename> message" when the sender has a title equipped -
 * only the "the titlename" portion is colored, using the title's RARITY
 * color (not a per-title color). Players with nothing equipped chat
 * exactly as vanilla does.
 */
@EventBusSubscriber(modid = towers_attack.MOD_ID)
public class TitleChatFormatter {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String equippedId = TitleManager.getEquipped(player);
        if (equippedId == null || equippedId.isEmpty()) return; // no title equipped - leave vanilla chat alone

        TitleDefinition title = TitleManager.get(equippedId);
        if (title == null) return; // equipped id no longer matches a loaded title (e.g. config changed)

        event.setCanceled(true);

        Component theTitlePart = Component.literal(" the " + title.name().toLowerCase())
                .withStyle(style -> style.withColor(TextColor.fromRgb(title.rarity().color())));

        Component formatted = Component.literal("<" + player.getGameProfile().getName())
                .append(theTitlePart)
                .append(Component.literal("> " + event.getRawText()));

        MinecraftServer server = player.getServer();
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(formatted, false);
        }
    }
}
