package de.gerrxt.phantom.util;

import de.gerrxt.phantom.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Klasse für das Senden von Logs über Discord-Webhooks
 */
public class DiscordWebhook {
    
    private final Main plugin;
    private final ConsoleColor console;
    private String webhookUrl;
    private boolean enabled;
    private boolean batching;
    private final List<String> messageQueue = new ArrayList<>();
    private BukkitTask batchTask;
    private long lastMessageTime = 0;
    private final long messageRateLimit; // Minimale Zeit zwischen einzelnen Nachrichten in Millisekunden
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
    /**
     * Konstruktor für DiscordWebhook
     * 
     * @param plugin Die Plugin-Instanz
     */
    public DiscordWebhook(Main plugin) {
        this.plugin = plugin;
        this.console = plugin.getConsole();
        
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("discord.webhook.enabled", false);
        this.webhookUrl = config.getString("discord.webhook.url", "");
        this.batching = config.getBoolean("discord.webhook.use-batching", true);
        this.messageRateLimit = config.getLong("discord.webhook.rate-limit", 5000);
        
        if (enabled && batching) {
            int batchInterval = config.getInt("discord.webhook.batch-interval", 30);
            startBatchTask(batchInterval);
        }
    }
    
    /**
     * Startet die regelmäßige Verarbeitung der Nachrichtenwarteschlange
     * 
     * @param intervalSeconds Das Intervall in Sekunden
     */
    private void startBatchTask(int intervalSeconds) {
        batchTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!messageQueue.isEmpty()) {
                synchronized (messageQueue) {
                    sendBatchedMessages();
                }
            }
        }, 20 * intervalSeconds, 20 * intervalSeconds); // Konvertiere Sekunden in Ticks (20 Ticks = 1 Sekunde)
    }
    
    /**
     * Sendet eine Log-Nachricht über den Discord-Webhook
     * 
     * @param message Die zu sendende Nachricht
     * @param type Der Typ der Nachricht (info, warning, error, success)
     * @param important Ob die Nachricht wichtig ist und sofort gesendet werden soll
     */
    public void sendLog(String message, LogType type, boolean important) {
        if (!enabled || webhookUrl.isEmpty()) {
            return;
        }
        
        // Timestamp zur Nachricht hinzufügen
        String timestamp = dateFormat.format(new Date());
        String formattedMessage = String.format("[%s] %s: %s", timestamp, type.name(), message);
        
        if (batching && !important) {
            synchronized (messageQueue) {
                messageQueue.add(formattedMessage);
            }
        } else {
            // Prüfen, ob die Rate-Limit eingehalten werden muss
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMessageTime < messageRateLimit) {
                // Rate-Limit würde überschritten, zur Warteschlange hinzufügen
                synchronized (messageQueue) {
                    messageQueue.add(formattedMessage);
                }
                return;
            }
            
            // Sofort senden
            sendImmediately(formattedMessage, type);
        }
    }
    
    /**
     * Sendet eine einzelne Nachricht sofort
     * 
     * @param message Die zu sendende Nachricht
     * @param type Der Typ der Nachricht
     */
    private void sendImmediately(String message, LogType type) {
        lastMessageTime = System.currentTimeMillis();
        
        int color;
        switch (type) {
            case INFO -> color = 0x3498DB;     // Blau
            case WARNING -> color = 0xFFD700;  // Gelb
            case ERROR -> color = 0xFF0000;    // Rot
            case SUCCESS -> color = 0x00FF00;  // Grün
            case DEBUG -> color = 0x9B59B6;    // Lila
            default -> color = 0x95A5A6;       // Grau
        }
        
        String json = String.format(
                "{\"embeds\":[{\"title\":\"PhantomWhitelist Log\",\"description\":\"%s\",\"color\":%d}]}",
                escapeJson(message),
                color
        );
        
        // Asynchron senden
        CompletableFuture.runAsync(() -> {
            try {
                sendJsonToWebhook(json);
            } catch (IOException e) {
                console.error("Fehler beim Senden des Discord-Webhooks: " + e.getMessage());
            }
        });
    }
    
    /**
     * Sendet alle angesammelten Nachrichten als eine Batch-Nachricht
     */
    private void sendBatchedMessages() {
        if (messageQueue.isEmpty()) {
            return;
        }
        
        lastMessageTime = System.currentTimeMillis();
        
        // Maximal 10 Nachrichten pro Batch, um Discord-Limits nicht zu überschreiten
        List<String> messagesToSend = new ArrayList<>();
        synchronized (messageQueue) {
            int count = Math.min(messageQueue.size(), 10);
            for (int i = 0; i < count; i++) {
                messagesToSend.add(messageQueue.get(i));
            }
            
            for (int i = 0; i < count; i++) {
                messageQueue.remove(0);
            }
        }
        
        StringBuilder contentBuilder = new StringBuilder();
        for (String msg : messagesToSend) {
            contentBuilder.append(msg).append("\\n");
        }
        
        String json = String.format(
                "{\"embeds\":[{\"title\":\"PhantomWhitelist Log-Zusammenfassung\",\"description\":\"%s\",\"color\":%d}]}",
                escapeJson(contentBuilder.toString()),
                0x95A5A6 // Grau
        );
        
        // Asynchron senden
        CompletableFuture.runAsync(() -> {
            try {
                sendJsonToWebhook(json);
            } catch (IOException e) {
                console.error("Fehler beim Senden des Discord-Webhooks: " + e.getMessage());
            }
        });
    }
    
    /**
     * Sendet JSON-Daten an die Webhook-URL
     * 
     * @param jsonContent Der zu sendende JSON-String
     * @throws IOException Bei einem Fehler beim Senden
     */
    private void sendJsonToWebhook(String jsonContent) throws IOException {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "PhantomWhitelist-Plugin");
        connection.setDoOutput(true);
        
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = jsonContent.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode != 204) {
            console.warning("Discord-Webhook wurde mit Code " + responseCode + " beantwortet");
        }
        
        connection.disconnect();
    }
    
    /**
     * Escape-Funktion für JSON-Strings
     * 
     * @param input Der zu escapende String
     * @return Der escapte String
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * Aktualisiert die Konfiguration des Webhooks
     * 
     * @param config Die aktuelle Konfiguration
     */
    public void updateConfig(FileConfiguration config) {
        this.enabled = config.getBoolean("discord.webhook.enabled", false);
        this.webhookUrl = config.getString("discord.webhook.url", "");
        this.batching = config.getBoolean("discord.webhook.use-batching", true);
        
        // Alte Batch-Task stoppen und neu starten, wenn batching aktiviert ist
        if (batchTask != null) {
            batchTask.cancel();
            batchTask = null;
        }
        
        if (enabled && batching) {
            int batchInterval = config.getInt("discord.webhook.batch-interval", 30);
            startBatchTask(batchInterval);
        }
    }
    
    /**
     * Sendet alle verbliebenen Nachrichten und beendet die Tasks
     */
    public void shutdown() {
        if (batchTask != null) {
            batchTask.cancel();
        }
        
        // Alle verbliebenen Nachrichten senden
        if (!messageQueue.isEmpty() && enabled) {
            sendBatchedMessages();
        }
    }
    
    /**
     * Prüft, ob der Webhook aktiviert ist
     * 
     * @return true, wenn der Webhook aktiviert ist
     */
    public boolean isEnabled() {
        return enabled && !webhookUrl.isEmpty();
    }
    
    /**
     * Sendet eine Info-Nachricht
     * 
     * @param message Die Nachricht
     */
    public void info(String message) {
        sendLog(message, LogType.INFO, false);
    }
    
    /**
     * Sendet eine Warning-Nachricht
     * 
     * @param message Die Nachricht
     */
    public void warning(String message) {
        sendLog(message, LogType.WARNING, false);
    }
    
    /**
     * Sendet eine Error-Nachricht
     * 
     * @param message Die Nachricht
     */
    public void error(String message) {
        sendLog(message, LogType.ERROR, true);
    }
    
    /**
     * Sendet eine Success-Nachricht
     * 
     * @param message Die Nachricht
     */
    public void success(String message) {
        sendLog(message, LogType.SUCCESS, false);
    }
    
    /**
     * Sendet eine Debug-Nachricht
     * 
     * @param message Die Nachricht
     */
    public void debug(String message) {
        sendLog(message, LogType.DEBUG, false);
    }
    
    /**
     * Typen für Log-Nachrichten
     */
    public enum LogType {
        INFO,
        WARNING,
        ERROR,
        SUCCESS,
        DEBUG
    }
}
