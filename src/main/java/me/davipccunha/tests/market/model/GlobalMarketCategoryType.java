package me.davipccunha.tests.market.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalMarketCategoryType {
    SPAWNERS("Spawners"),
    TOOLS("Ferramentas"),
    ORES("Minérios"),
    POTIONS("Poções"),
    OTHER("Outros");

    private final String name;

    public static GlobalMarketCategoryType getCategory(String name) {
        try {
            return GlobalMarketCategoryType.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return GlobalMarketCategoryType.OTHER;
        }
    }
}
