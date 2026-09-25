package com.gimr12.towers_attack.client;

import com.gimr12.towers_attack.leveling.StatType;

/**
 * Client-side-only cache of the local player's leveling data. This is NOT the
 * source of truth (the server attachments are) - it just mirrors the last
 * SyncPlayerDataPacket received, so the Status Menu has something to read
 * without needing built-in attachment sync (not available in 1.21/1.21.1).
 */
public class ClientPlayerData {
    private static int gp = 0;
    private static int sp = 0;
    private static int resetPoints = 0;
    private static int strength = 0;
    private static int hp = 0;
    private static int stamina = 0;
    private static int def = 0;
    private static int luck = 0;

    public static void update(int gp, int sp, int resetPoints, int strength, int hp,
                               int stamina, int def, int luck) {
        ClientPlayerData.gp = gp;
        ClientPlayerData.sp = sp;
        ClientPlayerData.resetPoints = resetPoints;
        ClientPlayerData.strength = strength;
        ClientPlayerData.hp = hp;
        ClientPlayerData.stamina = stamina;
        ClientPlayerData.def = def;
        ClientPlayerData.luck = luck;
    }

    public static int getGP() {
        return gp;
    }

    public static int getSP() {
        return sp;
    }

    public static int getResetPoints() {
        return resetPoints;
    }

    public static int getStat(StatType type) {
        return switch (type) {
            case STRENGTH -> strength;
            case HP -> hp;
            case STAMINA -> stamina;
            case DEF -> def;
            case LUCK -> luck;
        };
    }
}
