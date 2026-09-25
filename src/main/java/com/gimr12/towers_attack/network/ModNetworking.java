package com.gimr12.towers_attack.network;

import com.gimr12.towers_attack.towers_attack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = towers_attack.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(AllocateStatPacket.TYPE, AllocateStatPacket.STREAM_CODEC, AllocateStatPacket::handle);
        registrar.playToServer(ResetStatsPacket.TYPE, ResetStatsPacket.STREAM_CODEC, ResetStatsPacket::handle);
        registrar.playToClient(SyncPlayerDataPacket.TYPE, SyncPlayerDataPacket.STREAM_CODEC, SyncPlayerDataPacket::handle);
        registrar.playToClient(SyncTitleDataPacket.TYPE, SyncTitleDataPacket.STREAM_CODEC, SyncTitleDataPacket::handle);
        registrar.playToServer(EquipTitlePacket.TYPE, EquipTitlePacket.STREAM_CODEC, EquipTitlePacket::handle);
    }
}
