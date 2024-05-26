package me.davipccunha.tests.market.model;

import me.davipccunha.tests.market.MarketPlugin;
import me.davipccunha.tests.market.factory.MarketCategoryFactory;
import me.davipccunha.tests.market.utils.MarketUtils;
import me.davipccunha.utils.cache.RedisConnector;
import me.davipccunha.utils.item.ItemSerializer;
import me.davipccunha.utils.item.NBTHandler;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GlobalMarket {
    private static final String GLOBAL_MARKET_REDIS_KEY = "market:global-market";

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

    public void addProduct(MarketProduct product, boolean saveToRedis) {
        final GlobalMarketCategory category = this.getCategory(product.getCategory());

        if (category == null) return;

        category.getProducts().add(product);

        if (saveToRedis) this.saveToRedis();
    }

    public void removeProduct(MarketProduct product) {
        final MarketProduct realProduct = this.findRealProduct(product);
        if (realProduct == null) return;

        final GlobalMarketCategory category = this.getCategory(realProduct.getCategory());

        category.getProducts().remove(realProduct);

        this.saveToRedis();
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
            final GlobalMarketCategory category = MarketCategoryFactory.createCategory(config, key);
            categories.add(category);
        }
    }

    public void saveToRedis() {
        final RedisConnector redisConnector = new RedisConnector();

        final ItemStack[] marketItemStacks = this.convertMarketToItemStacks();

        final String serializedMarket = ItemSerializer.serialize(marketItemStacks);

        try (Jedis jedis = redisConnector.getJedis()) {
            final Pipeline pipeline = jedis.pipelined();
            pipeline.set(GLOBAL_MARKET_REDIS_KEY, serializedMarket);
            pipeline.sync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFromRedis(FileConfiguration config) {
        final RedisConnector redisConnector = new RedisConnector();

        try (Jedis jedis = redisConnector.getJedis()) {
            Pipeline pipeline = jedis.pipelined();
            Response<String> response = pipeline.get(GLOBAL_MARKET_REDIS_KEY);
            pipeline.sync();

            if (response == null || response.get() == null) return;

            final ItemStack[] itemStacks = ItemSerializer.deserialize(response.get());

            final List<MarketProduct> products = this.itemStacksToMarketProduct(config, itemStacks);

            for (MarketProduct product : products) {
                this.addProduct(product, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ItemStack[] convertMarketToItemStacks() {
        final List<ItemStack> items = new ArrayList<>();

        for (MarketProduct product : this.getAllProducts()) {
            final ItemStack item = product.getItemStack();
            final Map<String, String> nbtTags = Map.of(
                    "product", product.getOwner() + ";" + product.getPrice()
            );

            final ItemStack productItemStack = NBTHandler.addNBT(item, nbtTags);

            items.add(productItemStack);
        }

        return items.toArray(new ItemStack[0]);
    }

    private List<MarketProduct> itemStacksToMarketProduct(FileConfiguration config, ItemStack[] itemStacks) {
        final List<MarketProduct> products = new ArrayList<>();

        for (ItemStack itemStack : itemStacks) {
            final String serializedProduct = NBTHandler.getNBT(itemStack, "product");
            final String[] productInfo = serializedProduct.split(";");

            final String owner = productInfo[0];
            final double price = NumberUtils.toDouble(productInfo[1]);

            final ItemStack realItemStack = NBTHandler.removeNBT(itemStack, "product");

            final GlobalMarketCategoryType category = MarketUtils.getCategoryFromConfig(config, realItemStack);

            final String serializedItem = ItemSerializer.serialize(realItemStack);

            final MarketProduct product = new MarketProduct(owner, serializedItem, category, price);

            products.add(product);
        }

        return products;
    }
}
