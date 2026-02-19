package dev.itzstevee.quests.listener;

import dev.itzstevee.quests.data.PlayerDataCache;
import dev.itzstevee.quests.data.PlayerQuestData;
import dev.itzstevee.quests.quest.Quest;
import dev.itzstevee.quests.quest.QuestProgressService;
import dev.itzstevee.quests.quest.QuestRegistry;
import dev.itzstevee.quests.quest.QuestType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.UUID;

public class ExploringListener implements Listener {

    private final PlayerDataCache cache;
    private final QuestRegistry questRegistry;
    private final QuestProgressService questProgressService;

    public ExploringListener(PlayerDataCache cache, QuestRegistry questRegistry, QuestProgressService questProgressService) {
        this.cache = cache;
        this.questRegistry = questRegistry;
        this.questProgressService = questProgressService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerQuestData data = cache.getIfPresent(uuid);
        if (data == null) return;

        String currentBiome = player.getLocation().getBlock().getBiome().getKey().getKey();

        List<Quest> relevant = questRegistry.getQuestsByType(QuestType.EXPLORING)
                .stream()
                .filter(q -> !questProgressService.isCompleted(data, q.getId()))
                .filter(q -> q.getTarget().equalsIgnoreCase(currentBiome))
                .toList();

        for (Quest quest : relevant) {
            questProgressService.incrementProgress(player, quest, 1);
        }
    }
}
