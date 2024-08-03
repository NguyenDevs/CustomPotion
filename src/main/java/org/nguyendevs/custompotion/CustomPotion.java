package org.nguyendevs.custompotion;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.nguyendevs.custompotion.commands.CommandHandler;
import org.nguyendevs.custompotion.potions.PotionManager;

import java.util.Objects;

public class CustomPotion extends JavaPlugin {
    private PotionManager potionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        potionManager = new PotionManager(this);
        potionManager.reloadPotions();
        CommandHandler commandHandler = new CommandHandler(this, potionManager);
        Objects.requireNonNull(getCommand("cpotion")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("cpotion")).setTabCompleter(new TabComplete(this));
        getLogger().info(ChatColor.GREEN +"CustomPotion has been enabled successfully!");
    }
    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED +" CustomPotion has been disabled successfully!");
    }

    public PotionManager getPotionManager() {
        return potionManager;
    }
}

