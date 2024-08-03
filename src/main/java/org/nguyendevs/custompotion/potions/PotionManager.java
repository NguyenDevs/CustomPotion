package org.nguyendevs.custompotion.potions;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.nguyendevs.custompotion.CustomPotion;

import java.util.*;

public class PotionManager {
    private final CustomPotion plugin;
    private final Map<String, ItemStack> potions;

    public PotionManager(CustomPotion plugin) {
        this.plugin = plugin;
        this.potions = new HashMap<>();
        reloadPotions();
    }

    public void reloadPotions() {
        plugin.reloadConfig();
        potions.clear();
        FileConfiguration config = plugin.getConfig();
        for (String key : Objects.requireNonNull(config.getConfigurationSection("potions")).getKeys(false)) {
            String type = config.getString("potions." + key + ".type");
            String name = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("potions." + key + ".name")));
            List<String> effects = config.getStringList("potions." + key + ".potions");
            int duration = config.getInt("potions." + key + ".duration") * 20; // convert to ticks
            String colorHex = config.getString("potions." + key + ".color"); // New field for color

            ItemStack potionItem;
            if ("splash".equalsIgnoreCase(type)) {
                potionItem = new ItemStack(Material.SPLASH_POTION);
            } else if ("lingering".equalsIgnoreCase(type)) {
                potionItem = new ItemStack(Material.LINGERING_POTION);
            } else {
                potionItem = new ItemStack(Material.POTION);
            }
            PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
            if (meta != null) {
                for (String effect : effects) {
                    String[] parts = effect.split(":");
                    PotionEffectType potionEffectType = PotionEffectType.getByName(parts[0].toUpperCase());
                    int amplifier = parts.length > 1 ? Integer.parseInt(parts[1]) - 1 : 0;
                    if (potionEffectType != null) {
                        meta.addCustomEffect(new PotionEffect(potionEffectType, duration, amplifier), true);
                    } else {
                        plugin.getLogger().warning("Invalid potion effect type: " + parts[0]);
                    }
                }
                meta.setDisplayName(name);
                if (colorHex != null && !colorHex.isEmpty()) {
                    try {
                        meta.setColor(hexToColor(colorHex));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid color code: " + colorHex);
                    }
                }
                potionItem.setItemMeta(meta);
                potions.put(key, potionItem);
            }
        }
    }

    private Color hexToColor(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        int intColor = Integer.parseInt(hex, 16);
        return Color.fromRGB((intColor >> 16) & 0xFF, (intColor >> 8) & 0xFF, intColor & 0xFF);
    }

    public ItemStack getPotion(String name) {
        return potions.get(name).clone();
    }

    public void givePotion(Player player, String potionName, int amount) {
        ItemStack potion = getPotion(potionName);
        potion.setAmount(amount);
        player.getInventory().addItem(potion);
    }

    public List<String> getPotionNames() {
        return new ArrayList<>(potions.keySet());
    }
}
