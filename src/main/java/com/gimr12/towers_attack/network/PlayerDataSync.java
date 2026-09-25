package com.gimr12.towers_attack.network;

import com.gimr12.towers_attack.attachment.ModAttachments;
import com.gimr12.towers_attack.leveling.PlayerLeveling;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Pushes a full GP/SP/stats/Reset Points snapshot to a player's client.
 * Call this once at the end of any server-side operation that changes one
 * or more of those values. NeoForge 1.21/1.21.1 attachments don't sync
 * automatically, so without this the client never finds out anything changed.
 */
public class PlayerDataSync {

    /** No-op on the client; sends the snapshot when called with a real ServerPlayer. */
    public static void syncIfServer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            sendToClient(serverPlayer);
        }
    }

    public static void sendToClient(ServerPlayer player) {
        SyncPlayerDataPacket packet = new SyncPlayerDataPacket(
                player.getData(ModAttachments.GRAND_POINTS.get()),
                PlayerLeveling.getSP(player), // derived, not stored - see PlayerLeveling.getSP
                player.getData(ModAttachments.RESET_POINTS.get()),
                player.getData(ModAttachments.STRENGTH.get()),
                player.getData(ModAttachments.HP.get()),
                player.getData(ModAttachments.STAMINA.get()),
                player.getData(ModAttachments.DEF.get()),
                player.getData(ModAttachments.LUCK.get())
        );
        PacketDistributor.sendToPlayer(player, packet);
        com.gimr12.towers_attack.titles.TitleManager.sync(player);
    }
}

