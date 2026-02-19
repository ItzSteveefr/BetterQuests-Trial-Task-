package dev.itzstevee.quests.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemBuilder {

    private Material material;
    private int amount = 1;
    private Component displayName;
    private List<Component> lore = new ArrayList<>();
    private Set<ItemFlag> itemFlags = new HashSet<>();
    private int customModelData = 0;

    private ItemBuilder(Material material) {
        this.material = material;
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder name(String legacyText) {
        if (legacyText == null || legacyText.isEmpty()) {
            this.displayName = Component.empty();
            return this;
        }
        this.displayName = Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(legacyText));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        this.lore = new ArrayList<>();
        if (lines != null) {
            for (String line : lines) {
                this.lore.add(Component.empty()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(LegacyComponentSerializer.legacyAmpersand().deserialize(line)));
            }
        }
        return this;
    }

    public ItemBuilder flags(List<String> flagNames) {
        this.itemFlags = new HashSet<>();
        if (flagNames != null) {
            for (String flagName : flagNames) {
                try {
                    itemFlags.add(ItemFlag.valueOf(flagName.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return this;
    }

    public ItemBuilder customModelData(int data) {
        this.customModelData = data;
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) {
                meta.displayName(displayName);
            }
            if (!lore.isEmpty()) {
                meta.lore(lore);
            }
            if (!itemFlags.isEmpty()) {
                meta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
            }
            if (customModelData != 0) {
                meta.setCustomModelData(customModelData);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
