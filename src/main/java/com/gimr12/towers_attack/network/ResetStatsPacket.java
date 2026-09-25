package com.gimr12.towers_attack.network;

import com.gimr12.towers_attack.leveling.PlayerStats;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Sent client -> server when the player clicks RESET in the Status Menu. */
public record ResetStatsPacket() implements CustomPacketPayload {

    public static final ResetStatsPacket INSTANCE = new ResetStatsPacket();

    public static final Type<ResetStatsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "reset_stats"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResetStatsPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResetStatsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                boolean success = PlayerStats.resetStats(serverPlayer);
                if (!success) {
                    serverPlayer.displayClientMessage(
                            Component.literal("You need a Reset Point to do that."), true);
                }
            }
        });
    }
}
