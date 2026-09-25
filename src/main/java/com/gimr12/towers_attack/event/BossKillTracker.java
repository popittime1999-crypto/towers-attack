package com.gimr12.towers_attack.event;

import com.gimr12.towers_attack.attachment.ModAttachments;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;


public class BossKillTracker {

    public static boolean isFirstKill(Player player, LivingEntity boss) {
        EntityType<?> type = boss.getType();

        if (type == EntityType.WITHER) {
            return markIfFirst(player, ModAttachments.KILLED_WITHER.get());
        }
        if (type == EntityType.ENDER_DRAGON) {
            return markIfFirst(player, ModAttachments.KILLED_ENDER_DRAGON.get());
        }

        // Unknown/future boss type with no tracking attachment yet - treat every
        // kill as "first" rather than silently under-rewarding it.
        return true;
    }

    private static boolean markIfFirst(Player player, AttachmentType<Boolean> attachment) {
        boolean already = player.getData(attachment);
        if (!already) {
            player.setData(attachment, true);
            return true;
        }
        return false;
    }
}
