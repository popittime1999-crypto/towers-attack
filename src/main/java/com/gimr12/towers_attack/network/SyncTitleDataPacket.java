package com.gimr12.towers_attack.network;

import com.gimr12.towers_attack.client.ClientTitleData;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncTitleDataPacket(
        List<String> definitionsJson,
        Map<String, Integer> killCounts,
        List<String> unlocked,
        String equipped
) implements CustomPacketPayload {
    public static final Type<SyncTitleDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "sync_title_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTitleDataPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.definitionsJson().size());
                for (String json : packet.definitionsJson()) buf.writeUtf(json, 32767);

                buf.writeVarInt(packet.killCounts().size());
                packet.killCounts().forEach((key, value) -> {
                    buf.writeUtf(key, 32767);
                    buf.writeVarInt(value);
                });

                buf.writeVarInt(packet.unlocked().size());
                for (String id : packet.unlocked()) buf.writeUtf(id, 32767);
                buf.writeUtf(packet.equipped() == null ? "" : packet.equipped(), 32767);
            },
            buf -> {
                int definitionCount = buf.readVarInt();
                List<String> definitions = new ArrayList<>(definitionCount);
                for (int i = 0; i < definitionCount; i++) definitions.add(buf.readUtf(32767));

                int killCount = buf.readVarInt();
                Map<String, Integer> kills = new HashMap<>();
                for (int i = 0; i < killCount; i++) kills.put(buf.readUtf(32767), buf.readVarInt());

                int unlockedCount = buf.readVarInt();
                List<String> unlocked = new ArrayList<>(unlockedCount);
                for (int i = 0; i < unlockedCount; i++) unlocked.add(buf.readUtf(32767));
                String equipped = buf.readUtf(32767);
                return new SyncTitleDataPacket(definitions, kills, unlocked, equipped);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncTitleDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientTitleData.update(packet.definitionsJson(), packet.killCounts(), packet.unlocked(), packet.equipped()));
    }
}
