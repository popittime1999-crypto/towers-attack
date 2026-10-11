package com.gimr12.towers_attack.entity;

import java.util.Arrays;
import java.util.Comparator;

public enum GoblinVariant {
    MALE(0),
    FEMALE(1);

    private static final GoblinVariant[] BY_ID = Arrays.stream(values()).sorted(
            Comparator.comparingInt(GoblinVariant::getId)).toArray(GoblinVariant[] ::new);
    private final int id;

    GoblinVariant(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static GoblinVariant byId(int id){
        return BY_ID[id % BY_ID.length];
    }
}
