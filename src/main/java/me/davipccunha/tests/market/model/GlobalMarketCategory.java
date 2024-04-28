package me.davipccunha.tests.market.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class GlobalMarketCategory {
    private final GlobalMarketCategoryType type;
    private final List<MarketProduct> products = new ArrayList<>();
    private final int iconID;
    private final short iconData;

    @SuppressWarnings("deprecation")
    public ItemStack getIcon() {
        return new ItemStack(iconID, 1, iconData);
    }

    @Override
    public String toString() {
        return products.toString();
    }
}
