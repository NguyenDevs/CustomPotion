package org.nguyendevs.custompotion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TabComplete implements TabCompleter {
    private final CustomPotion plugin;

    public TabComplete(CustomPotion plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("cpotion")) {
            if (args.length == 1) {
                return Arrays.asList("give", "reload", "create","remove");
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length == 2) {
                    return plugin.getPotionManager().getPotionNames();
                }else {
                    return List.of();
                }
            }
                else if (args[0].equalsIgnoreCase("give")) {
                if (args.length == 2) {
                    return plugin.getPotionManager().getPotionNames();
                } else if (args.length == 3) {
                    return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                } else {
                    return List.of();
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                return List.of();
            } else if (args[0].equalsIgnoreCase("create")) {
                if (args.length == 2) {
                    return Arrays.asList("bottle", "splash", "lingering");
                } else if (args.length == 3) {
                        return Arrays.asList("&", "#");
                    } else {
                    return List.of();
                }
            }
        }
        return null;
    }
}
