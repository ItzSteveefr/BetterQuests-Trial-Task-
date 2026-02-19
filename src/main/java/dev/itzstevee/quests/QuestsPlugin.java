package dev.itzstevee.quests;

import dev.itzstevee.quests.command.QuestAdminCommand;
import dev.itzstevee.quests.command.QuestsCommand;
import dev.itzstevee.quests.config.ConfigManager;
import dev.itzstevee.quests.data.MongoService;
import dev.itzstevee.quests.data.PlayerDataCache;
import dev.itzstevee.quests.gui.GuiClickListener;
import dev.itzstevee.quests.gui.GuiManager;
import dev.itzstevee.quests.listener.BlockPlaceListener;
import dev.itzstevee.quests.listener.ExploringListener;
import dev.itzstevee.quests.listener.KillingListener;
import dev.itzstevee.quests.listener.MiningListener;
import dev.itzstevee.quests.listener.PlayerConnectionListener;
import dev.itzstevee.quests.listener.RunningListener;
import dev.itzstevee.quests.papi.PapiHelper;
import dev.itzstevee.quests.papi.QuestsExpansion;
import dev.itzstevee.quests.quest.QuestProgressService;
import dev.itzstevee.quests.quest.QuestRegistry;
import dev.itzstevee.quests.reward.RewardService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class QuestsPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MongoService mongoService;
    private QuestRegistry questRegistry;
    private PlayerDataCache playerDataCache;
    private QuestProgressService questProgressService;
    private GuiManager guiManager;
    private RewardService rewardService;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();

        mongoService = new MongoService(configManager, getLogger());
        boolean connected = mongoService.connect();
        if (!connected) {
            getLogger().severe("Failed to connect to MongoDB! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        questRegistry = new QuestRegistry(getLogger());
        questRegistry.load(configManager.getQuestsConfig());

        playerDataCache = new PlayerDataCache(mongoService, getLogger());

        rewardService = new RewardService(configManager);

        questProgressService = new QuestProgressService(playerDataCache, questRegistry, configManager, rewardService, getLogger());

        guiManager = new GuiManager(this, questRegistry, playerDataCache, configManager);

        registerListeners();

        registerCommands();

        PapiHelper.init();
        if (PapiHelper.isEnabled()) {
            new QuestsExpansion(this).register();
            getLogger().info("PlaceholderAPI hooked successfully.");
        }

        int saveInterval = configManager.getSaveIntervalSeconds();
        getServer().getScheduler().runTaskTimerAsynchronously(this,
                playerDataCache::saveAll, 20L * saveInterval, 20L * saveInterval);

        getLogger().info("BetterQuests enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (playerDataCache != null) {
            playerDataCache.saveAllSync();
        }
        if (mongoService != null) {
            mongoService.disconnect();
        }
        getLogger().info("BetterQuests disabled. All player data saved.");
    }

    private void registerListeners() {
        BlockPlaceListener blockPlaceListener = new BlockPlaceListener();
        getServer().getPluginManager().registerEvents(blockPlaceListener, this);
        getServer().getPluginManager().registerEvents(new MiningListener(playerDataCache, questRegistry, questProgressService, blockPlaceListener), this);
        getServer().getPluginManager().registerEvents(new KillingListener(playerDataCache, questRegistry, questProgressService), this);
        getServer().getPluginManager().registerEvents(new RunningListener(playerDataCache, questRegistry, questProgressService), this);
        getServer().getPluginManager().registerEvents(new ExploringListener(playerDataCache, questRegistry, questProgressService), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(playerDataCache, mongoService, getLogger()), this);
        getServer().getPluginManager().registerEvents(new GuiClickListener(guiManager, configManager.getGuiConfig()), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("quests")).setExecutor(new QuestsCommand(guiManager));

        QuestAdminCommand adminCommand = new QuestAdminCommand(
                configManager, questRegistry, questProgressService,
                playerDataCache, mongoService, this::reloadPlugin
        );
        Objects.requireNonNull(getCommand("questadmin")).setExecutor(adminCommand);
        Objects.requireNonNull(getCommand("questadmin")).setTabCompleter(adminCommand);
    }

    private void reloadPlugin() {
        configManager.reload();
        questRegistry.load(configManager.getQuestsConfig());
    }

    public PlayerDataCache getPlayerDataCache() {
        return playerDataCache;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public QuestRegistry getQuestRegistry() {
        return questRegistry;
    }
}
