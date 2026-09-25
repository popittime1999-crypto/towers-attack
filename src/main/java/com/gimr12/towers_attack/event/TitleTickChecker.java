package com.gimr12.towers_attack.event;

import com.gimr12.towers_attack.titles.TitleManager;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Kill-based titles get checked/synced whenever a kill happens (see
 * GPEventHandler -> TitleManager.recordKill). But titles based on playtime,
 * distance walked, or any other passive stat have no such trigger - without
 * this, they'd only ever get checked if the player happened to also kill
 * something or level up around the same time. This runs the same check on
 * a timer instead, so passive-stat titles actually unlock (and their
 * progress actually reaches the client) during normal play.
 */
@EventBusSubscriber(modid = towers_attack.MOD_ID)
public class TitleTickChecker {

    private static final int CHECK_INTERVAL_TICKS = 200; // 10 seconds
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL_TICKS) return;
        tickCounter = 0;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            TitleManager.checkUnlocks(player);
            TitleManager.sync(player);
        }
    }
}
