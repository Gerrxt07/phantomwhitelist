package de.gerrxt.phantom.util;

import de.gerrxt.phantom.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerFreezeManager implements Listener {
    
    private final Main plugin;
    private final Map<UUID, Long> frozenPlayers = new HashMap<>();
    private final Map<UUID, Integer> reminderTasks = new HashMap<>();
    
    public PlayerFreezeManager(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Friert einen Spieler ein, bis er seinen Discord-Namen eingibt
     * 
     * @param player Der einzufrierende Spieler
     */
    public void freezePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Spieler einfrieren
        frozenPlayers.put(playerId, System.currentTimeMillis() + (2 * 60 * 1000)); // 2 Minuten
        
        // Nachricht anzeigen
        String message = plugin.getConfig().getString("messages.verification.enter-discord", 
                "Du musst deinen Discord-Namen eingeben, um zu spielen!");
        String subtitle = plugin.getConfig().getString("messages.verification.enter-command", 
                "Verwende /discord <Dein-Discord-Name>");
        
        // Titel-Zeiten erstellen
        Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(1));
        
        // Titel erstellen und anzeigen
        Title title = Title.title(
                Component.text(message).color(NamedTextColor.RED),
                Component.text(subtitle).color(NamedTextColor.YELLOW),
                times
        );
        
        player.showTitle(title);
        
        // Regelmäßige Erinnerungen einrichten
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (frozenPlayers.containsKey(playerId) && player.isOnline()) {
                player.sendActionBar(Component.text(subtitle).color(NamedTextColor.YELLOW));
                
                // Alle 20 Sekunden ein Titel anzeigen
                if (Bukkit.getCurrentTick() % 400 == 0) {
                    player.showTitle(title);
                }
                
                // Prüfen, ob die Zeit abgelaufen ist
                long endTime = frozenPlayers.get(playerId);
                if (System.currentTimeMillis() > endTime) {
                    player.kick(Component.text(plugin.getConfig().getString("messages.verification.timeout", 
                            "Zeit abgelaufen! Bitte versuche es später erneut.")).color(NamedTextColor.RED));
                    unfreezePlayer(player);
                }
            } else {
                // Aufgabe beenden, wenn der Spieler nicht mehr eingefroren ist
                cancelReminderTask(playerId);
            }
        }, 20L, 20L); // Alle 1 Sekunde ausführen
        
        reminderTasks.put(playerId, taskId);
    }
    
    /**
     * Entsperrt einen eingefrorenen Spieler
     * 
     * @param player Der zu entsperrende Spieler
     */
    public void unfreezePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Spieler aus der Liste entfernen
        frozenPlayers.remove(playerId);
        
        // Erinnerungsaufgabe beenden
        cancelReminderTask(playerId);
        
        // Titel-Zeiten erstellen
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1));
        
        // Erfolgsnachricht anzeigen
        player.showTitle(Title.title(
                Component.text(plugin.getConfig().getString("messages.verification.success", 
                        "Verifizierung erfolgreich!")).color(NamedTextColor.GREEN),
                Component.text(plugin.getConfig().getString("messages.verification.welcome", 
                        "Willkommen auf dem Server!")).color(NamedTextColor.YELLOW),
                times
        ));
    }
    
    /**
     * Beendet die Erinnerungsaufgabe für einen Spieler
     * 
     * @param playerId Die UUID des Spielers
     */
    private void cancelReminderTask(UUID playerId) {
        if (reminderTasks.containsKey(playerId)) {
            Bukkit.getScheduler().cancelTask(reminderTasks.get(playerId));
            reminderTasks.remove(playerId);
        }
    }
    
    /**
     * Prüft, ob ein Spieler eingefroren ist
     * 
     * @param player Der zu prüfende Spieler
     * @return true, wenn der Spieler eingefroren ist
     */
    public boolean isPlayerFrozen(Player player) {
        return frozenPlayers.containsKey(player.getUniqueId());
    }
    
    /**
     * Verhindert, dass eingefrorene Spieler sich bewegen
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isPlayerFrozen(event.getPlayer())) {
            // Nur Bewegungen mit Positionsveränderung blockieren (Kopf drehen erlauben)
            if (event.getFrom().getX() != event.getTo().getX() || 
                    event.getFrom().getY() != event.getTo().getY() || 
                    event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Verhindert, dass eingefrorene Spieler Schaden nehmen
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isPlayerFrozen(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Entfernt Spieler aus der Liste, wenn sie den Server verlassen
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        frozenPlayers.remove(playerId);
        cancelReminderTask(playerId);
    }
}
