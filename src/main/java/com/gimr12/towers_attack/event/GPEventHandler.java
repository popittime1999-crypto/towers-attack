package com.gimr12.towers_attack.event;

import com.gimr12.towers_attack.event.MobClassifier.MobClass;
import com.gimr12.towers_attack.leveling.PlayerLeveling;
import com.gimr12.towers_attack.towers_attack;
import com.gimr12.towers_attack.titles.TitleManager;
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

        if (event.getSource().getEntity() instanceof Player player) {
            TitleManager.recordKill(player, killed);
            int gp = calculateGpReward(player, killed);
            if (gp > 0) {
                PlayerLeveling.addGP(player, gp);
            }
        }
    }

    /**
     * GP = modifier x (sqrt(Max Health) x sqrt(Attack Damage))
     *
     * Modifier by mob class:
     * - Passive: x0.5
     * - Normal:  x1.0
     * - Elite:   x1.5   (suggested addition - see MobClassifier)
     * - Boss:    x2.0 on the player's first-ever kill of that boss, x1.5 every kill after
     *
     * Baby mobs additionally get x1.25 on top of whatever their base class modifier is.
     */
    private static int calculateGpReward(Player killer, LivingEntity entity) {
        double maxHealth = Math.max(0, entity.getAttributeValue(Attributes.MAX_HEALTH));
        double attackDamage = Math.max(0, entity.getAttributeValue(Attributes.ATTACK_DAMAGE));
        double base = Math.sqrt(maxHealth) * Math.sqrt(attackDamage);

        MobClass mobClass = MobClassifier.classify(entity);
        double modifier = switch (mobClass) {
            case PASSIVE -> 0.5;
            case NORMAL -> 1.0;
            case ELITE -> 1.5;
            case BOSS -> BossKillTracker.isFirstKill(killer, entity) ? 2.0 : 1.5;
        };

        if (entity.isBaby()) {
            modifier *= 1.25;
        }

        int gp = (int) Math.round(modifier * base);
        return Math.max(1, gp);
    }
}
