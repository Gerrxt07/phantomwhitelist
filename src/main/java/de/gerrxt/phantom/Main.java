package de.gerrxt.phantom;

import de.gerrxt.phantom.commands.DiscordCommand;
import de.gerrxt.phantom.commands.WhitelistCommand;
import de.gerrxt.phantom.discord.DiscordManager;
import de.gerrxt.phantom.util.ConsoleColor;
import de.gerrxt.phantom.util.DiscordWebhook;
import de.gerrxt.phantom.util.LanguageManager;
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
import java.util.Map;
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
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        this.logger = getLogger();
        this.console = new ConsoleColor(logger);
        
        // Plugin-Header anzeigen
        console.section("PhantomWhitelist v" + getPluginMeta().getVersion());
        
        // Verzeichnisse überprüfen
        checkDirectories();
        
        // Konfiguration laden
        saveDefaultConfig();
        config = getConfig();
        debug = config.getBoolean("debug", false);
        
        // Sprachmanager initialisieren
        languageManager = new LanguageManager(this);
        languageManager.initialize();
        
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
        console.success(languageManager.getMessage("plugin.enable"));
        if (discordWebhook != null && discordWebhook.isEnabled()) {
            discordWebhook.success(languageManager.getMessage("plugin.enable"));
        }
        console.info(languageManager.getMessage("plugin.version-info", "version", getServer().getVersion()));
        console.info(languageManager.getMessage("plugin.java-info", "version", System.getProperty("java.version")));
        
        // Überprüfen, ob Discord-Integration aktiviert ist
        if (config.getBoolean("discord.enabled", false)) {
            if (discordManager.isEnabled()) {
                console.success(languageManager.getMessage("discord.enabled"));
                if (discordWebhook != null && discordWebhook.isEnabled()) {
                    discordWebhook.success(languageManager.getMessage("discord.enabled"));
                }
            } else {
                console.warning(languageManager.getMessage("discord.not-connected"));
                if (discordWebhook != null && discordWebhook.isEnabled()) {
                    discordWebhook.warning(languageManager.getMessage("discord.not-connected"));
                }
            }
        }
        
        // Webhook-Status anzeigen
        if (discordWebhook.isEnabled()) {
            console.success(languageManager.getMessage("discord.webhook.enabled"));
        }
    }

    @Override
    public void onDisable() {
        console.info(languageManager.getMessage("plugin.disable"));
        if (discordWebhook != null && discordWebhook.isEnabled()) {
            try {
                discordWebhook.info(languageManager.getMessage("plugin.disable"));
            } catch (Exception e) {
                console.warning("Fehler beim Senden der Abschaltmeldung an Discord: " + e.getMessage());
            }
        }
        
        // Discord-Client herunterfahren
        if (discordManager != null) {
            try {
                discordManager.shutdown();
            } catch (Exception e) {
                console.error(languageManager.getMessage("discord.shutdown-error", "error", e.getMessage()));
            }
        }
        
        // Webhook herunterfahren
        if (discordWebhook != null) {
            try {
                discordWebhook.shutdown();
            } catch (Exception e) {
                console.error(languageManager.getMessage("discord.webhook.shutdown-error", "error", e.getMessage()));
            }
        }
        
        // Scheduler-Tasks bereinigen
        try {
            Bukkit.getScheduler().cancelTasks(this);
        } catch (Exception e) {
            console.error("Fehler beim Bereinigen der Scheduler-Tasks: " + e.getMessage());
        }
        
        console.success(languageManager.getMessage("plugin.disable"));
    }
    
    /**
     * Behandelt die Spieler-Login-Ereignisse und prüft die Whitelist
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (debug) {
            console.debug(languageManager.getMessage("whitelist.player.login-attempt", "player", event.getPlayer().getName()));
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
                    console.error(languageManager.getMessage("whitelist.player.discord-role-error", 
                            Map.of("player", playerName, "error", e.getMessage())));
                    if (discordWebhook != null && discordWebhook.isEnabled()) {
                        discordWebhook.error(languageManager.getMessage("whitelist.player.discord-role-error", 
                            Map.of("player", playerName, "error", e.getMessage())));
                    }
                }
                
                if (!hasRole) {
                    String kickMessage = languageManager.getMessage("verification.no-role");
                    event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, 
                            Component.text(kickMessage).color(NamedTextColor.RED));
                    
                    if (discordWebhook != null && discordWebhook.isEnabled()) {
                        discordWebhook.warning(languageManager.getMessage("whitelist.player.rejected-no-role", "player", playerName));
                    }
                    
                    if (debug) {
                        console.debug(languageManager.getMessage("whitelist.player.rejected-no-role", "player", playerName));
                    }
                }
                
                return;
            }
            
            // Der Spieler ist nicht verknüpft - wird nach dem Login gefriert
        }
        
        // Falls Discord nicht aktiv oder Whitelist deaktiviert, Standard-Whitelist-Verhalten verwenden
        else if (!event.getPlayer().isWhitelisted() && Bukkit.hasWhitelist()) {
            String kickMessage = languageManager.getMessage("whitelist.player.not-whitelisted");
            
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, 
                    Component.text(kickMessage).color(NamedTextColor.RED));
            
            if (discordWebhook != null && discordWebhook.isEnabled()) {
                discordWebhook.warning(languageManager.getMessage("whitelist.player.rejected-no-whitelist", "player", event.getPlayer().getName()));
            }
            
            if (debug) {
                console.debug(languageManager.getMessage("whitelist.player.rejected-no-whitelist", "player", event.getPlayer().getName()));
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
                player.sendMessage(Component.text(languageManager.getMessage("verification.header")).color(NamedTextColor.GOLD));
                player.sendMessage(Component.text(languageManager.getMessage("verification.required")).color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text(languageManager.getMessage("verification.instruction")).color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text(languageManager.getMessage("verification.usage")).color(NamedTextColor.GREEN));
                player.sendMessage(Component.text(languageManager.getMessage("freeze.timeout-info")).color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text(languageManager.getMessage("verification.separator")).color(NamedTextColor.GOLD));
                
                if (discordWebhook != null && discordWebhook.isEnabled()) {
                    discordWebhook.info(languageManager.getMessage("verification.player-frozen", "player", player.getName()));
                }
                
                if (debug) {
                    console.debug(languageManager.getMessage("verification.player-frozen", "player", player.getName()));
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
            try {
                if (dataFolder.mkdirs()) {
                    console.success("Datenverzeichnis erstellt: " + dataFolder.getAbsolutePath());
                } else {
                    console.error("Konnte das Datenverzeichnis nicht erstellen: " + dataFolder.getAbsolutePath());
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
            } catch (SecurityException e) {
                console.error("Sicherheitsfehler beim Erstellen des Datenverzeichnisses: " + e.getMessage());
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        
        // Zusätzliche wichtige Verzeichnisse prüfen
        File whitelistDir = new File(dataFolder, "whitelist");
        if (!whitelistDir.exists() && !whitelistDir.mkdirs()) {
            console.warning("Konnte Whitelist-Verzeichnis nicht erstellen: " + whitelistDir.getAbsolutePath());
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
     * Gibt den Sprachmanager zurück
     */
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    /**
     * Gibt den Discord-Webhook zurück
     */
    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }
}