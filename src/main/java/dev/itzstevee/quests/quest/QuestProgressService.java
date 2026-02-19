package dev.itzstevee.quests.quest;

import dev.itzstevee.quests.config.ConfigManager;
import dev.itzstevee.quests.data.PlayerDataCache;
import dev.itzstevee.quests.data.PlayerQuestData;
import dev.itzstevee.quests.reward.RewardService;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class QuestProgressService {

    private final PlayerDataCache playerDataCache;
    private final QuestRegistry questRegistry;
    private final ConfigManager configManager;
    private final RewardService rewardService;
    private final Logger logger;

    public QuestProgressService(PlayerDataCache playerDataCache, QuestRegistry questRegistry, ConfigManager configManager, RewardService rewardService, Logger logger) {
        this.playerDataCache = playerDataCache;
        this.questRegistry = questRegistry;
        this.configManager = configManager;
        this.rewardService = rewardService;
        this.logger = logger;
    }

    public void incrementProgress(Player player, Quest quest, int amount) {
        PlayerQuestData data = playerDataCache.getIfPresent(player.getUniqueId());
        if (data == null) return;
        if (data.isCompleted(quest.getId())) return;

        data.getActiveQuests().add(quest.getId());
        data.incrementProgress(quest.getId(), amount);

        if (data.getProgress(quest.getId()) >= quest.getRequiredAmount()) {
            completeQuest(player, quest, data);
        }
    }

    public boolean isCompleted(PlayerQuestData data, String questId) {
        return data.isCompleted(questId);
    }

    public boolean isActive(PlayerQuestData data, String questId) {
        return data.getActiveQuests().contains(questId);
    }

    public void completeQuest(Player player, Quest quest, PlayerQuestData data) {
        data.setProgress(quest.getId(), quest.getRequiredAmount());
        data.markCompleted(quest.getId());
        rewardService.giveRewards(player, quest);
    }

    public void setProgress(Player player, String questId, int value) {
        PlayerQuestData data = playerDataCache.getIfPresent(player.getUniqueId());
        if (data == null) return;

        data.setProgress(questId, value);
        data.getActiveQuests().add(questId);

        Quest quest = questRegistry.getQuest(questId);
        if (quest != null && value >= quest.getRequiredAmount()) {
            completeQuest(player, quest, data);
        }
    }

    public void resetQuest(Player player, String questId) {
        PlayerQuestData data = playerDataCache.getIfPresent(player.getUniqueId());
        if (data == null) return;
        data.resetQuest(questId);
    }
}
