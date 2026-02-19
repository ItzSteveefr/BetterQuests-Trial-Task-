package dev.itzstevee.quests.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class ConfigItemParser {

    public static ItemBuilder fromSection(ConfigurationSection section) {
        String mat = section.getString("material", "STONE");
        int amount = section.getInt("amount", 1);
        String name = section.getString("name", "");
        List<String> lore = section.getStringList("lore");
        List<String> flags = section.getStringList("flags");
        int cmd = section.getInt("custom-model-data", 0);

        Material material = Material.matchMaterial(mat);
        if (material == null) {
            material = Material.STONE;
        }

        return ItemBuilder.of(material)
                .amount(amount)
                .name(name)
                .lore(lore)
                .flags(flags)
                .customModelData(cmd);
    }
}
