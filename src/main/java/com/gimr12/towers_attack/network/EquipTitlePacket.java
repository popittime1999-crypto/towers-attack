package com.gimr12.towers_attack.network;

import com.gimr12.towers_attack.titles.TitleManager;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EquipTitlePacket(String titleId) implements CustomPacketPayload {
    public static final Type<EquipTitlePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "equip_title"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipTitlePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUtf(packet.titleId(), 256),
            buf -> new EquipTitlePacket(buf.readUtf(256))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(EquipTitlePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                if (packet.titleId().isEmpty()) TitleManager.unequip(player);
                else TitleManager.equip(player, packet.titleId());
            }
        });
    }
}
