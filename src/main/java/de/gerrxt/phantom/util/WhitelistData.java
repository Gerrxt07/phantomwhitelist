package de.gerrxt.phantom.util;

import de.gerrxt.phantom.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class WhitelistData {
    
    private static WhitelistData instance;
    
    private Main plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private ConsoleColor console;
    
    // Speichern der Daten im Cache für schnelleren Zugriff
    private final Map<String, String> playerDiscordLinks = new HashMap<>();
    
    private WhitelistData() {
        // Singleton-Pattern
    }
    
    public static synchronized WhitelistData getInstance() {
        if (instance == null) {
            instance = new WhitelistData();
        }
        return instance;
    }
    
    /**
     * Initialisiert die Datenstruktur für WhitelistData
     * 
     * @param plugin Die Plugin-Instanz
     */
    public void initialize(Main plugin) {
        if (this.plugin != null) {
            // Verhindert doppelte Initialisierung
            return;
        }
        
        this.plugin = plugin;
        this.console = plugin.getConsole();
        
        // Sicherstellen, dass das Datenverzeichnis existiert
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            console.error("Konnte das Datenverzeichnis nicht erstellen: " + dataFolder.getAbsolutePath());
            return;
        }
        
        // Daten-Datei einrichten
        dataFile = new File(dataFolder, "whitelist_data.yml");
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile()) {
                    console.success("Whitelist-Datendatei erstellt: " + dataFile.getAbsolutePath());
                } else {
                    console.error("Konnte die Whitelist-Datendatei nicht erstellen: " + dataFile.getAbsolutePath());
                    return;
                }
            } catch (IOException e) {
                console.exception("Konnte whitelist_data.yml nicht erstellen", e);
                return;
            }
        }
        
        // Konfiguration laden
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Cache mit Daten aus der Datei befüllen
        loadPlayerLinks();
    }
    
    /**
     * Lädt alle Spieler-Discord-Verknüpfungen aus der Datei in den Cache
     */
    private void loadPlayerLinks() {
        playerDiscordLinks.clear();
        
        if (dataConfig.contains("player-links")) {
            for (String playerName : dataConfig.getConfigurationSection("player-links").getKeys(false)) {
                String discordId = dataConfig.getString("player-links." + playerName);
                playerDiscordLinks.put(playerName.toLowerCase(), discordId);
            }
        }
        
        console.info("Whitelist-Daten geladen: " + playerDiscordLinks.size() + " Spieler-Verknüpfungen");
    }
    
    /**
     * Speichert die Verknüpfung zwischen einem Minecraft-Spieler und einer Discord-ID
     * 
     * @param playerName Der Minecraft-Spielername
     * @param discordId Die Discord-ID
     */
    public void savePlayerLink(String playerName, String discordId) {
        playerName = playerName.toLowerCase();
        
        // Im Cache speichern
        playerDiscordLinks.put(playerName, discordId);
        
        // In die Datei schreiben
        dataConfig.set("player-links." + playerName, discordId);
        saveConfig();
        
        console.success("Spieler " + playerName + " mit Discord-ID " + discordId + " verknüpft");
    }
    
    /**
     * Entfernt die Verknüpfung eines Spielers
     * 
     * @param playerName Der Minecraft-Spielername
     * @return true, wenn der Spieler entfernt wurde
     */
    public boolean removePlayerLink(String playerName) {
        playerName = playerName.toLowerCase();
        
        // Prüfen ob der Spieler verknüpft ist
        if (!isPlayerLinked(playerName)) {
            return false;
        }
        
        // Aus dem Cache entfernen
        playerDiscordLinks.remove(playerName);
        
        // Aus der Datei entfernen
        dataConfig.set("player-links." + playerName, null);
        saveConfig();
        
        console.warning("Verknüpfung für Spieler " + playerName + " entfernt");
        return true;
    }
    
    /**
     * Prüft, ob ein Spieler mit Discord verknüpft ist
     * 
     * @param playerName Der Minecraft-Spielername
     * @return true, wenn der Spieler verknüpft ist
     */
    public boolean isPlayerLinked(String playerName) {
        return playerDiscordLinks.containsKey(playerName.toLowerCase());
    }
    
    /**
     * Gibt die Discord-ID eines Minecraft-Spielers zurück
     * 
     * @param playerName Der Minecraft-Spielername
     * @return Die verknüpfte Discord-ID oder null
     */
    public String getDiscordId(String playerName) {
        return playerDiscordLinks.get(playerName.toLowerCase());
    }
    
    /**
     * Speichert die Konfiguration in die Datei
     */
    private void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            console.exception("Konnte whitelist_data.yml nicht speichern", e);
        }
    }
    
    /**
     * Neu laden der Daten aus der Datei
     */
    public void reload() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadPlayerLinks();
        console.info("Whitelist-Daten neu geladen");
    }
}
