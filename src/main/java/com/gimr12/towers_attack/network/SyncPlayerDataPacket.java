package com.gimr12.towers_attack.network;

import com.gimr12.towers_attack.client.ClientPlayerData;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent server -> client whenever a player's GP/SP/Reset Points/stats change
 * (see network/PlayerDataSync.java) so the Status Menu can show live values.
 *
 * NeoForge 1.21/1.21.1 has no built-in attachment sync, so this is the
 * manual equivalent: a full snapshot, always sent as one packet.
 */
public record SyncPlayerDataPacket(
        int gp,
        int sp,
        int resetPoints,
        int strength,
        int hp,
        int stamina,
        int def,
        int luck
) implements CustomPacketPayload {

    public static final Type<SyncPlayerDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "sync_player_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerDataPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.gp());
                buf.writeVarInt(packet.sp());
                buf.writeVarInt(packet.resetPoints());
                buf.writeVarInt(packet.strength());
                buf.writeVarInt(packet.hp());
                buf.writeVarInt(packet.stamina());
                buf.writeVarInt(packet.def());
                buf.writeVarInt(packet.luck());
            },
            buf -> new SyncPlayerDataPacket(
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPlayerData.update(
                packet.gp(), packet.sp(), packet.resetPoints(),
                packet.strength(), packet.hp(), packet.stamina(),
                packet.def(), packet.luck()
        ));
    }
}
