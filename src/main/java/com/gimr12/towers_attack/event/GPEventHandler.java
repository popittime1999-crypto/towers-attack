package com.gimr12.towers_attack.event;

import com.gimr12.towers_attack.event.MobClassifier.MobClass;
import com.gimr12.towers_attack.leveling.PlayerLeveling;
import com.gimr12.towers_attack.towers_attack;
import com.gimr12.towers_attack.titles.TitleManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = towers_attack.MOD_ID)
public class GPEventHandler {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity killed = event.getEntity();
        if (killed instanceof Player) return;

        Entity credit = killed.getKillCredit();
        if (!(credit instanceof Player player)) return;

        TitleManager.recordKill(player, killed);
        int gp = calculateGpReward(player, killed);
        if (gp > 0) {
            PlayerLeveling.addGP(player, gp);
            player.displayClientMessage(Component.literal("+" + gp + " XP"), true);
        }
    }

    private static int calculateGpReward(Player killer, LivingEntity entity) {
        double maxHealth = entity.getAttribute(Attributes.MAX_HEALTH) != null
                ? entity.getAttributeValue(Attributes.MAX_HEALTH)
                : 10.0;

        double attackDamage;
        if (entity.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            attackDamage = Math.max(0, entity.getAttributeValue(Attributes.ATTACK_DAMAGE));
        } else {
            attackDamage = 5.0;
        }

        double base = Math.sqrt(maxHealth) * Math.sqrt(attackDamage);

        MobClass mobClass = MobClassifier.classify(entity);
        double modifier = switch (mobClass) {
            case PASSIVE -> 1.0;
            case NORMAL -> 1.0;
            case ELITE -> 1.5;
            case BOSS -> BossKillTracker.isFirstKill(killer, entity) ? 2.0 : 1.5;
        };

        if (entity.isBaby()) {
            modifier *= 1.25;
        }

        int gp = (int) Math.round(modifier * base);
        return gp;
    }
}