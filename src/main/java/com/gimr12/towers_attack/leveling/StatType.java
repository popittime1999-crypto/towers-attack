package com.gimr12.towers_attack.leveling;

public enum StatType {
    STRENGTH("Strength"),
    HP("Health"),
    STAMINA("Stamina"),
    DEF("Defense"),
    LUCK("Luck");

    private final String displayName;

    StatType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
