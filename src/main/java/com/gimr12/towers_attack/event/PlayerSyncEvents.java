package com.gimr12.towers_attack.event;

import com.gimr12.towers_attack.leveling.StatAttributes;
import com.gimr12.towers_attack.network.PlayerDataSync;
import com.gimr12.towers_attack.titles.TitleManager;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Since attachments don't sync automatically in NeoForge 1.21/1.21.1, a
 * freshly-joined (or respawned) client has no idea what its GP/SP/stats
 * are until something changes them. These events push an initial snapshot
 * at the moments the client would otherwise be stuck showing zeros.
 */
@EventBusSubscriber(modid = towers_attack.MOD_ID)
public class PlayerSyncEvents {

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            StatAttributes.applyAll(serverPlayer);
            TitleManager.applyTitleAttributes(serverPlayer);
            TitleManager.updateTeam(serverPlayer);
            TitleManager.sync(serverPlayer);
            PlayerDataSync.sendToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Safety net: re-apply stat bonuses in case respawn didn't carry over
            // the permanent attribute modifiers as expected.
            StatAttributes.applyAll(serverPlayer);
            TitleManager.applyTitleAttributes(serverPlayer);
            TitleManager.updateTeam(serverPlayer);
            TitleManager.sync(serverPlayer);
            PlayerDataSync.sendToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TitleManager.sync(serverPlayer);
            PlayerDataSync.sendToClient(serverPlayer);
        }
    }
}
