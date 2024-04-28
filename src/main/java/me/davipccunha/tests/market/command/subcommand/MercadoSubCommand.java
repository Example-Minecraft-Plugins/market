package me.davipccunha.tests.market.command.subcommand;

import org.bukkit.entity.Player;

public interface MercadoSubCommand {
    boolean execute(Player player, String[] args);

    String getUsage();
}
