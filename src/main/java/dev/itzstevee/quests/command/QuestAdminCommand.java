package dev.itzstevee.quests.command;

import dev.itzstevee.quests.config.ConfigManager;
import dev.itzstevee.quests.data.MongoService;
import dev.itzstevee.quests.data.PlayerDataCache;
import dev.itzstevee.quests.quest.Quest;
import dev.itzstevee.quests.quest.QuestProgressService;
import dev.itzstevee.quests.quest.QuestRegistry;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuestAdminCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager;
    private final QuestRegistry questRegistry;
    private final QuestProgressService questProgressService;
    private final PlayerDataCache playerDataCache;
    private final MongoService mongoService;
    private final Runnable reloadCallback;

    public QuestAdminCommand(ConfigManager configManager, QuestRegistry questRegistry, QuestProgressService questProgressService, PlayerDataCache playerDataCache, MongoService mongoService, Runnable reloadCallback) {
        this.configManager = configManager;
        this.questRegistry = questRegistry;
        this.questProgressService = questProgressService;
        this.playerDataCache = playerDataCache;
        this.mongoService = mongoService;
        this.reloadCallback = reloadCallback;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("quests.admin")) {
            sendMessage(sender, configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    "&eUsage: /questadmin <setprogress|reset|reload>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setprogress" -> handleSetProgress(sender, args);
            case "reset" -> handleReset(sender, args);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    "&eUsage: /questadmin <setprogress|reset|reload>"));
        }
        return true;
    }

    private void handleSetProgress(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sendMessage(sender, "&eUsage: /questadmin setprogress <player> <questId> <value>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, configManager.getMessage("player-not-found"));
            return;
        }

        String questId = args[2];
        Quest quest = questRegistry.getQuest(questId);
        if (quest == null) {
            sendMessage(sender, configManager.getMessage("quest-not-found").replace("%id%", questId));
            return;
        }

        int value;
        try {
            value = Integer.parseInt(args[3]);
            if (value < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sendMessage(sender, configManager.getMessage("invalid-value"));
            return;
        }

        questProgressService.setProgress(target, questId, value);
        mongoService.setProgress(target.getUniqueId(), questId, value);

        String msg = configManager.getMessage("progress-set")
                .replace("%player%", target.getName())
                .replace("%quest%", questId)
                .replace("%value%", String.valueOf(value));
        sendMessage(sender, configManager.getPrefix() + msg);
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, "&eUsage: /questadmin reset <player> <questId>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, configManager.getMessage("player-not-found"));
            return;
        }

        String questId = args[2];
        Quest quest = questRegistry.getQuest(questId);
        if (quest == null) {
            sendMessage(sender, configManager.getMessage("quest-not-found").replace("%id%", questId));
            return;
        }

        questProgressService.resetQuest(target, questId);
        mongoService.resetQuest(target.getUniqueId(), questId);

        String msg = configManager.getMessage("quest-reset")
                .replace("%player%", target.getName())
                .replace("%quest%", questId);
        sendMessage(sender, configManager.getPrefix() + msg);
    }

    private void handleReload(CommandSender sender) {
        reloadCallback.run();
        sendMessage(sender, configManager.getPrefix() + configManager.getMessage("reload-complete"));
    }

    private void sendMessage(CommandSender sender, String legacyText) {
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(legacyText));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("quests.admin")) return List.of();

        if (args.length == 1) {
            return filterStartsWith(Arrays.asList("setprogress", "reset", "reload"), args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("setprogress") || args[0].equalsIgnoreCase("reset"))) {
            return filterStartsWith(
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                    args[1]
            );
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("setprogress") || args[0].equalsIgnoreCase("reset"))) {
            return filterStartsWith(
                    questRegistry.getAllQuests().stream().map(Quest::getId).collect(Collectors.toList()),
                    args[2]
            );
        }

        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
