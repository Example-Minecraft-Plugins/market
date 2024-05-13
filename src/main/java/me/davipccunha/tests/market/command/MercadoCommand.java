package me.davipccunha.tests.market.command;

import me.davipccunha.tests.market.MarketPlugin;
import me.davipccunha.tests.market.command.subcommand.AnunciarSubCommand;
import me.davipccunha.tests.market.command.subcommand.MercadoSubCommand;
import me.davipccunha.tests.market.factory.view.MarketGUIFactory;
import me.davipccunha.utils.messages.ErrorMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MercadoCommand implements CommandExecutor {
    private static String COMMAND_USAGE;
    private final MarketPlugin plugin;
    private final Map<String, MercadoSubCommand> subCommands = new HashMap<>();

    public MercadoCommand(MarketPlugin plugin) {
        this.plugin = plugin;

        this.loadSubCommands();

        this.updateUsage();
    }

    private void updateUsage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/mercado [");
        for (String subCommand : this.subCommands.keySet()) {
            stringBuilder.append(subCommand).append(" | ");
        }
        stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length());
        stringBuilder.append("]");

        COMMAND_USAGE = stringBuilder.toString();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ErrorMessages.EXECUTOR_NOT_PLAYER.getMessage());
            return false;
        }

        final Player player = (Player) sender;

        if (args.length == 0) {
            player.openInventory(MarketGUIFactory.createGlobalMarketGUI(plugin.getGlobalMarket()));
            return true;
        }

        final MercadoSubCommand subCommand = this.subCommands.get(args[0]);

        if (subCommand == null) {
            sender.sendMessage(ErrorMessages.SUBCOMMAND_NOT_FOUND.getMessage());
            sender.sendMessage("§cUso: " + COMMAND_USAGE);
            return false;
        }

        if (!subCommand.execute(player, args)) {
            sender.sendMessage("§cUso: " + subCommand.getUsage());
            return false;
        }

        return true;
    }

    private void loadSubCommands() {
        this.subCommands.put("anunciar", new AnunciarSubCommand(this.plugin));
    }
}
