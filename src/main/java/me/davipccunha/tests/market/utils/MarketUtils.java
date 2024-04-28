package me.davipccunha.tests.market.utils;

import lombok.NonNull;
import me.davipccunha.tests.market.model.GlobalMarketCategoryType;
import me.davipccunha.tests.market.model.MarketProduct;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MarketUtils {
    private static final String KEY = "market-categories.";

    public static @NonNull GlobalMarketCategoryType getProductCategoryFromConfig(FileConfiguration config, int id, short data) {
        String productID = id + ":" + data;
        for (String category : config.getConfigurationSection(KEY).getKeys(false)) {
            List<String> products = config.getStringList(KEY + category + ".products");
            if (products.contains(productID)) return GlobalMarketCategoryType.getCategory(category);
        }

        return GlobalMarketCategoryType.OTHER;
    }

    @SuppressWarnings("deprecation")
    public static @NonNull GlobalMarketCategoryType getCategoryFromConfig(FileConfiguration config, ItemStack item) {
        return getProductCategoryFromConfig(config, item.getTypeId(), item.getDurability());
    }

    public static MarketProduct extractProductInfo(String serialized) {
        String[] parts = serialized.split(";");
        // We set the category as null because it is not important for other operations
        return new MarketProduct(parts[0], parts[1], null, Double.parseDouble(parts[2]));
    }
}
