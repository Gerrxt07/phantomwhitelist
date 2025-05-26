package de.gerrxt.phantom.commands;

import de.gerrxt.phantom.Main;
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
    
    public WhitelistCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("phantomwhitelist.reload")) {
                    sender.sendMessage(Component.text(plugin.getConfig().getString("messages.no-permission", 
                            "Du hast keine Berechtigung, diesen Befehl auszuführen.")).color(NamedTextColor.RED));
                    return true;
                }
                
                plugin.reloadConfig();
                sender.sendMessage(Component.text(plugin.getConfig().getString("messages.config-reloaded", 
                        "Konfiguration wurde neu geladen.")).color(NamedTextColor.GREEN));
                return true;
            }
            case "add" -> {
                if (!sender.hasPermission("phantomwhitelist.admin")) {
                    sender.sendMessage(Component.text(plugin.getConfig().getString("messages.no-permission", 
                            "Du hast keine Berechtigung, diesen Befehl auszuführen.")).color(NamedTextColor.RED));
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Verwendung: /pwhitelist add <Spielername>").color(NamedTextColor.RED));
                    return true;
                }
                
                // Hier kommt später die eigene Whitelist-Logik
                // Vorläufig verwenden wir die Server-Whitelist
                OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text(plugin.getConfig().getString("messages.player-not-found", 
                                    "Spieler {player} wurde nicht gefunden.")
                            .replace("{player}", args[1])).color(NamedTextColor.RED));
                    return true;
                }
                
                target.setWhitelisted(true);
                String message = plugin.getConfig().getString("messages.player-added", 
                                "Spieler {player} wurde zur Whitelist hinzugefügt.")
                        .replace("{player}", target.getName());
                sender.sendMessage(Component.text(message).color(NamedTextColor.GREEN));
                return true;
            }
            case "remove" -> {
                if (!sender.hasPermission("phantomwhitelist.admin")) {
                    sender.sendMessage(Component.text(plugin.getConfig().getString("messages.no-permission", 
                            "Du hast keine Berechtigung, diesen Befehl auszuführen.")).color(NamedTextColor.RED));
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Verwendung: /pwhitelist remove <Spielername>").color(NamedTextColor.RED));
                    return true;
                }
                
                // Hier kommt später die eigene Whitelist-Logik
                // Vorläufig verwenden wir die Server-Whitelist
                OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text(plugin.getConfig().getString("messages.player-not-found", 
                                    "Spieler {player} wurde nicht gefunden.")
                            .replace("{player}", args[1])).color(NamedTextColor.RED));
                    return true;
                }
                
                target.setWhitelisted(false);
                String message = plugin.getConfig().getString("messages.player-removed", 
                                "Spieler {player} wurde von der Whitelist entfernt.")
                        .replace("{player}", target.getName());
                sender.sendMessage(Component.text(message).color(NamedTextColor.RED));
                return true;
            }
            case "list" -> {
                if (!sender.hasPermission("phantomwhitelist.command")) {
                    sender.sendMessage(Component.text(plugin.getConfig().getString("messages.no-permission", 
                            "Du hast keine Berechtigung, diesen Befehl auszuführen.")).color(NamedTextColor.RED));
                    return true;
                }
                
                // Vorläufig verwenden wir die Server-Whitelist
                List<String> whitelistedPlayers = Arrays.stream(Bukkit.getWhitelistedPlayers().toArray(new OfflinePlayer[0]))
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.toList());
                
                sender.sendMessage(Component.text("Whitelist (" + whitelistedPlayers.size() + " Spieler):").color(NamedTextColor.YELLOW));
                if (whitelistedPlayers.isEmpty()) {
                    sender.sendMessage(Component.text("Die Whitelist ist leer.").color(NamedTextColor.GRAY));
                } else {
                    sender.sendMessage(Component.text(String.join(", ", whitelistedPlayers)).color(NamedTextColor.WHITE));
                }
                return true;
            }
            default -> {
                showHelp(sender);
                return true;
            }
        }
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("PhantomWhitelist Befehle:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/pwhitelist reload").color(NamedTextColor.GOLD)
                .append(Component.text(" - Lädt die Konfiguration neu").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/pwhitelist add <Spielername>").color(NamedTextColor.GOLD)
                .append(Component.text(" - Fügt einen Spieler zur Whitelist hinzu").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/pwhitelist remove <Spielername>").color(NamedTextColor.GOLD)
                .append(Component.text(" - Entfernt einen Spieler von der Whitelist").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/pwhitelist list").color(NamedTextColor.GOLD)
                .append(Component.text(" - Zeigt alle Spieler auf der Whitelist").color(NamedTextColor.GRAY)));
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
