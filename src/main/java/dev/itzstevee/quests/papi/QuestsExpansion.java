package dev.itzstevee.quests.papi;

import dev.itzstevee.quests.QuestsPlugin;
import dev.itzstevee.quests.data.PlayerQuestData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuestsExpansion extends PlaceholderExpansion {

    private final QuestsPlugin plugin;

    public QuestsExpansion(QuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "quests";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ItzStevee";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return null;

        UUID uuid = offlinePlayer.getUniqueId();
        PlayerQuestData data = plugin.getPlayerDataCache().getIfPresent(uuid);

        if (params.startsWith("progress_")) {
            String questId = params.substring("progress_".length());
            if (data == null) return "0";
            return String.valueOf(data.getProgress(questId));
        }

        if (params.equals("completed_total")) {
            if (data == null) return "0";
            return String.valueOf(data.getCompletedQuests().size());
        }

        return null;
    }
}
