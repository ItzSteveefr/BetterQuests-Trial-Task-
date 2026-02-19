package dev.itzstevee.quests.gui;

import dev.itzstevee.quests.config.ConfigManager;
import dev.itzstevee.quests.data.PlayerDataCache;
import dev.itzstevee.quests.data.PlayerQuestData;
import dev.itzstevee.quests.quest.QuestRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GuiManager {

    private final JavaPlugin plugin;
    private final QuestRegistry questRegistry;
    private final PlayerDataCache playerDataCache;
    private final ConfigManager configManager;
    private final PagedGuiRenderer renderer;
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Set<UUID> openInventories = new HashSet<>();

    public GuiManager(JavaPlugin plugin, QuestRegistry questRegistry, PlayerDataCache playerDataCache, ConfigManager configManager) {
        this.plugin = plugin;
        this.questRegistry = questRegistry;
        this.playerDataCache = playerDataCache;
        this.configManager = configManager;
        this.renderer = new PagedGuiRenderer(questRegistry, configManager);
    }

    public void openGui(Player player, int page) {
        PlayerQuestData data = playerDataCache.getIfPresent(player.getUniqueId());
        if (data == null) {
            playerDataCache.get(player.getUniqueId()).thenAccept(loadedData -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    openGuiWithData(player, page, loadedData);
                });
            }).exceptionally(ex -> {
                plugin.getLogger().warning("Failed to load data for " + player.getName() + ": " + ex.getMessage());
                return null;
            });
            return;
        }
        openGuiWithData(player, page, data);
    }

    private void openGuiWithData(Player player, int page, PlayerQuestData data) {
        int totalPages = renderer.getTotalPages();
        int safePage = Math.max(1, Math.min(page, totalPages));

        Inventory inventory = renderer.renderPage(player, data, safePage, totalPages);
        playerPages.put(player.getUniqueId(), safePage);
        openInventories.add(player.getUniqueId());
        player.openInventory(inventory);
    }

    public int getPlayerPage(UUID uuid) {
        return playerPages.getOrDefault(uuid, 1);
    }

    public boolean isPluginGui(UUID uuid) {
        return openInventories.contains(uuid);
    }

    public void removePlayer(UUID uuid) {
        playerPages.remove(uuid);
        openInventories.remove(uuid);
    }

    public PagedGuiRenderer getRenderer() {
        return renderer;
    }
}
