package com.gimr12.towers_attack.client;

import com.gimr12.towers_attack.client.gui.LevelingScreen;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = towers_attack.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ModKeyMappings.OPEN_STATUS_SCREEN.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof LevelingScreen) {
                // Already open - B acts as a toggle, close it.
                mc.setScreen(null);
            } else if (mc.screen == null) {
                mc.setScreen(new LevelingScreen());
            }
        }
    }
}
