package org.nguyendevs.custompotion.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nguyendevs.custompotion.CustomPotion;
import org.nguyendevs.custompotion.potions.PotionManager;

import java.util.Objects;

public class CommandHandler implements CommandExecutor {
    private final CustomPotion plugin;
    private final PotionManager potionManager;

    public CommandHandler(CustomPotion plugin, PotionManager potionManager) {
        this.plugin = plugin;
        this.potionManager = potionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("cpotion.admin")) {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /cpotion <give|reload> <args>");
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("messages.no-permission"))));
            }}

            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("reload")) {
                if (args.length !=1) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /cpotion reload");
                    return true;
                }
                if (sender.hasPermission("cpotion.admin")) {
                    potionManager.reloadPotions();
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("messages.reload-success"))));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("messages.no-permission"))));
                }
                return true;
            } else if (subCommand.equalsIgnoreCase("give")) {
                if (sender.hasPermission("cpotion.admin")) {
                    if (args.length != 4) {
                        sender.sendMessage(ChatColor.YELLOW + "Usage: /cpotion give <potion_name> <player> <amount>");
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                        return true;
                    }

                    Player player = (Player) sender;
                    String potionName = args[1];
                    String targetName = args[2];
                    if (targetName.matches("\\d+")) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-player")));
                        return true;
                    }

                    int amount;
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("messages.invalid-amount"))));
                        return true;
                    }

                    Player targetPlayer = plugin.getServer().getPlayer(targetName);
                    if (targetPlayer == null || targetName.length() <= 3) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.player-not-found")));
                        return true;
                    }

                    if (!potionManager.getPotionNames().contains(potionName)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-potion")));
                        return true;
                    }

                    potionManager.givePotion(targetPlayer, potionName, amount);
                    String givenMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.given-potion"));
                    givenMessage = givenMessage.replace("%amount%", String.valueOf(amount))
                            .replace("%potion_name%", potionName)
                            .replace("%player%", targetName);
                    player.sendMessage(givenMessage);

                    return true;
                } else {
                    sender.sendMessage(Objects.requireNonNull(plugin.getConfig().getString(ChatColor.translateAlternateColorCodes('&', "messages.no-permission"))));
                }
                return true;
            }
        return false;
        }
    }
