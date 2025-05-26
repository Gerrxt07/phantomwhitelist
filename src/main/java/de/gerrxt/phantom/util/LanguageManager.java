package de.gerrxt.phantom.util;

import de.gerrxt.phantom.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Verwaltet die Übersetzungen und Sprachen für das Plugin
 */
public class LanguageManager {
    private final Main plugin;
    private final ConsoleColor console;
    private FileConfiguration languageConfig;
    private String currentLanguage;
    private final Map<String, FileConfiguration> languageCache = new HashMap<>();
    private final String defaultLanguage = "en";
    
    /**
     * Liste der unterstützten Sprachen
     */
    public static final String[] SUPPORTED_LANGUAGES = {
            "en", // Englisch
            "de", // Deutsch
            "fr", // Französisch
            "es", // Spanisch
            "pt", // Portugiesisch
            "pl", // Polnisch
            "ru", // Russisch
            "zh" // Chinesisch (vereinfacht)
    };

    /**
     * Konstruktor für den LanguageManager
     *
     * @param plugin Die Plugin-Instanz
     */
    public LanguageManager(Main plugin) {
        this.plugin = plugin;
        this.console = plugin.getConsole();
    }

    /**
     * Initialisiert den LanguageManager und lädt die konfigurierte Sprache
     */
    public void initialize() {
        // Sprache aus der Konfiguration laden
        String configuredLanguage = plugin.getConfig().getString("language", defaultLanguage);
        setLanguage(configuredLanguage);
    }

    /**
     * Setzt die aktuelle Sprache und lädt die entsprechende Sprachdatei
     *
     * @param language Der Sprachcode (z.B. "en", "de", "fr")
     * @return true, wenn die Sprache erfolgreich geladen wurde
     */
    public boolean setLanguage(String language) {
        // Wenn die Sprache nicht unterstützt wird, auf die Standardsprache zurückfallen
        boolean isSupported = false;
        for (String supportedLang : SUPPORTED_LANGUAGES) {
            if (supportedLang.equalsIgnoreCase(language)) {
                isSupported = true;
                language = supportedLang; // Korrekter Fall
                break;
            }
        }

        if (!isSupported) {
            console.warning("Sprache '" + language + "' wird nicht unterstützt! Verwende Standardsprache '" + defaultLanguage + "'.");
            language = defaultLanguage;
        }

        // Prüfen, ob die Sprache bereits im Cache ist
        if (languageCache.containsKey(language)) {
            languageConfig = languageCache.get(language);
            currentLanguage = language;
            return true;
        }

        // Andernfalls Sprachdatei laden
        File langFile = new File(plugin.getDataFolder(), "lang/lang_" + language + ".yml");
        
        if (langFile.exists()) {
            // Externe Sprachdatei laden
            languageConfig = YamlConfiguration.loadConfiguration(langFile);
            languageCache.put(language, languageConfig);
            currentLanguage = language;
            console.info("Sprache '" + language + "' wurde aus externer Datei geladen.");
            return true;
        } else {
            // Interne Standardsprachdatei laden
            InputStream inputStream = plugin.getResource("lang/lang_" + language + ".yml");
            if (inputStream != null) {
                languageConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                languageCache.put(language, languageConfig);
                currentLanguage = language;
                
                // Sprachdatei extrahieren, damit sie angepasst werden kann
                plugin.saveResource("lang/lang_" + language + ".yml", false);
                console.info("Sprache '" + language + "' wurde aus interner Ressource geladen und extrahiert.");
                return true;
            } else {
                // Sprache nicht gefunden, Fallback auf Englisch
                if (!language.equals(defaultLanguage)) {
                    console.warning("Sprachdatei für '" + language + "' nicht gefunden! Verwende Standardsprache '" + defaultLanguage + "'.");
                    return setLanguage(defaultLanguage);
                } else {
                    console.error("Standardsprachdatei nicht gefunden! Plugin kann nicht korrekt funktionieren.");
                    return false;
                }
            }
        }
    }

    /**
     * Lädt alle Sprachdateien neu
     */
    public void reload() {
        languageCache.clear();
        initialize();
    }

    /**
     * Gibt einen übersetzten Text zurück
     *
     * @param key Der Schlüssel für den Text in der Sprachdatei
     * @return Der übersetzte Text oder der Schlüssel, wenn keine Übersetzung gefunden wurde
     */
    public String getMessage(String key) {
        return getMessage(key, null);
    }

    /**
     * Gibt einen übersetzten Text zurück und ersetzt Platzhalter
     *
     * @param key Der Schlüssel für den Text in der Sprachdatei
     * @param replacements Eine Map mit Platzhaltern und deren Ersetzungen
     * @return Der übersetzte Text mit ersetzten Platzhaltern oder der Schlüssel, wenn keine Übersetzung gefunden wurde
     */
    public String getMessage(String key, Map<String, String> replacements) {
        if (languageConfig == null) {
            console.warning("Sprachkonfiguration nicht geladen! Verwende Schlüssel als Fallback.");
            return key;
        }

        String message = languageConfig.getString(key);
        
        // Wenn der Schlüssel nicht gefunden wurde, versuchen wir es mit der Standardsprache
        if (message == null && !currentLanguage.equals(defaultLanguage) && languageCache.containsKey(defaultLanguage)) {
            message = languageCache.get(defaultLanguage).getString(key);
            
            // Wenn auch in der Standardsprache nicht gefunden, geben wir den Schlüssel zurück
            if (message == null) {
                console.debug("Übersetzungsschlüssel nicht gefunden: " + key);
                return key;
            }
        } else if (message == null) {
            console.debug("Übersetzungsschlüssel nicht gefunden: " + key);
            return key;
        }

        // Platzhalter ersetzen, wenn vorhanden
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message;
    }

    /**
     * Hilfsmethode zum einfachen Ersetzen eines einzelnen Platzhalters
     *
     * @param key Der Schlüssel für den Text in der Sprachdatei
     * @param placeholder Der Name des Platzhalters
     * @param value Der Wert, mit dem der Platzhalter ersetzt werden soll
     * @return Der übersetzte Text mit ersetztem Platzhalter
     */
    public String getMessage(String key, String placeholder, String value) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put(placeholder, value);
        return getMessage(key, replacements);
    }

    /**
     * Gibt die aktuelle Sprache zurück
     *
     * @return Der aktuelle Sprachcode
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
