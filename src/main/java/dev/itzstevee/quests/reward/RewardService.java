package dev.itzstevee.quests.reward;

import dev.itzstevee.quests.config.ConfigManager;
import dev.itzstevee.quests.papi.PapiHelper;
import dev.itzstevee.quests.quest.Quest;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class RewardService {

    private final ConfigManager configManager;

    public RewardService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void giveRewards(Player player, Quest quest) {
        String command = quest.getRewardCommand();
        if (command != null && !command.isEmpty() && !command.equalsIgnoreCase("none")) {
            String parsed = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        if (quest.getRewardItems() != null) {
            for (ItemStack item : quest.getRewardItems()) {
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item.clone());
                for (ItemStack drop : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }

        String message = configManager.getMessage("quest-complete");
        if (message != null && !message.isEmpty()) {
            String prefix = configManager.getPrefix();
            String full = prefix + message.replace("%quest_name%", quest.getGuiItem().getName());
            full = PapiHelper.parse(player, full);
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(full));
        }
    }
}
