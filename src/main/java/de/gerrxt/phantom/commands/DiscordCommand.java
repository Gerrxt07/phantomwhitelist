package de.gerrxt.phantom.commands;

import de.gerrxt.phantom.Main;
import de.gerrxt.phantom.discord.DiscordManager;
import de.gerrxt.phantom.util.LanguageManager;
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
    private final LanguageManager lang;
    
    public DiscordCommand(Main plugin, DiscordManager discordManager, PlayerFreezeManager freezeManager) {
        this.plugin = plugin;
        this.discordManager = discordManager;
        this.freezeManager = freezeManager;
        this.lang = plugin.getLanguageManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getMessage("verification.player-only")).color(NamedTextColor.RED));
            return true;
        }
        
        // Prüfen, ob Discord aktiviert ist
        if (!discordManager.isEnabled()) {
            player.sendMessage(Component.text(lang.getMessage("discord.disabled")).color(NamedTextColor.RED));
            return true;
        }
          if (args.length < 1) {
            player.sendMessage(Component.text(lang.getMessage("verification.enter-discord-name")).color(NamedTextColor.RED));
            player.sendMessage(Component.text(lang.getMessage("verification.usage")).color(NamedTextColor.YELLOW));
            return true;
        }
        
        // Discord-Name aus den Argumenten zusammenbauen (falls Leerzeichen enthalten)
        String discordName = String.join(" ", args);
        
        player.sendMessage(Component.text(lang.getMessage("verification.checking", "user", discordName)).color(NamedTextColor.YELLOW));
        
        // Asynchron die Discord-ID abrufen
        CompletableFuture<String> discordIdFuture = discordManager.getDiscordIdByUsername(discordName);
        discordIdFuture.thenAccept(discordId -> {
            if (discordId == null) {
                // Discord-Benutzer nicht gefunden
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(Component.text(lang.getMessage("verification.not-found", "user", discordName)).color(NamedTextColor.RED));
                    player.sendMessage(Component.text(lang.getMessage("verification.name-check")).color(NamedTextColor.YELLOW));
                });
                return;
            }
            
            // Prüfen, ob der Benutzer die erforderliche Rolle hat
            discordManager.hasRequiredRole(discordId).thenAccept(hasRole -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!hasRole) {
                        // Keine erforderliche Rolle
                        player.sendMessage(Component.text(lang.getMessage("verification.no-role")).color(NamedTextColor.RED));
                        return;
                    }
                      // Alles in Ordnung, Spieler verknüpfen
                    discordManager.linkPlayer(player.getName(), discordId);
                    
                    // Spieler entsperren, falls eingefroren
                    if (freezeManager.isPlayerFrozen(player)) {
                        freezeManager.unfreezePlayer(player);
                    }
                    
                    player.sendMessage(Component.text(lang.getMessage("verification.success")).color(NamedTextColor.GREEN));
                });
            });
        }).exceptionally(e -> {
            plugin.getLogger().severe("Fehler bei der Discord-Überprüfung: " + e.getMessage());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.sendMessage(Component.text(lang.getMessage("error.generic", "error", e.getMessage())).color(NamedTextColor.RED));
            });
            return null;
        });
        
        return true;
    }
}
