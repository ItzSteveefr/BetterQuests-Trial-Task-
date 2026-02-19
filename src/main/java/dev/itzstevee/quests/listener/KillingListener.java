package dev.itzstevee.quests.listener;

import dev.itzstevee.quests.data.PlayerDataCache;
import dev.itzstevee.quests.data.PlayerQuestData;
import dev.itzstevee.quests.quest.Quest;
import dev.itzstevee.quests.quest.QuestProgressService;
import dev.itzstevee.quests.quest.QuestRegistry;
import dev.itzstevee.quests.quest.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;
import java.util.UUID;

public class KillingListener implements Listener {

    private final PlayerDataCache cache;
    private final QuestRegistry questRegistry;
    private final QuestProgressService questProgressService;

    public KillingListener(PlayerDataCache cache, QuestRegistry questRegistry, QuestProgressService questProgressService) {
        this.cache = cache;
        this.questRegistry = questRegistry;
        this.questProgressService = questProgressService;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        UUID uuid = killer.getUniqueId();
        PlayerQuestData data = cache.getIfPresent(uuid);
        if (data == null) return;

        String entityType = event.getEntity().getType().name();

        List<Quest> relevant = questRegistry.getQuestsByType(QuestType.KILLING)
                .stream()
                .filter(q -> !questProgressService.isCompleted(data, q.getId()))
                .filter(q -> q.getTarget().equalsIgnoreCase(entityType))
                .toList();

        for (Quest quest : relevant) {
            questProgressService.incrementProgress(killer, quest, 1);
        }
    }
}
