package de.gerrxt.phantom.commands;

import de.gerrxt.phantom.Main;
import de.gerrxt.phantom.discord.DiscordManager;
import de.gerrxt.phantom.util.PlayerFreezeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DiscordCommand implements CommandExecutor {
    
    private final Main plugin;
    private final DiscordManager discordManager;
    private final PlayerFreezeManager freezeManager;
    
    public DiscordCommand(Main plugin, DiscordManager discordManager, PlayerFreezeManager freezeManager) {
        this.plugin = plugin;
        this.discordManager = discordManager;
        this.freezeManager = freezeManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern verwendet werden!").color(NamedTextColor.RED));
            return true;
        }
        
        // Prüfen, ob Discord aktiviert ist
        if (!discordManager.isEnabled()) {
            player.sendMessage(Component.text("Die Discord-Integration ist derzeit deaktiviert.").color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(Component.text("Bitte gib deinen Discord-Namen ein!").color(NamedTextColor.RED));
            player.sendMessage(Component.text("Verwendung: /discord <Dein-Discord-Name>").color(NamedTextColor.YELLOW));
            return true;
        }
        
        // Discord-Name aus den Argumenten zusammenbauen (falls Leerzeichen enthalten)
        String discordName = String.join(" ", args);
        
        player.sendMessage(Component.text("Überprüfe Discord-Benutzer: " + discordName + "...").color(NamedTextColor.YELLOW));
        
        // Asynchron die Discord-ID abrufen
        CompletableFuture<String> discordIdFuture = discordManager.getDiscordIdByUsername(discordName);
        discordIdFuture.thenAccept(discordId -> {
            if (discordId == null) {
                // Discord-Benutzer nicht gefunden
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(Component.text("Konnte keinen Discord-Benutzer mit dem Namen " + discordName + " finden!").color(NamedTextColor.RED));
                    player.sendMessage(Component.text("Stelle sicher, dass du deinen exakten Discord-Namen angegeben hast.").color(NamedTextColor.YELLOW));
                });
                return;
            }
            
            // Prüfen, ob der Benutzer die erforderliche Rolle hat
            discordManager.hasRequiredRole(discordId).thenAccept(hasRole -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!hasRole) {
                        // Keine erforderliche Rolle
                        player.sendMessage(Component.text("Dein Discord-Konto hat nicht die erforderliche Rolle, um auf diesem Server zu spielen!").color(NamedTextColor.RED));
                        return;
                    }
                    
                    // Alles in Ordnung, Spieler verknüpfen
                    discordManager.linkPlayer(player.getName(), discordId);
                    
                    // Spieler entsperren, falls eingefroren
                    if (freezeManager.isPlayerFrozen(player)) {
                        freezeManager.unfreezePlayer(player);
                    }
                    
                    player.sendMessage(Component.text("Dein Minecraft-Konto wurde erfolgreich mit deinem Discord-Konto verknüpft!").color(NamedTextColor.GREEN));
                });
            });
        }).exceptionally(e -> {
            plugin.getLogger().severe("Fehler bei der Discord-Überprüfung: " + e.getMessage());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.sendMessage(Component.text("Bei der Überprüfung deines Discord-Kontos ist ein Fehler aufgetreten. Bitte versuche es später erneut.").color(NamedTextColor.RED));
            });
            return null;
        });
        
        return true;
    }
}
