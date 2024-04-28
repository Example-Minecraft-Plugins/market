package me.davipccunha.tests.market.command.subcommand;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.market.MarketPlugin;
import me.davipccunha.tests.market.model.GlobalMarketCategoryType;
import me.davipccunha.tests.market.model.MarketProduct;
import me.davipccunha.tests.market.model.PersonalMarket;
import me.davipccunha.tests.market.utils.MarketUtils;
import me.davipccunha.utils.cache.RedisCache;
import me.davipccunha.utils.item.ItemSerializer;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class AnunciarSubCommand implements MercadoSubCommand {
    private final MarketPlugin plugin;
    @Override
    public boolean execute(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("§cInforme o preço do produto.");
            return false;
        }

        final double price = NumberUtils.toDouble(args[1]);
        if (price <= 0) {
            player.sendMessage("§cO preço precisa ser maior que 0.");
            return false;
        }

        final ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() == null || item.getType() == Material.AIR) {
            player.sendMessage("§cVocê precisa ter um item na mão para anunciar.");
            return true;
        }

        final String serializedItem = ItemSerializer.serialize(item);

        if (args.length >= 3) {
            final Player target = this.plugin.getServer().getPlayer(args[2]);

            if (target == null) {
                player.sendMessage("§cJogador não encontrado.");
                return true;
            }

            if (target.getName().equals(player.getName())) {
                player.sendMessage("§cVocê não pode anunciar um item para você mesmo.");
                return true;
            }

            handlePersonalMarket(player, target, item, price);
            return true;
        }

        final GlobalMarketCategoryType category = MarketUtils.getCategoryFromConfig(this.plugin.getConfig(), item);
        final MarketProduct product = new MarketProduct(player.getName(), serializedItem, category, price);

        player.getInventory().remove(item);
        this.plugin.getGlobalMarket().addProduct(product);

        player.sendMessage("§aSeu item foi anunciado no mercado com sucesso.");

        return true;
    }

    private void handlePersonalMarket(Player player, Player target, ItemStack item, double price) {
        final RedisCache<PersonalMarket> cache = this.plugin.getPersonalMarketCache();
        PersonalMarket personalMarket = this.plugin.getPersonalMarketCache().get(target.getName());
        if (personalMarket == null) {
            personalMarket = new PersonalMarket(target.getName());
            cache.add(target.getName(), personalMarket);
        }

        if (personalMarket.getProducts().size() > 36) {
            player.sendMessage("§cO mercado pessoal do jogador já está cheio.");
            return;
        }

        final MarketProduct product = new MarketProduct(player.getName(), ItemSerializer.serialize(item), null, price);

        player.getInventory().remove(item);
        personalMarket.addProduct(product);

        cache.add(target.getName(), personalMarket);

        player.sendMessage(String.format("§aVocê anunciou um item no mercado pessoal de §f%s §acom sucesso.", target.getName()));
        target.sendMessage(String.format("§f%s §aanunciou um item em seu mercado pessoal.", player.getName()));
    }

    @Override
    public String getUsage() {
        return "§e/mercado anunciar <preço> [jogador]";
    }
}
