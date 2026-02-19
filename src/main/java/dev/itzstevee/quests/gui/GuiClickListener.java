package dev.itzstevee.quests.gui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiClickListener implements Listener {

    private final GuiManager guiManager;
    private final FileConfiguration guiConfig;

    public GuiClickListener(GuiManager guiManager, FileConfiguration guiConfig) {
        this.guiManager = guiManager;
        this.guiConfig = guiConfig;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!guiManager.isPluginGui(player.getUniqueId())) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        int prevSlot = guiConfig.getInt("gui.previous-page-slot", 45);
        int nextSlot = guiConfig.getInt("gui.next-page-slot", 53);
        int closeSlot = guiConfig.getInt("gui.close-button-slot", 49);

        if (slot == prevSlot) {
            int currentPage = guiManager.getPlayerPage(player.getUniqueId());
            if (currentPage > 1) {
                guiManager.openGui(player, currentPage - 1);
            }
            return;
        }

        if (slot == nextSlot) {
            int currentPage = guiManager.getPlayerPage(player.getUniqueId());
            int totalPages = guiManager.getRenderer().getTotalPages();
            if (currentPage < totalPages) {
                guiManager.openGui(player, currentPage + 1);
            }
            return;
        }

        if (slot == closeSlot) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            guiManager.removePlayer(player.getUniqueId());
        }
    }
}
