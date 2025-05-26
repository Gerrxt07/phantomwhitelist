package de.gerrxt.phantom.discord;

import de.gerrxt.phantom.Main;
import de.gerrxt.phantom.util.ConsoleColor;
import de.gerrxt.phantom.util.DiscordWebhook;
import de.gerrxt.phantom.util.WhitelistData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DiscordManager {
    
    private final Main plugin;
    private JDA jda;
    private Guild guild;
    private List<String> requiredRoleIds;
    private String guildId;
    private boolean isEnabled;
    private ConsoleColor console;
    private DiscordWebhook webhook;
    
    public DiscordManager(Main plugin) {
        this.plugin = plugin;
        this.console = plugin.getConsole();
        this.webhook = plugin.getDiscordWebhook();
        this.isEnabled = false;
    }
    
    /**
     * Initialisiert den Discord-Bot und stellt die Verbindung her
     */
    public void initialize() {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("discord.enabled", false)) {
            console.info("Discord-Integration ist deaktiviert.");
            return;
        }
        
        console.section("Discord-Bot Initialisierung");
        
        String token = config.getString("discord.token", "");
        if (token.isEmpty() || token.equals("DEIN_DISCORD_BOT_TOKEN_HIER")) {
            console.error("Discord-Bot-Token ist nicht konfiguriert. Discord-Integration wird deaktiviert.");
            if (webhook != null) {
                webhook.error("Discord-Bot-Token ist nicht konfiguriert. Discord-Integration wird deaktiviert.");
            }
            return;
        }
        
        guildId = config.getString("discord.guild-id", "");
        if (guildId.isEmpty() || guildId.equals("DEINE_DISCORD_SERVER_ID_HIER")) {
            console.error("Discord-Server-ID ist nicht konfiguriert. Discord-Integration wird deaktiviert.");
            if (webhook != null) {
                webhook.error("Discord-Server-ID ist nicht konfiguriert. Discord-Integration wird deaktiviert.");
            }
            return;
        }
        
        requiredRoleIds = config.getStringList("discord.whitelisted-role-ids");
        if (requiredRoleIds.isEmpty() || requiredRoleIds.get(0).equals("ROLLE_ID_1")) {
            console.error("Keine gültigen Rollen-IDs konfiguriert. Discord-Integration wird deaktiviert.");
            if (webhook != null) {
                webhook.error("Keine gültigen Rollen-IDs konfiguriert. Discord-Integration wird deaktiviert.");
            }
            return;
        }
        
        try {
            console.info("Verbinde mit Discord...");
            
            // JDA mit den notwendigen Intents initialisieren
            EnumSet<GatewayIntent> intents = EnumSet.of(
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGES
            );
            
            jda = JDABuilder.createDefault(token)
                    .enableIntents(intents)
                    .build();
            
            // Warten auf die Initialisierung
            jda.awaitReady();
            
            // Guild abrufen
            guild = jda.getGuildById(guildId);
            if (guild == null) {
                console.error("Konnte den Discord-Server mit der ID " + guildId + " nicht finden.");
                if (webhook != null) {
                    webhook.error("Konnte den Discord-Server mit der ID " + guildId + " nicht finden.");
                }
                jda.shutdown();
                return;
            }
            
            isEnabled = true;
            console.success("Discord-Bot erfolgreich verbunden mit Server: " + guild.getName());
            if (webhook != null) {
                webhook.success("Discord-Bot erfolgreich verbunden mit Server: " + guild.getName());
            }
            console.info("Erforderliche Rollen: " + requiredRoleIds.size());
            
            // Rolleninformationen anzeigen
            for (String roleId : requiredRoleIds) {
                Role role = guild.getRoleById(roleId);
                if (role != null) {
                    console.info(" - Rolle gefunden: " + role.getName() + " (ID: " + roleId + ")");
                } else {
                    console.warning(" - Rolle nicht gefunden für ID: " + roleId);
                    if (webhook != null) {
                        webhook.warning(" - Rolle nicht gefunden für ID: " + roleId);
                    }
                }
            }
        } catch (Exception e) {
            console.exception("Fehler beim Initialisieren des Discord-Bots", e);
            if (webhook != null) {
                webhook.error("Fehler beim Initialisieren des Discord-Bots: " + e.getMessage());
            }
        }
    }
    
    /**
     * Überprüft, ob ein Discord-Benutzer die erforderliche Rolle hat
     * 
     * @param discordId Die Discord-ID des zu überprüfenden Benutzers
     * @return CompletableFuture mit dem Ergebnis der Überprüfung
     */
    public CompletableFuture<Boolean> hasRequiredRole(String discordId) {
        if (!isEnabled || guild == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        guild.retrieveMemberById(discordId).queue(member -> {
            if (member == null) {
                future.complete(false);
                return;
            }
            
            boolean hasRole = false;
            for (String roleId : requiredRoleIds) {
                Role role = guild.getRoleById(roleId);
                if (role != null && member.getRoles().contains(role)) {
                    hasRole = true;
                    break;
                }
            }
            
            future.complete(hasRole);
        }, error -> {
            console.error("Fehler beim Abrufen des Discord-Mitglieds: " + error.getMessage());
            if (webhook != null) {
                webhook.error("Fehler beim Abrufen des Discord-Mitglieds: " + error.getMessage());
            }
            future.complete(false);
        });
        
        return future;
    }
    
    /**
     * Überprüft, ob ein Discord-Benutzer im Server existiert
     * 
     * @param discordUsername Der Discord-Benutzername (mit #Tag)
     * @return Die Discord-ID, wenn gefunden, sonst null
     */
    public CompletableFuture<String> getDiscordIdByUsername(String discordUsername) {
        if (!isEnabled || guild == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<String> future = new CompletableFuture<>();
        
        console.info("Suche nach Discord-Benutzer: " + discordUsername);
        
        // Laden aller Mitglieder des Servers
        guild.loadMembers().onSuccess(members -> {
            for (Member member : members) {
                String name = member.getUser().getName();
                if (name.equalsIgnoreCase(discordUsername)) {
                    console.success("Discord-Benutzer gefunden: " + name + " (ID: " + member.getId() + ")");
                    if (webhook != null) {
                        webhook.success("Discord-Benutzer gefunden: " + name + " (ID: " + member.getId() + ")");
                    }
                    future.complete(member.getId());
                    return;
                }
            }
            console.warning("Konnte keinen Discord-Benutzer mit dem Namen " + discordUsername + " finden.");
            if (webhook != null) {
                webhook.warning("Konnte keinen Discord-Benutzer mit dem Namen " + discordUsername + " finden.");
            }
            future.complete(null);
        }).onError(error -> {
            console.error("Fehler beim Laden der Server-Mitglieder: " + error.getMessage());
            if (webhook != null) {
                webhook.error("Fehler beim Laden der Server-Mitglieder: " + error.getMessage());
            }
            future.complete(null);
        });
        
        return future;
    }
    
    /**
     * Speichert die Verknüpfung zwischen Minecraft- und Discord-Benutzer
     * 
     * @param minecraftName Der Minecraft-Benutzername
     * @param discordId Die Discord-ID
     */
    public void linkPlayer(String minecraftName, String discordId) {
        WhitelistData.getInstance().savePlayerLink(minecraftName, discordId);
        console.success("Spieler " + minecraftName + " mit Discord-ID " + discordId + " verknüpft");
        if (webhook != null) {
            webhook.success("Spieler " + minecraftName + " mit Discord-ID " + discordId + " verknüpft");
        }
    }
    
    /**
     * Gibt die Discord-ID eines Minecraft-Spielers zurück
     * 
     * @param minecraftName Der Minecraft-Benutzername
     * @return Die verknüpfte Discord-ID oder null
     */
    public String getDiscordIdByMinecraft(String minecraftName) {
        return WhitelistData.getInstance().getDiscordId(minecraftName);
    }
    
    /**
     * Überprüft, ob ein Minecraft-Spieler mit Discord verknüpft ist
     * 
     * @param minecraftName Der Minecraft-Benutzername
     * @return true, wenn der Spieler verknüpft ist
     */
    public boolean isPlayerLinked(String minecraftName) {
        return WhitelistData.getInstance().isPlayerLinked(minecraftName);
    }
    
    /**
     * Prüft, ob der Discord-Bot aktiviert und verbunden ist
     * 
     * @return true, wenn der Bot aktiv ist
     */
    public boolean isEnabled() {
        return isEnabled && jda != null && guild != null;
    }
    
    /**
     * Fährt den Discord-Bot herunter
     */
    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            console.info("Discord-Bot wurde heruntergefahren.");
            if (webhook != null) {
                webhook.info("Discord-Bot wurde heruntergefahren.");
            }
        }
    }
}
