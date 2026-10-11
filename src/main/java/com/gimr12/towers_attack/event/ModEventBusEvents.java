package com.gimr12.towers_attack.event;

import com.gimr12.towers_attack.entity.ModEntities;
import com.gimr12.towers_attack.entity.client.GoblinModel;
import com.gimr12.towers_attack.entity.custom.GoblinEntity;
import com.gimr12.towers_attack.towers_attack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = towers_attack.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GoblinModel.LAYER_LOCATION, GoblinModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.GOBLIN.get(), GoblinEntity.createAttributes().build());
    }
}