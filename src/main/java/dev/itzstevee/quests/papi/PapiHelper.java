package dev.itzstevee.quests.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PapiHelper {

    private static boolean enabled = false;

    public static void init() {
        enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static String parse(Player player, String text) {
        if (!enabled) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
