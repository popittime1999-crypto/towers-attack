package com.gimr12.towers_attack.network;

import com.gimr12.towers_attack.leveling.PlayerStats;
import com.gimr12.towers_attack.leveling.StatType;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Sent client -> server when the player clicks a stat icon in the Status Menu. */
public record AllocateStatPacket(String statId) implements CustomPacketPayload {

    public static final Type<AllocateStatPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "allocate_stat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AllocateStatPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AllocateStatPacket::statId,
            AllocateStatPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllocateStatPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                try {
                    StatType type = StatType.valueOf(packet.statId());
                    PlayerStats.allocatePoint(serverPlayer, type);
                } catch (IllegalArgumentException ignored) {
                    // Unknown/garbage stat id - ignore silently.
                }
            }
        });
    }
}
