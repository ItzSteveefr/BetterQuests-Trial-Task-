package dev.itzstevee.quests.quest;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Quest {

    private final String id;
    private final QuestType type;
    private final String target;
    private final int requiredAmount;
    private final String rewardCommand;
    private final List<ItemStack> rewardItems;
    private final GuiItemDefinition guiItem;

    public Quest(String id, QuestType type, String target, int requiredAmount, String rewardCommand, List<ItemStack> rewardItems, GuiItemDefinition guiItem) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.requiredAmount = requiredAmount;
        this.rewardCommand = rewardCommand;
        this.rewardItems = rewardItems;
        this.guiItem = guiItem;
    }

    public String getId() {
        return id;
    }

    public QuestType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public String getRewardCommand() {
        return rewardCommand;
    }

    public List<ItemStack> getRewardItems() {
        return rewardItems;
    }

    public GuiItemDefinition getGuiItem() {
        return guiItem;
    }
}
