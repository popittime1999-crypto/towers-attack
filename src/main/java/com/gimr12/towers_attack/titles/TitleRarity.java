package com.gimr12.towers_attack.titles;

public enum TitleRarity {
    COMMON(0xFFAAAAAA, 1.0, "Easy, mostly cosmetic"),
    RARE(0xFF55AAFF, 2.0, "Moderate, small buffs"),
    EPIC(0xFFAA55FF, 4.0, "Notable effort required"),
    MYTHIC(0xFFFF5555, 8.0, "Server-wide prestige"),
    LEGENDARY(0xFFFFAA00, 15.0, "Only for dedicated players"),
    DEMI_GOD(0xFFFF55FF, 1.0, "Command only"),
    GOD(0xFFFFFF55, 1.0, "Command only");

    private final int color;
    private final double defaultDifficultyMultiplier;
    private final String description;

    TitleRarity(int color, double defaultDifficultyMultiplier, String description) {
        this.color = color;
        this.defaultDifficultyMultiplier = defaultDifficultyMultiplier;
        this.description = description;
    }

    public int color() { return color; }
    public double defaultDifficultyMultiplier() { return defaultDifficultyMultiplier; }
    public String description() { return description; }

    public static TitleRarity fromString(String value) {
        if (value == null) return COMMON;
        try {
            return valueOf(value.trim().toUpperCase().replace(' ', '_').replace('-', '_'));
        } catch (IllegalArgumentException ignored) {
            return COMMON;
        }
    }
}
