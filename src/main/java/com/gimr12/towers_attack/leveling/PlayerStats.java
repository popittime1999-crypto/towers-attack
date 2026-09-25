package com.gimr12.towers_attack.leveling;

import com.gimr12.towers_attack.attachment.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Reads/writes the 5 allocatable stats. allocatePoint() and resetStats() must
 * only ever be called SERVER-SIDE (from a payload handler) since they mutate
 * synced data — never call them directly from client code.
 */
public class PlayerStats {

    private static DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> attachmentFor(StatType type) {
        return switch (type) {
            case STRENGTH -> ModAttachments.STRENGTH;
            case HP -> ModAttachments.HP;
            case STAMINA -> ModAttachments.STAMINA;
            case DEF -> ModAttachments.DEF;
            case LUCK -> ModAttachments.LUCK;
        };
    }

    public static int getStat(Player player, StatType type) {
        return player.getData(attachmentFor(type).get());
    }

    /**
     * SERVER-SIDE ONLY. Adds 1 point to the given stat, as long as the player has
     * at least 1 SP available right now (SP is derived, not stored - see
     * PlayerLeveling.getSP). Returns false if not enough SP.
     */
    public static boolean allocatePoint(Player player, StatType type) {
        if (PlayerLeveling.getSP(player) < 1) {
            return false;
        }
        int updated = getStat(player, type) + 1;
        player.setData(attachmentFor(type).get(), updated);
        StatAttributes.apply(player, type, updated);
        com.gimr12.towers_attack.network.PlayerDataSync.syncIfServer(player);
        return true;
    }

    /**
     * SERVER-SIDE ONLY. Zeroes every stat - but only if the player has at least
     * 1 Reset Point, which is consumed on use. Returns false (and changes
     * nothing) if they have none. SP is derived, so zeroing stats automatically
     * makes it rise back to the full (3 x level) for their current level -
     * nothing needs to be separately refunded.
     */
    public static boolean resetStats(Player player) {
        if (getResetPoints(player) <= 0) {
            return false;
        }
        setResetPoints(player, getResetPoints(player) - 1);

        for (StatType type : StatType.values()) {
            player.setData(attachmentFor(type).get(), 0);
            StatAttributes.apply(player, type, 0);
        }
        com.gimr12.towers_attack.network.PlayerDataSync.syncIfServer(player);
        return true;
    }

    public static int getResetPoints(Player player) {
        return player.getData(ModAttachments.RESET_POINTS.get());
    }

    /**
     * SERVER-SIDE ONLY. Same effect as resetStats(), but bypasses the Reset Point
     * requirement entirely and never consumes one. Intended only for the admin
     * /gimr leveling reset command - the player-facing RESET button always goes
     * through resetStats() above instead.
     */
    public static void forceResetStats(Player player) {
        for (StatType type : StatType.values()) {
            player.setData(attachmentFor(type).get(), 0);
            StatAttributes.apply(player, type, 0);
        }
        com.gimr12.towers_attack.network.PlayerDataSync.syncIfServer(player);
    }

    /** SERVER-SIDE ONLY. Intended to be called only from the admin /gimr leveling resetpoint command. */
    public static void setResetPoints(Player player, int amount) {
        player.setData(ModAttachments.RESET_POINTS.get(), Math.max(0, amount));
    }

    /** SERVER-SIDE ONLY. Intended to be called only from the admin /gimr leveling resetpoint command. */
    public static void addResetPoints(Player player, int amount) {
        setResetPoints(player, getResetPoints(player) + amount);
        com.gimr12.towers_attack.network.PlayerDataSync.syncIfServer(player);
    }

    /**
     * SERVER-SIDE ONLY. If the player has more points allocated across their 5
     * stats than (3 x level) allows, removes the excess evenly across all 5 -
     * one point at a time, round-robin, so no single stat absorbs the whole cut.
     * Called automatically by PlayerLeveling.setLevel() whenever level drops (or
     * is set) below what the player's current stat investment requires, so the
     * "total allocated never exceeds 3 x level" rule holds even for admin-forced
     * level changes, not just natural leveling.
     */
    public static void clawBackExcess(Player player, int level) {
        int cap = PlayerLeveling.SP_PER_LEVEL * level;
        int total = 0;
        for (StatType type : StatType.values()) {
            total += getStat(player, type);
        }

        int excess = total - cap;
        if (excess <= 0) return;

        while (excess > 0) {
            boolean removedAny = false;
            for (StatType type : StatType.values()) {
                if (excess <= 0) break;
                int current = getStat(player, type);
                if (current > 0) {
                    int updated = current - 1;
                    player.setData(attachmentFor(type).get(), updated);
                    StatAttributes.apply(player, type, updated);
                    excess--;
                    removedAny = true;
                }
            }
            if (!removedAny) break; // every stat is already at 0 - nothing left to claw back
        }
    }
}
