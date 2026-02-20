package dev.itzstevee.quests.gui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiClickListener implements Listener {

    private final GuiManager guiManager;
    private final int previousPageSlot;
    private final int nextPageSlot;
    private final int closeButtonSlot;

    public GuiClickListener(GuiManager guiManager, FileConfiguration guiConfig) {
        this.guiManager = guiManager;
        this.previousPageSlot = guiConfig.getInt("gui.previous-page-slot", 45);
        this.nextPageSlot = guiConfig.getInt("gui.next-page-slot", 53);
        this.closeButtonSlot = guiConfig.getInt("gui.close-button-slot", 49);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!guiManager.isPluginGui(player.getUniqueId())) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot == previousPageSlot) {
            int currentPage = guiManager.getPlayerPage(player.getUniqueId());
            if (currentPage > 1) {
                guiManager.openGui(player, currentPage - 1);
            }
            return;
        }

        if (slot == nextPageSlot) {
            int currentPage = guiManager.getPlayerPage(player.getUniqueId());
            int totalPages = guiManager.getRenderer().getTotalPages();
            if (currentPage < totalPages) {
                guiManager.openGui(player, currentPage + 1);
            }
            return;
        }

        if (slot == closeButtonSlot) {
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
