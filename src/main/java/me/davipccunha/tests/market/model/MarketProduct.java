package me.davipccunha.tests.market.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.davipccunha.utils.item.ItemSerializer;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
@Getter
public class MarketProduct {
    private final String owner;
    private final String serializedItemStack;
    private final GlobalMarketCategoryType category;
    private final double price;

    public ItemStack getItemStack() {
        return ItemSerializer.deserialize(serializedItemStack)[0];
    }

    public String serialize() {
        return owner + ";" + serializedItemStack + ";" + price;
    }

    @Override
    public String toString() {
        return "MarketProduct{" +
                "owner='" + owner + '\'' +
                ", category=" + category +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MarketProduct)) return false;
        MarketProduct otherProduct = (MarketProduct) other;
        return this.serialize().equals(otherProduct.serialize());
    }

    @Override
    public int hashCode() {
        return this.serialize().hashCode();
    }
}