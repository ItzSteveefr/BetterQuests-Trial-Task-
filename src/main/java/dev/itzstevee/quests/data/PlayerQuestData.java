package dev.itzstevee.quests.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerQuestData {

    private final UUID uuid;
    private String playerName;
    private final Map<String, Integer> progress;
    private final Set<String> completedQuests;
    private final Set<String> activeQuests;

    public PlayerQuestData(UUID uuid, String playerName, Map<String, Integer> progress, Set<String> completedQuests, Set<String> activeQuests) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.progress = progress != null ? new HashMap<>(progress) : new HashMap<>();
        this.completedQuests = completedQuests != null ? new HashSet<>(completedQuests) : new HashSet<>();
        this.activeQuests = activeQuests != null ? new HashSet<>(activeQuests) : new HashSet<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Map<String, Integer> getProgress() {
        return progress;
    }

    public Set<String> getCompletedQuests() {
        return completedQuests;
    }

    public Set<String> getActiveQuests() {
        return activeQuests;
    }

    public int getProgress(String questId) {
        return progress.getOrDefault(questId, 0);
    }

    public void incrementProgress(String questId, int amount) {
        progress.merge(questId, amount, Integer::sum);
    }

    public void setProgress(String questId, int value) {
        progress.put(questId, value);
    }

    public void markCompleted(String questId) {
        completedQuests.add(questId);
        activeQuests.remove(questId);
    }

    public void resetQuest(String questId) {
        progress.remove(questId);
        completedQuests.remove(questId);
        activeQuests.remove(questId);
    }

    public boolean isCompleted(String questId) {
        return completedQuests.contains(questId);
    }
}
