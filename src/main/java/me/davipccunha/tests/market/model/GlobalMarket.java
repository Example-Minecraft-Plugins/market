package me.davipccunha.tests.market.model;

import me.davipccunha.tests.market.factory.MarketCategoryFactory;
import me.davipccunha.tests.market.utils.MarketUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collection;

public class GlobalMarket {
    private final Collection<GlobalMarketCategory> categories = new ArrayList<>();

    public GlobalMarket(FileConfiguration config) {
        this.loadCategories(config);
    }

    public GlobalMarketCategory getCategory(GlobalMarketCategoryType type) {
        for (GlobalMarketCategory category : categories) {
            if (category.getType().equals(type)) return category;
        }

        return null;
    }

    public void addProduct(MarketProduct product) {
        final GlobalMarketCategory category = this.getCategory(product.getCategory());

        if (category == null) return;

        category.getProducts().add(product);
    }

    public boolean removeProduct(MarketProduct product) {
        final MarketProduct realProduct = this.findRealProduct(product);
        if (realProduct == null) return false;

        final GlobalMarketCategory category = this.getCategory(realProduct.getCategory());

        return category.getProducts().remove(realProduct);
    }

    public boolean has(MarketProduct product) {
        return this.findRealProduct(product) != null;
    }

    public Collection<MarketProduct> getAllProducts() {
        Collection<MarketProduct> products = new ArrayList<>();
        for (GlobalMarketCategory category : categories) {
            products.addAll(category.getProducts());
        }

        return products;
    }

    private MarketProduct findRealProduct(MarketProduct product) {
        for (MarketProduct realProduct : this.getAllProducts()) {
            if (realProduct.equals(product)) return realProduct;
        }

        return null;
    }

    private void loadCategories(FileConfiguration config) {
        for (String key : config.getConfigurationSection("market-categories").getKeys(false)) {
            GlobalMarketCategory category = MarketCategoryFactory.createCategory(config, key);
            categories.add(category);
        }
    }
}
