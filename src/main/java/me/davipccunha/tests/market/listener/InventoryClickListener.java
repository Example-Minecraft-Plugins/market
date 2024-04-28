package me.davipccunha.tests.market.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.economy.api.EconomyAPI;
import me.davipccunha.tests.economy.api.EconomyType;
import me.davipccunha.tests.market.MarketPlugin;
import me.davipccunha.tests.market.factory.view.MarketGUIFactory;
import me.davipccunha.tests.market.model.GlobalMarketCategoryType;
import me.davipccunha.tests.market.model.MarketProduct;
import me.davipccunha.tests.market.model.PersonalMarket;
import me.davipccunha.tests.market.utils.MarketUtils;
import me.davipccunha.utils.cache.RedisCache;
import me.davipccunha.utils.inventory.InventoryUtil;
import me.davipccunha.utils.item.NBTHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class InventoryClickListener implements Listener {
    private final MarketPlugin plugin;

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory == null) return;
        if (event.getCurrentItem() == null) return;

        String inventoryName = inventory.getName();
        if (!inventoryName.equals("Mercado")
            && !inventoryName.equals("Seu Mercado"))

            return;

        event.setCancelled(true);

        if (!event.getCurrentItem().hasItemMeta()) return;
        if (!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        final Player player = (Player) event.getWhoClicked();

        if (player == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        String action = NBTHandler.getNBT(clickedItem, "action");

        if (action == null) return;

        final RedisCache<PersonalMarket> personalMarketCache = this.plugin.getPersonalMarketCache();

        // In case we had more than one back button in the plugin, we get the inventory to open from the NBT tag "inventory"
        // That should contain the name of the inventory to open that could then be fetched from a Map<String, Inventory> for example
        switch(action) {
            case "back":
                player.openInventory(MarketGUIFactory.createGlobalMarketGUI(this.plugin.getGlobalMarket()));
                break;

            // TODO: Find a better way to handle the "open" action
            case "open":
                String inventoryToOpen = NBTHandler.getNBT(clickedItem, "inventory");
                if (inventoryToOpen == null) return;

                switch(inventoryToOpen) {
                    case "market-category":
                        String category = NBTHandler.getNBT(clickedItem, "category");
                        if (category == null) return;

                        player.openInventory(MarketGUIFactory.createMarketCategoryGUI(
                                this.plugin.getGlobalMarket(), GlobalMarketCategoryType.valueOf(category)
                        ));

                        break;

                    case "personal-market":
                        player.openInventory(MarketGUIFactory.createPersonalMarketGUI(
                                this.plugin.getPersonalMarketCache().get(player.getName())
                        ));

                        break;

                    case "filtered-market":
                        player.openInventory(MarketGUIFactory.createFilteredMarketGUI(
                                this.plugin.getGlobalMarket(), player.getName()
                        ));

                        break;
                }

                break;

            case "global-buy":
                final String serializedItem = NBTHandler.getNBT(clickedItem, "product");
                if (serializedItem == null) return;

                final MarketProduct product = MarketUtils.extractProductInfo(serializedItem);

                if (!plugin.getGlobalMarket().has(product)) {
                    player.sendMessage("§cEste item não está mais disponível no mercado.");
                    player.openInventory(MarketGUIFactory.createGlobalMarketGUI(this.plugin.getGlobalMarket()));
                    return;
                }

                if (!this.handleMarketBuy(player, product)) return;

                plugin.getGlobalMarket().removeProduct(product);

                player.openInventory(MarketGUIFactory.createGlobalMarketGUI(this.plugin.getGlobalMarket()));

                break;

            case "personal-item":
                final String serializedPersonalItem = NBTHandler.getNBT(clickedItem, "product");
                if (serializedPersonalItem == null) return;

                final MarketProduct personalProduct = MarketUtils.extractProductInfo(serializedPersonalItem);
                final PersonalMarket personalMarket = personalMarketCache.get(player.getName());
                // If the player left-clicks on the item they buy it, if they right-click it, it's removed from the personal market
                if (event.isLeftClick()) {
                    if (!this.handleMarketBuy(player, personalProduct)) return;

                    personalMarket.removeProduct(personalProduct);
                    personalMarketCache.add(player.getName(), personalMarket);

                    player.openInventory(MarketGUIFactory.createPersonalMarketGUI(personalMarket));
                }

                if (event.isRightClick()) {
                    this.handlePrivateMarketRemoval(player, personalProduct);

                    player.openInventory(MarketGUIFactory.createPersonalMarketGUI(personalMarketCache.get(player.getName())));
                }

                break;

            case "remove-item":
                final String serializedItemToDelete = NBTHandler.getNBT(clickedItem, "product");
                if (serializedItemToDelete == null) return;

                final MarketProduct productToDelete = MarketUtils.extractProductInfo(serializedItemToDelete);

                plugin.getGlobalMarket().removeProduct(productToDelete);

                // TODO: Removed items should be given back to the player. This will require a new plugin "Postman" that stores items for players

                player.sendMessage("§aVocê removeu seu item do mercado com sucesso.");

                player.openInventory(MarketGUIFactory.createFilteredMarketGUI(this.plugin.getGlobalMarket(), player.getName()));

                break;
        }
    }

    private boolean handleMarketBuy(Player player, MarketProduct product) {
        final String buyer = player.getName();
        final String seller = product.getOwner();

        final EconomyAPI economyAPI = this.plugin.getEconomyAPI();
        if (!economyAPI.hasAccount(seller) || !economyAPI.hasAccount(buyer)) {
            player.sendMessage("§cOcorreu um erro interno. Contate a nossa equipe");
            return false;
        }

        final double price = product.getPrice();

        if (economyAPI.getBalance(buyer, EconomyType.COINS) < price) {
            player.sendMessage("§cVocê não tem coins suficientes para comprar este item.");
            return false;
        }

        final ItemStack item = product.getItemStack();

        final int missingAmount = InventoryUtil.getMissingAmount(player.getInventory(), item);
        if (missingAmount <= item.getAmount()) {
            player.sendMessage("§cVocê não tem espaço suficiente no inventário para comprar este item.");
            return false;
        }

        economyAPI.addBalance(seller, EconomyType.COINS, price);
        economyAPI.removeBalance(buyer, EconomyType.COINS, price);

        player.getInventory().addItem(item);
        player.sendMessage("§aVocê comprou um item com sucesso!");

        final Player sellerPlayer = this.plugin.getServer().getPlayer(seller);
        if (sellerPlayer != null)
            sellerPlayer.sendMessage(String.format("§aUm de seus itens anunciados foi comprado por §f%s§a.", buyer));

        return true;
    }

    private void handlePrivateMarketRemoval(Player player, MarketProduct product) {
        final RedisCache<PersonalMarket> personalMarketCache = this.plugin.getPersonalMarketCache();
        final PersonalMarket personalMarket = personalMarketCache.get(player.getName());

        // TODO: Removed items should be given back to the seller. This will require a new plugin "Postman" that stores items for players

        personalMarket.removeProduct(product);
        personalMarketCache.add(player.getName(), personalMarket);

        player.sendMessage("§aVocê removeu um item do seu mercado pessoal com sucesso.");
    }
}
