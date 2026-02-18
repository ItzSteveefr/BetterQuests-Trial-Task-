package dev.itzstevee.quests.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration guiConfig;
    private FileConfiguration questsConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveResource("config.yml", false);
        plugin.saveResource("gui.yml", false);
        plugin.saveResource("quests.yml", false);

        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        guiConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "gui.yml"));
        questsConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "quests.yml"));
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        guiConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "gui.yml"));
        questsConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "quests.yml"));
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    public FileConfiguration getQuestsConfig() {
        return questsConfig;
    }

    public String getMongoUri() {
        return config.getString("mongodb.uri", "mongodb://localhost:27017");
    }

    public String getMongoDatabase() {
        return config.getString("mongodb.database", "quests");
    }

    public int getConnectionTimeoutMs() {
        return config.getInt("mongodb.connection-timeout-ms", 5000);
    }

    public int getSaveIntervalSeconds() {
        return config.getInt("save-interval-seconds", 30);
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "");
    }

    public String getPrefix() {
        return config.getString("messages.prefix", "&8[&bQuests&8] &r");
    }
}
