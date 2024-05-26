package me.davipccunha.tests.market;

import lombok.Getter;
import me.davipccunha.tests.economy.api.EconomyAPI;
import me.davipccunha.tests.mailman.api.MailmanAPI;
import me.davipccunha.tests.market.command.MercadoCommand;
import me.davipccunha.tests.market.listener.InventoryClickListener;
import me.davipccunha.tests.market.model.GlobalMarket;
import me.davipccunha.tests.market.model.PersonalMarket;
import me.davipccunha.utils.cache.RedisCache;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class MarketPlugin extends JavaPlugin {
    private EconomyAPI economyAPI;
    private MailmanAPI mailmanAPI;
    private GlobalMarket globalMarket;
    private RedisCache<PersonalMarket> personalMarketCache;

    @Override
    public void onEnable() {
        this.init();
        getLogger().info("Market plugin enabled!");
    }

    @Override
    public void onDisable() {
        this.saveGlobalMarket();
        getLogger().info("Market plugin disabled!");
    }

    private void init() {
        saveDefaultConfig();
        this.registerListeners(
                new InventoryClickListener(this)
        );
        this.registerCommands();
        this.load();

        if (Bukkit.getPluginManager().getPlugin("mailman") == null)
            getLogger().warning("Mailman not found. Some features may not work properly.");
    }

    private void registerListeners(Listener... listeners) {
        PluginManager pluginManager = getServer().getPluginManager();
        for (Listener listener : listeners) pluginManager.registerEvents(listener, this);
    }

    private void registerCommands() {
        getCommand("mercado").setExecutor(new MercadoCommand(this));
    }

    private void load() {
        this.economyAPI = Bukkit.getServicesManager().load(EconomyAPI.class);
        this.mailmanAPI = Bukkit.getServicesManager().load(MailmanAPI.class);
        this.globalMarket = new GlobalMarket(getConfig());

        this.globalMarket.loadFromRedis(getConfig());
        this.personalMarketCache = new RedisCache<>("market:personal-market", PersonalMarket.class);
    }

    private void saveGlobalMarket() {
        this.globalMarket.saveToRedis();
    }
}
