package dev.itzstevee.quests.gui;

import dev.itzstevee.quests.config.ConfigManager;
import dev.itzstevee.quests.data.PlayerQuestData;
import dev.itzstevee.quests.item.ConfigItemParser;
import dev.itzstevee.quests.item.ItemBuilder;
import dev.itzstevee.quests.papi.PapiHelper;
import dev.itzstevee.quests.quest.Quest;
import dev.itzstevee.quests.quest.QuestRegistry;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PagedGuiRenderer {

    private final QuestRegistry questRegistry;
    private final ConfigManager configManager;

    public PagedGuiRenderer(QuestRegistry questRegistry, ConfigManager configManager) {
        this.questRegistry = questRegistry;
        this.configManager = configManager;
    }

    public int getTotalPages() {
        List<Integer> questSlots = getQuestSlots();
        int questsPerPage = questSlots.size();
        int total = questRegistry.getAllQuests().size();
        if (total == 0) return 1;
        return (int) Math.ceil((double) total / questsPerPage);
    }

    public Inventory renderPage(Player player, PlayerQuestData data, int page, int totalPages) {
        FileConfiguration gui = configManager.getGuiConfig();
        int rows = gui.getInt("gui.rows", 6);
        String title = gui.getString("gui.title", "&8Quest Journal");
        title = title.replace("%page%", String.valueOf(page)).replace("%total%", String.valueOf(totalPages));

        Inventory inventory = Bukkit.createInventory(null, rows * 9,
                LegacyComponentSerializer.legacyAmpersand().deserialize(title));

        fillBorder(inventory, gui);

        List<Integer> questSlots = getQuestSlots();
        List<Quest> allQuests = questRegistry.getAllQuests();
        int questsPerPage = questSlots.size();
        int startIndex = (page - 1) * questsPerPage;

        for (int i = 0; i < questSlots.size(); i++) {
            int questIndex = startIndex + i;
            if (questIndex < allQuests.size()) {
                Quest quest = allQuests.get(questIndex);
                ItemStack item = buildQuestItem(player, data, quest);
                inventory.setItem(questSlots.get(i), item);
            }
        }

        if (startIndex >= allQuests.size() && !allQuests.isEmpty()) {
            ConfigurationSection noQuestsSection = gui.getConfigurationSection("gui.no-quests-item");
            if (noQuestsSection != null) {
                ItemStack noQuests = ConfigItemParser.fromSection(noQuestsSection).build();
                inventory.setItem(questSlots.get(0), noQuests);
            }
        }

        int prevSlot = gui.getInt("gui.previous-page-slot", 45);
        int nextSlot = gui.getInt("gui.next-page-slot", 53);
        int closeSlot = gui.getInt("gui.close-button-slot", 49);

        if (page > 1) {
            ConfigurationSection prevSection = gui.getConfigurationSection("gui.previous-page-item");
            if (prevSection != null) {
                inventory.setItem(prevSlot, ConfigItemParser.fromSection(prevSection).build());
            }
        }

        if (page < totalPages) {
            ConfigurationSection nextSection = gui.getConfigurationSection("gui.next-page-item");
            if (nextSection != null) {
                inventory.setItem(nextSlot, ConfigItemParser.fromSection(nextSection).build());
            }
        }

        ConfigurationSection closeSection = gui.getConfigurationSection("gui.close-button-item");
        if (closeSection != null) {
            inventory.setItem(closeSlot, ConfigItemParser.fromSection(closeSection).build());
        }

        return inventory;
    }

    private ItemStack buildQuestItem(Player player, PlayerQuestData data, Quest quest) {
        String status;
        if (data.isCompleted(quest.getId())) {
            status = configManager.getMessage("status-completed");
        } else if (data.getActiveQuests().contains(quest.getId())) {
            status = configManager.getMessage("status-in-progress");
        } else {
            status = configManager.getMessage("status-not-started");
        }

        int progress = data.getProgress(quest.getId());
        int required = quest.getRequiredAmount();
        String progressBar = buildProgressBar(progress, required);

        String name = quest.getGuiItem().getName();
        name = name.replace("%progress%", String.valueOf(progress))
                .replace("%required%", String.valueOf(required))
                .replace("%percentage%", progressBar)
                .replace("%status%", status);
        name = PapiHelper.parse(player, name);

        List<String> rawLore = quest.getGuiItem().getLore();
        List<String> parsedLore = new ArrayList<>();
        for (String line : rawLore) {
            String parsed = line.replace("%progress%", String.valueOf(progress))
                    .replace("%required%", String.valueOf(required))
                    .replace("%percentage%", progressBar)
                    .replace("%status%", status);
            parsed = PapiHelper.parse(player, parsed);
            parsedLore.add(parsed);
        }

        Material material = Material.matchMaterial(quest.getGuiItem().getMaterial());
        if (material == null) material = Material.STONE;

        return ItemBuilder.of(material)
                .amount(quest.getGuiItem().getAmount())
                .name(name)
                .lore(parsedLore)
                .flags(quest.getGuiItem().getFlags())
                .customModelData(quest.getGuiItem().getCustomModelData())
                .build();
    }

    private String buildProgressBar(int current, int required) {
        FileConfiguration gui = configManager.getGuiConfig();
        int barLength = gui.getInt("progress-bar.length", 10);
        String filledChar = gui.getString("progress-bar.filled-char", "■");
        String emptyChar = gui.getString("progress-bar.empty-char", "□");
        String filledColor = gui.getString("progress-bar.filled-color", "&a");
        String emptyColor = gui.getString("progress-bar.empty-color", "&7");

        int filledCount = required > 0 ? (int) Math.round((double) current / required * barLength) : 0;
        filledCount = Math.min(filledCount, barLength);

        return filledColor + filledChar.repeat(filledCount)
                + emptyColor + emptyChar.repeat(barLength - filledCount);
    }

    private void fillBorder(Inventory inventory, FileConfiguration gui) {
        List<Integer> fillerSlots = gui.getIntegerList("gui.filler-slots");
        ConfigurationSection fillerSection = gui.getConfigurationSection("gui.filler-item");
        if (fillerSection == null) return;

        ItemStack filler = ConfigItemParser.fromSection(fillerSection).build();
        for (int slot : fillerSlots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private List<Integer> getQuestSlots() {
        return configManager.getGuiConfig().getIntegerList("gui.quest-slots");
    }
}
