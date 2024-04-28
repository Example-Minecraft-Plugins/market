package me.davipccunha.tests.market.factory.view;

import me.davipccunha.tests.economy.api.util.EconomyFormatter;
import me.davipccunha.tests.market.model.*;
import me.davipccunha.utils.inventory.InteractiveInventory;
import me.davipccunha.utils.item.ItemName;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MarketGUIFactory {
    private static final ItemStack BACK = InteractiveInventory.createBackItem("global-market");
    public static Inventory createGlobalMarketGUI(GlobalMarket globalMarket) {
        final Inventory inventory = Bukkit.createInventory(null, 4 * 9, "Mercado");

        final List<String> categoryLore = List.of("§7 Clique para ver os produtos desta categoria");

        int slot = 11;
        for (GlobalMarketCategoryType categoryType : GlobalMarketCategoryType.values()) {
            GlobalMarketCategory category = globalMarket.getCategory(categoryType);
            if (category == null) continue;

            final ItemStack icon = category.getIcon();
            final String name = "§r§a" + categoryType.getName();
            final Map<String, String> categoryNBTTags = Map.of(
                    "action", "open",
                    "inventory", "market-category",
                    "category", categoryType.name()
            );

            final ItemStack categoryItem = InteractiveInventory.createActionItem(icon, categoryNBTTags, name, categoryLore);

            inventory.setItem(slot, categoryItem);
            slot++;
        }

        final Map<String, String> personalMarketNBTTags = Map.of(
                "action", "open",
                "inventory", "personal-market"
        );

        final List<String> personalMarketLore = List.of("§7 Clique para abrir seu mercado pessoal");

        final ItemStack personalMarket = InteractiveInventory.createActionItem(
                new ItemStack(Material.CHEST), personalMarketNBTTags, "§aMercado pessoal", personalMarketLore
        );

        inventory.setItem(30, personalMarket);

        final Map<String, String> filteredNBTTags = Map.of(
                "action", "open",
                "inventory", "filtered-market"
        );

        final List<String> filteredLore = List.of("§7 Clique para ver seus itens à venda");

        final ItemStack filteredMarket = InteractiveInventory.createActionItem(
                new ItemStack(Material.HOPPER), filteredNBTTags, "§aSeus itens", filteredLore
        );

        inventory.setItem(32, filteredMarket);

        return inventory;
    }

    public static Inventory createMarketCategoryGUI(GlobalMarket globalMarket, GlobalMarketCategoryType categoryType) {
        final GlobalMarketCategory category = globalMarket.getCategory(categoryType);

        if (category.getProducts().isEmpty()) return createEmptyMenu("Mercado", "Não há itens à venda nesta categoria.");

        final Inventory inventory = Bukkit.createInventory(null, 6 * 9, "Mercado");
        inventory.setItem(49, BACK);

        // TODO: Multiple pages according to the amount of products in the category (max 36 products per page)

        for (MarketProduct product : category.getProducts()) {
            final ItemStack item = product.getItemStack();
            final Map<String, String> nbtTags = Map.of(
                    "action", "global-buy",
                    "product", product.serialize()
            );
            final List<String> lore = List.of(
                    "§7 * Vendedor: §f" + product.getOwner(),
                    "§7 * Preço: §f" + EconomyFormatter.suffixFormat(product.getPrice()),
                    "§7 Clique para comprar este item"
            );
            final String name = item.getItemMeta().getDisplayName() == null ?
                    "§e" + ItemName.valueOf(item).toString()
                    : "§e" + item.getItemMeta().getDisplayName();

            final ItemStack productItem = InteractiveInventory.createActionItem(item, nbtTags, name, lore);

            inventory.addItem(productItem);
        }

        return inventory;
    }

    public static Inventory createPersonalMarketGUI(@Nullable PersonalMarket personalMarket) {
        if (personalMarket == null || personalMarket.getProducts().isEmpty())
            return createEmptyMenu("Seu Mercado", "Não há itens em seu mercado pessoal.");

        final Inventory inventory = Bukkit.createInventory(null, 6 * 9, "Seu Mercado");
        inventory.setItem(49, BACK);

        for (MarketProduct product : personalMarket.getProducts()) {
            final ItemStack item = product.getItemStack();
            final Map<String, String> nbtTags = Map.of(
                    "action", "personal-item",
                    "product", product.serialize()
            );
            final List<String> lore = List.of(
                    "§7 * Vendedor: §f" + product.getOwner(),
                    "§7 * Preço: §f" + EconomyFormatter.suffixFormat(product.getPrice()),
                    "§7 Clique esquerdo para comprar este item",
                    "§7 Clique direito para remover este item do seu mercado"
            );
            final String name = item.getItemMeta().getDisplayName() == null ?
                    "§e" + ItemName.valueOf(item).toString()
                    : "§e" + item.getItemMeta().getDisplayName();

            final ItemStack productItem = InteractiveInventory.createActionItem(item, nbtTags, name, lore);

            inventory.addItem(productItem);
        }

        return inventory;
    }

    public static Inventory createFilteredMarketGUI(GlobalMarket globalMarket, String owner) {
        final Inventory inventory = Bukkit.createInventory(null, 6 * 9, "Mercado");
        inventory.setItem(49, BACK);

        Collection<MarketProduct> products = globalMarket.getAllProducts();
        products.removeIf(product -> !product.getOwner().equals(owner));

        if (products.isEmpty()) return createEmptyMenu("Mercado", "Você não tem nenhum item à venda.");

        for (MarketProduct product : products) {
            final ItemStack item = product.getItemStack();
            final Map<String, String> nbtTags = Map.of(
                    "action", "remove-item",
                    "product", product.serialize()
            );
            final List<String> lore = List.of(
                    "§7 * Preço: §f" + EconomyFormatter.suffixFormat(product.getPrice()),
                    "§7 Clique para remover este item do mercado"
            );
            final String name = item.getItemMeta().getDisplayName() == null ?
                    "§e" + ItemName.valueOf(item).toString()
                    : "§e" + item.getItemMeta().getDisplayName();

            final ItemStack productItem = InteractiveInventory.createActionItem(item, nbtTags, name, lore);

            inventory.addItem(productItem);
        }

        return inventory;
    }

    private static Inventory createEmptyMenu(String title, String description) {
        final Inventory inventory = Bukkit.createInventory(null, 6 * 9, title);
        ItemStack empty = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = empty.getItemMeta();
        itemMeta.setDisplayName("§eVazio");
        itemMeta.setLore(List.of("§c  " + description));
        empty.setItemMeta(itemMeta);

        inventory.setItem(22, empty);
        inventory.setItem(49, BACK);
        return inventory;
    }
}
