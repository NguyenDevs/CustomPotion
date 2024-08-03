package org.nguyendevs.custompotion.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.nguyendevs.custompotion.CustomPotion;
import org.nguyendevs.custompotion.potions.PotionManager;

import java.util.ArrayList;
import java.util.List;
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
                sender.sendMessage(ChatColor.YELLOW + "Usage: /cpotion <give|reload|create|remove> <args>");
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("messages.no-permission"))));
            }
        }

        String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("reload")) {
            if (args.length != 1) {
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

                String potionName = args[1];
                String targetName = args[2];

                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("messages.invalid-amount"))));
                    return true;
                }

                if (!potionManager.getPotionNames().contains(potionName)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.potion-not-found")));
                    return true;
                }

                Player targetPlayer = plugin.getServer().getPlayer(targetName);
                if (targetPlayer == null || targetName.length() <= 3) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.player-not-found")));
                    return true;
                }

                potionManager.givePotion(targetPlayer, potionName, amount);
                String givenMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.given-potion"));
                givenMessage = givenMessage.replace("%amount%", String.valueOf(amount))
                        .replace("%potion_name%", potionName)
                        .replace("%player%", targetName);
                sender.sendMessage(givenMessage);

                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("messages.no-permission"))));
            }
            return true;
        } else if (subCommand.equalsIgnoreCase("create")) {
            if (sender.hasPermission("cpotion.admin")) {
                if (args.length != 6) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /cp create <type> <color> <potion_name> <duration_seconds> <effect:level>,<effect:level>,...");
                    return true;
                }

                String type = args[1];
                String colorInput = args[2];
                String potionName = args[3];

                if (plugin.getConfig().contains("potions." + potionName)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.potion-already-exists")));
                    return true;
                }

                String color = parseColor(colorInput);
                if (color == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-color")));
                    return true;
                }

                int duration;
                try {
                    duration = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-duration")));
                    return true;
                }

                String[] effectsArray = args[5].split(",");
                List<String> effects = new ArrayList<>();
                for (String effect : effectsArray) {
                    String[] effectParts = effect.split(":");
                    if (effectParts.length != 2) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-potion-effect")));
                        return true;
                    }
                    try {
                        Integer.parseInt(effectParts[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-effect-level")));
                        return true;
                    }
                    effects.add(effect);
                }

                FileConfiguration config = plugin.getConfig();
                String path = "potions." + potionName;
                config.set(path + ".type", type);
                config.set(path + ".color", color);
                config.set(path + ".name", potionName);
                config.set(path + ".potions", effects);
                config.set(path + ".duration", duration);

                plugin.saveConfig();
                potionManager.reloadPotions();

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.create-success").replace("%potion_name%", potionName)));

                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("messages.no-permission"))));
            }
            return true;
        } else if (subCommand.equalsIgnoreCase("remove")) {
        if (sender.hasPermission("cpotion.admin")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /cp remove <potion_name>");
                return true;
            }

            String potionName = args[1];
            if (!plugin.getConfig().contains("potions." + potionName)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.potion-not-found")));
                return true;
            }

            plugin.getConfig().set("potions." + potionName, null);
            plugin.saveConfig();
            potionManager.reloadPotions();

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.remove-success").replace("%potion_name%", potionName)));

            return true;
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("messages.no-permission"))));
        }
        return true;
    }

        return false;
}

    private String parseColor(String input) {
        if (input.startsWith("#")) {
            if (input.length() == 7) {
                return input;
            } else {
                return null;
            }
        } else if (input.startsWith("&")) {
            return switch (input.toLowerCase()) {
                case "&0" -> "#000000";
                case "&1" -> "#0000AA";
                case "&2" -> "#00AA00";
                case "&3" -> "#00AAAA";
                case "&4" -> "#AA0000";
                case "&5" -> "#AA00AA";
                case "&6" -> "#FFAA00";
                case "&7" -> "#AAAAAA";
                case "&8" -> "#555555";
                case "&9" -> "#5555FF";
                case "&a" -> "#55FF55";
                case "&b" -> "#55FFFF";
                case "&c" -> "#FF5555";
                case "&d" -> "#FF55FF";
                case "&e" -> "#FFFF55";
                case "&f" -> "#FFFFFF";
                default -> null;
            };
        }
        return null;
    }
}
