package de.gerrxt.phantom.commands;

import de.gerrxt.phantom.Main;
import de.gerrxt.phantom.util.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

public class WhitelistCommand implements CommandExecutor, TabCompleter {
    
    private final Main plugin;
    private final LanguageManager lang;
    
    public WhitelistCommand(Main plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {            case "reload" -> {
                if (!sender.hasPermission("phantomwhitelist.reload")) {
                    sender.sendMessage(Component.text(lang.getMessage("whitelist.command.no-permission")).color(NamedTextColor.RED));
                    return true;
                }
                
                plugin.reloadConfig();
                plugin.getLanguageManager().reload(); // Auch Sprachdateien neu laden
                sender.sendMessage(Component.text(lang.getMessage("plugin.reload")).color(NamedTextColor.GREEN));
                return true;
            }
            case "add" -> {
                if (!sender.hasPermission("phantomwhitelist.admin")) {
                    sender.sendMessage(Component.text(lang.getMessage("whitelist.command.no-permission")).color(NamedTextColor.RED));
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(Component.text(lang.getMessage("whitelist.command.help-add")).color(NamedTextColor.RED));
                    return true;
                }
                
                // Hier kommt später die eigene Whitelist-Logik
                // Vorläufig verwenden wir die Server-Whitelist
                OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text(lang.getMessage("whitelist.command.player-not-found", "player", args[1])).color(NamedTextColor.RED));
                    return true;
                }
                
                target.setWhitelisted(true);
                sender.sendMessage(Component.text(lang.getMessage("whitelist.command.player-added", "player", target.getName())).color(NamedTextColor.GREEN));
                return true;
            }            case "remove" -> {
                if (!sender.hasPermission("phantomwhitelist.admin")) {
                    sender.sendMessage(Component.text(lang.getMessage("whitelist.command.no-permission")).color(NamedTextColor.RED));
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(Component.text(lang.getMessage("whitelist.command.help-remove")).color(NamedTextColor.RED));
                    return true;
                }
                
                // Hier kommt später die eigene Whitelist-Logik
                // Vorläufig verwenden wir die Server-Whitelist
                OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text(lang.getMessage("whitelist.command.player-not-found", "player", args[1])).color(NamedTextColor.RED));
                    return true;
                }
                
                target.setWhitelisted(false);
                sender.sendMessage(Component.text(lang.getMessage("whitelist.command.player-removed", "player", target.getName())).color(NamedTextColor.RED));
                return true;
            }
            case "list" -> {
                if (!sender.hasPermission("phantomwhitelist.command")) {
                    sender.sendMessage(Component.text(lang.getMessage("whitelist.command.no-permission")).color(NamedTextColor.RED));
                    return true;
                }
                
                // Vorläufig verwenden wir die Server-Whitelist
                List<String> whitelistedPlayers = Arrays.stream(Bukkit.getWhitelistedPlayers().toArray(new OfflinePlayer[0]))
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.toList());
                
                sender.sendMessage(Component.text(lang.getMessage("whitelist.command.list-title") + " (" + whitelistedPlayers.size() + "):").color(NamedTextColor.YELLOW));
                if (whitelistedPlayers.isEmpty()) {
                    sender.sendMessage(Component.text(lang.getMessage("whitelist.command.list-empty")).color(NamedTextColor.GRAY));
                } else {
                    sender.sendMessage(Component.text(String.join(", ", whitelistedPlayers)).color(NamedTextColor.WHITE));
                }
                return true;            }
            default -> {
                showHelp(sender);
                return true;
            }
        }
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text(lang.getMessage("whitelist.command.help-title")).color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(lang.getMessage("whitelist.command.help-reload").split(" - ")[0]).color(NamedTextColor.GOLD)
                .append(Component.text(" - " + lang.getMessage("whitelist.command.help-reload").split(" - ")[1]).color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text(lang.getMessage("whitelist.command.help-add").split(" - ")[0]).color(NamedTextColor.GOLD)
                .append(Component.text(" - " + lang.getMessage("whitelist.command.help-add").split(" - ")[1]).color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text(lang.getMessage("whitelist.command.help-remove").split(" - ")[0]).color(NamedTextColor.GOLD)
                .append(Component.text(" - " + lang.getMessage("whitelist.command.help-remove").split(" - ")[1]).color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text(lang.getMessage("whitelist.command.help-list").split(" - ")[0]).color(NamedTextColor.GOLD)
                .append(Component.text(" - " + lang.getMessage("whitelist.command.help-list").split(" - ")[1]).color(NamedTextColor.GRAY)));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission("phantomwhitelist.reload")) subCommands.add("reload");
            if (sender.hasPermission("phantomwhitelist.admin")) {
                subCommands.add("add");
                subCommands.add("remove");
            }
            if (sender.hasPermission("phantomwhitelist.command")) subCommands.add("list");
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                for (OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
                    if (player.getName() != null && player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}
