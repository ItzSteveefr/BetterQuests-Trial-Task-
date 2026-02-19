package dev.itzstevee.quests.quest;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class QuestRegistry {

    private final Logger logger;
    private final Map<String, Quest> quests = new HashMap<>();

    public QuestRegistry(Logger logger) {
        this.logger = logger;
    }

    public void load(FileConfiguration questsConfig) {
        quests.clear();
        for (String key : questsConfig.getKeys(false)) {
            ConfigurationSection section = questsConfig.getConfigurationSection(key);
            if (section == null) continue;

            try {
                String id = section.getString("id", key);
                String typeStr = section.getString("type", "");
                QuestType type;
                try {
                    type = QuestType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid quest type '" + typeStr + "' for quest '" + id + "'. Skipping.");
                    continue;
                }

                String target = section.getString("target", "ANY");
                int requiredAmount = section.getInt("required-amount", 1);
                String rewardCommand = section.getString("reward-command", "");
                List<ItemStack> rewardItems = new ArrayList<>();

                if (type == QuestType.MINING) {
                    Material mat = Material.matchMaterial(target);
                    if (mat == null) {
                        logger.warning("Invalid material '" + target + "' for quest '" + id + "'. Skipping.");
                        continue;
                    }
                }

                if (type == QuestType.KILLING) {
                    try {
                        EntityType.valueOf(target.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid entity type '" + target + "' for quest '" + id + "'. Skipping.");
                        continue;
                    }
                }

                ConfigurationSection guiSection = section.getConfigurationSection("gui-item");
                GuiItemDefinition guiItem;
                if (guiSection != null) {
                    guiItem = new GuiItemDefinition(
                            guiSection.getString("material", "STONE"),
                            guiSection.getInt("amount", 1),
                            guiSection.getString("name", ""),
                            guiSection.getStringList("lore"),
                            guiSection.getStringList("flags"),
                            guiSection.getInt("custom-model-data", 0)
                    );
                } else {
                    guiItem = new GuiItemDefinition("STONE", 1, "", Collections.emptyList(), Collections.emptyList(), 0);
                }

                Quest quest = new Quest(id, type, target, requiredAmount, rewardCommand, rewardItems, guiItem);
                quests.put(id, quest);
            } catch (Exception e) {
                logger.warning("Failed to load quest '" + key + "': " + e.getMessage());
            }
        }
        logger.info("Loaded " + quests.size() + " quests.");
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    public List<Quest> getAllQuests() {
        return new ArrayList<>(quests.values());
    }

    public List<Quest> getQuestsByType(QuestType type) {
        List<Quest> result = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (quest.getType() == type) {
                result.add(quest);
            }
        }
        return result;
    }
}
