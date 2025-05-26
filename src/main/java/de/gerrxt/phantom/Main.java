package de.gerrxt.phantom;

import de.gerrxt.phantom.commands.DiscordCommand;
import de.gerrxt.phantom.commands.WhitelistCommand;
import de.gerrxt.phantom.discord.DiscordManager;
import de.gerrxt.phantom.util.ConsoleColor;
import de.gerrxt.phantom.util.DiscordWebhook;
import de.gerrxt.phantom.util.PlayerFreezeManager;
import de.gerrxt.phantom.util.WhitelistData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {
    
    private FileConfiguration config;
    private Logger logger;
    private ConsoleColor console;
    private DiscordWebhook discordWebhook;
    private boolean debug;
    private DiscordManager discordManager;
    private PlayerFreezeManager freezeManager;
    private WhitelistData whitelistData;

    @Override
    public void onEnable() {
        this.logger = getLogger();
        this.console = new ConsoleColor(logger);
        
        // Plugin-Header anzeigen
        console.section("PhantomWhitelist v" + getDescription().getVersion());
        
        // Verzeichnisse überprüfen
        checkDirectories();
        
        // Konfiguration laden
        saveDefaultConfig();
        config = getConfig();
        debug = config.getBoolean("debug", false);
        
        // Discord Webhook initialisieren
        discordWebhook = new DiscordWebhook(this);
        
        // Debug-Modus
        if (debug) {
            console.debug("Debug-Modus aktiviert!");
            if (discordWebhook != null && discordWebhook.isEnabled()) {
                discordWebhook.debug("Debug-Modus aktiviert!");
            }
        }
        
        // WhitelistData initialisieren
        whitelistData = WhitelistData.getInstance();
        whitelistData.initialize(this);
        
        // Manager initialisieren
        freezeManager = new PlayerFreezeManager(this);
        discordManager = new DiscordManager(this);
        
        // Discord-Bot starten
        discordManager.initialize();
        
        // Eventlistener registrieren
        getServer().getPluginManager().registerEvents(this, this);
        
        // Befehle registrieren
        WhitelistCommand whitelistCommand = new WhitelistCommand(this);
        getCommand("pwhitelist").setExecutor(whitelistCommand);
        getCommand("pwhitelist").setTabCompleter(whitelistCommand);
        
        DiscordCommand discordCommand = new DiscordCommand(this, discordManager, freezeManager);
        getCommand("discord").setExecutor(discordCommand);
        
        // Erfolgreiche Aktivierung
        console.success("PhantomWhitelist wurde erfolgreich aktiviert!");
        if (discordWebhook != null && discordWebhook.isEnabled()) {
            discordWebhook.success("PhantomWhitelist wurde erfolgreich aktiviert!");
        }
        console.info("Unterstützte Minecraft-Version: " + getServer().getVersion());
        console.info("Java-Version: " + System.getProperty("java.version"));
        
        // Überprüfen, ob Discord-Integration aktiviert ist
        if (config.getBoolean("discord.enabled", false)) {
            if (discordManager.isEnabled()) {
                console.success("Discord-Integration ist aktiviert und verbunden!");
                if (discordWebhook != null && discordWebhook.isEnabled()) {
                    discordWebhook.success("Discord-Integration ist aktiviert und verbunden!");
                }
            } else {
                console.warning("Discord-Integration ist aktiviert, aber nicht erfolgreich verbunden!");
                if (discordWebhook != null && discordWebhook.isEnabled()) {
                    discordWebhook.warning("Discord-Integration ist aktiviert, aber nicht erfolgreich verbunden!");
                }
            }
        }
        
        // Webhook-Status anzeigen
        if (discordWebhook.isEnabled()) {
            console.success("Discord Webhook-Logging ist aktiviert!");
        }
    }

    @Override
    public void onDisable() {
        console.info("PhantomWhitelist wird deaktiviert...");
        if (discordWebhook != null && discordWebhook.isEnabled()) {
            discordWebhook.info("PhantomWhitelist wird deaktiviert...");
        }
        
        // Discord-Client herunterfahren
        if (discordManager != null) {
            discordManager.shutdown();
        }
        
        // Webhook herunterfahren
        if (discordWebhook != null) {
            discordWebhook.shutdown();
        }
        
        console.success("PhantomWhitelist wurde erfolgreich deaktiviert!");
    }
    
    /**
     * Behandelt die Spieler-Login-Ereignisse und prüft die Whitelist
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (debug) {
            console.debug("Spieler " + event.getPlayer().getName() + " versucht sich anzumelden...");
        }
        
        // Wenn Discord-Integration aktiv ist und keine Server-Whitelist verwenden
        if (discordManager.isEnabled() && config.getBoolean("whitelist.enabled", true)) {
            Player player = event.getPlayer();
            String playerName = player.getName();
            
            // Falls der Spieler bereits verknüpft ist, prüfen wir die Rolle
            if (discordManager.isPlayerLinked(playerName)) {
                String discordId = discordManager.getDiscordIdByMinecraft(playerName);
                boolean hasRole = false;
                
                try {
                    // Synchron prüfen für das Login-Event
                    hasRole = discordManager.hasRequiredRole(discordId).get();
                } catch (Exception e) {
                    console.error("Fehler beim Prüfen der Discord-Rolle für " + playerName + ": " + e.getMessage());
                    if (discordWebhook != null && discordWebhook.isEnabled()) {
                        discordWebhook.error("Fehler beim Prüfen der Discord-Rolle für " + playerName + ": " + e.getMessage());
                    }
                }
                
                if (!hasRole) {
                    String kickMessage = config.getString("messages.discord.no-role", 
                            "Dein Discord-Konto hat nicht die erforderliche Rolle, um auf diesem Server zu spielen!");
                    event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, 
                            Component.text(kickMessage).color(NamedTextColor.RED));
                    
                    if (discordWebhook != null && discordWebhook.isEnabled()) {
                        discordWebhook.warning("Spieler " + playerName + " wurde abgelehnt: Keine erforderliche Discord-Rolle!");
                    }
                    
                    if (debug) {
                        console.debug("Spieler " + playerName + " wurde abgelehnt: Keine erforderliche Discord-Rolle!");
                    }
                }
                
                return;
            }
            
            // Der Spieler ist nicht verknüpft - wird nach dem Login gefriert
        }
        
        // Falls Discord nicht aktiv oder Whitelist deaktiviert, Standard-Whitelist-Verhalten verwenden
        else if (!event.getPlayer().isWhitelisted() && Bukkit.hasWhitelist()) {
            String kickMessage = config.getString("messages.not-whitelisted", 
                    "Du bist nicht auf der Whitelist dieses Servers!");
            
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, 
                    Component.text(kickMessage).color(NamedTextColor.RED));
            
            if (discordWebhook != null && discordWebhook.isEnabled()) {
                discordWebhook.warning("Spieler " + event.getPlayer().getName() + " wurde abgelehnt: Nicht auf der Whitelist!");
            }
            
            if (debug) {
                console.debug("Spieler " + event.getPlayer().getName() + 
                        " wurde abgelehnt: Nicht auf der Whitelist!");
            }
        }
    }
    
    /**
     * Behandelt die Spieler-Join-Ereignisse für Discord-Verifizierung
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Wenn Discord-Integration aktiv ist und der Spieler nicht verknüpft ist
        if (discordManager.isEnabled() && !discordManager.isPlayerLinked(player.getName())) {
            // Verzögerung für bessere Benutzererfahrung
            Bukkit.getScheduler().runTaskLater(this, () -> {
                // Spieler einfrieren und zur Eingabe des Discord-Namens auffordern
                freezeManager.freezePlayer(player);
                
                // Infomeldung im Chat
                player.sendMessage(Component.text("----- Discord-Verifizierung -----").color(NamedTextColor.GOLD));
                player.sendMessage(Component.text("Du musst deinen Discord-Namen eingeben, um auf diesem Server spielen zu können.").color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Verwende: /discord <Dein-Discord-Name>").color(NamedTextColor.GREEN));
                player.sendMessage(Component.text("Du hast 2 Minuten Zeit, um dich zu verifizieren.").color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text("---------------------------").color(NamedTextColor.GOLD));
                
                if (discordWebhook != null && discordWebhook.isEnabled()) {
                    discordWebhook.info("Spieler " + player.getName() + " wurde eingefroren für Discord-Verifizierung.");
                }
                
                if (debug) {
                    console.debug("Spieler " + player.getName() + " wurde eingefroren für Discord-Verifizierung.");
                }
            }, 20L); // 1 Sekunde Verzögerung
        }
    }
    
    /**
     * Hilfsmethode, um zu prüfen, ob alle benötigten Verzeichnisse existieren.
     * Erstellt die Verzeichnisse bei Bedarf.
     */
    private void checkDirectories() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            if (dataFolder.mkdirs()) {
                console.success("Datenverzeichnis erstellt: " + dataFolder.getAbsolutePath());
            } else {
                console.error("Konnte das Datenverzeichnis nicht erstellen!");
                if (discordWebhook != null && discordWebhook.isEnabled()) {
                    discordWebhook.error("Konnte das Datenverzeichnis nicht erstellen!");
                }
            }
        }
    }
    
    /**
     * Gibt den Discord-Manager zurück
     */
    public DiscordManager getDiscordManager() {
        return discordManager;
    }
    
    /**
     * Gibt den Freeze-Manager zurück
     */
    public PlayerFreezeManager getFreezeManager() {
        return freezeManager;
    }
    
    /**
     * Gibt die Konsole zurück
     */
    public ConsoleColor getConsole() {
        return console;
    }
    
    /**
     * Gibt den Discord-Webhook zurück
     */
    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }
}