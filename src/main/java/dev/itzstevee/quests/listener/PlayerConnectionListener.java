package dev.itzstevee.quests.listener;

import dev.itzstevee.quests.data.MongoService;
import dev.itzstevee.quests.data.PlayerDataCache;
import dev.itzstevee.quests.data.PlayerQuestData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.logging.Logger;

public class PlayerConnectionListener implements Listener {

    private final PlayerDataCache cache;
    private final MongoService mongoService;
    private final Logger logger;

    public PlayerConnectionListener(PlayerDataCache cache, MongoService mongoService, Logger logger) {
        this.cache = cache;
        this.mongoService = mongoService;
        this.logger = logger;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        cache.get(uuid).thenAccept(data -> {
            data.setPlayerName(player.getName());
        }).exceptionally(ex -> {
            logger.warning("Failed to load quest data for " + player.getName() + ": " + ex.getMessage());
            return null;
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerQuestData data = cache.getIfPresent(uuid);
        if (data != null) {
            mongoService.savePlayer(data).exceptionally(ex -> {
                logger.warning("Failed to save quest data for " + event.getPlayer().getName() + ": " + ex.getMessage());
                return null;
            });
        }
        cache.invalidate(uuid);
    }
}
