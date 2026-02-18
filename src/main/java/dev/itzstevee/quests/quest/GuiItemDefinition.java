package dev.itzstevee.quests.quest;

public class GuiItemDefinition {

    private final String material;
    private final int amount;
    private final String name;
    private final java.util.List<String> lore;
    private final java.util.List<String> flags;
    private final int customModelData;

    public GuiItemDefinition(String material, int amount, String name, java.util.List<String> lore, java.util.List<String> flags, int customModelData) {
        this.material = material;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
        this.flags = flags;
        this.customModelData = customModelData;
    }

    public String getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public java.util.List<String> getLore() {
        return lore;
    }

    public java.util.List<String> getFlags() {
        return flags;
    }

    public int getCustomModelData() {
        return customModelData;
    }
}
