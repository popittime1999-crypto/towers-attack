package com.gimr12.towers_attack.client;

import com.gimr12.towers_attack.titles.TitleDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientTitleData {
    private static final List<TitleDefinition> titles = new ArrayList<>();
    private static final Map<String, Integer> killCounts = new HashMap<>();
    private static final Set<String> unlocked = new HashSet<>();
    private static String equipped = "";

    private ClientTitleData() {}

    public static void update(List<String> definitionsJson, Map<String, Integer> kills, List<String> unlockedIds, String equippedId) {
        titles.clear();
        for (String json : definitionsJson) {
            try { titles.add(TitleDefinition.fromJson(json, "unknown")); }
            catch (Exception ignored) {}
        }
        killCounts.clear();
        killCounts.putAll(kills);
        unlocked.clear();
        unlocked.addAll(unlockedIds);
        equipped = equippedId == null ? "" : equippedId;
    }

    public static List<TitleDefinition> getTitles() { return List.copyOf(titles); }
    public static int getKillCount(String key) { return killCounts.getOrDefault(key, 0); }
    public static boolean isUnlocked(String id) { return unlocked.contains(id); }
    public static String getEquipped() { return equipped; }

    /** Returns the currently equipped title's full definition, or null if nothing's equipped. */
    public static TitleDefinition getEquippedTitle() {
        if (equipped.isEmpty()) return null;
        for (TitleDefinition title : titles) {
            if (title.id().equals(equipped)) return title;
        }
        return null;
    }
}
