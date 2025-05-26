# PhantomWhitelist

![PhantomWhitelist Banner](assets/banner.png)

*Ein Minecraft Paper/Purpur Server Plugin für die Verwaltung einer Whitelist mit Discord-Integration.*

---

## ✨ Funktionen

- 📋 Eigene Whitelist-Verwaltung mit Konfigurationsmöglichkeiten
- 🤖 Discord-Integration mit Rollenbasierter Whitelist
- 📢 Discord-Webhook-Logging für wichtige Serverereignisse
- ⏳ Automatische Spieler-Freeze bei fehlender Discord-Verknüpfung
- 🎮 Unterstützung für Minecraft 1.21.1+
- ☕ Kompatibel mit Java 21+

## 📋 Anforderungen

- 📄 Paper oder Purpur Server (Version 1.21.1 oder höher)
- ☕ Java 21 oder höher
- 🤖 Discord-Bot mit entsprechenden Berechtigungen (für die Discord-Integration)

## 🚀 Installation

1. Lade die neueste Version des Plugins aus dem [Releases](https://github.com/gerrxt/phantom-whitelist/releases)-Bereich herunter.
2. Platziere die JAR-Datei in deinem Server-Ordner unter `/plugins`.
3. Starte deinen Server neu oder verwende einen Plugin-Manager, um das Plugin zu laden.
4. Konfiguriere das Plugin nach deinen Wünschen in der `config.yml`.
5. Für die Discord-Integration:
   - Erstelle einen Discord-Bot auf der [Discord Developer Portal](https://discord.com/developers/applications)
   - Aktiviere die "Server Members Intent" unter Bot > Privileged Gateway Intents
   - Füge deinen Bot zu deinem Discord-Server hinzu
   - Trage Bot-Token und Server-ID in die config.yml ein

## ⚙️ Konfiguration

Nach dem ersten Start des Plugins wird eine Konfigurationsdatei unter `plugins/PhantomWhitelist/config.yml` erstellt. Hier kannst du alle Einstellungen anpassen:

```yaml
# Whitelist-Einstellungen
whitelist:
  enabled: true
  storage-method: 'file'

# Discord-Integration
discord:
  enabled: true
  token: "DEIN_DISCORD_BOT_TOKEN_HIER"
  guild-id: "DEINE_DISCORD_SERVER_ID_HIER"
  whitelisted-role-ids:
    - "ROLLE_ID_1"
    - "ROLLE_ID_2"
    
  # Discord Webhook Logging
  webhook:
    enabled: false
    url: "DEINE_DISCORD_WEBHOOK_URL_HIER"
    use-batching: true
    batch-interval: 30
    rate-limit: 5000
    log-level: "WARNING"
```

## 🔄 Funktionsweise

1. Wenn ein Spieler den Server betritt, wird überprüft, ob er bereits mit Discord verknüpft ist
2. Falls nicht, wird der Spieler eingefroren und aufgefordert, seinen Discord-Namen einzugeben
3. Der Spieler hat 2 Minuten Zeit, den `/discord <Discord-Name>` Befehl auszuführen
4. Das Plugin überprüft, ob der angegebene Discord-Benutzer auf dem Server existiert und die erforderliche Rolle hat
5. Bei erfolgreicher Verknüpfung kann der Spieler normal spielen
6. Bei jedem weiteren Login wird überprüft, ob der verknüpfte Discord-Account noch die erforderliche Rolle hat

## 💬 Befehle

- `/discord <Discord-Name>` - Verknüpft deinen Minecraft-Account mit deinem Discord-Account
- `/pwhitelist reload` - Lädt die Konfiguration neu
- `/pwhitelist add <Spielername>` - Fügt einen Spieler zur Whitelist hinzu
- `/pwhitelist remove <Spielername>` - Entfernt einen Spieler von der Whitelist
- `/pwhitelist list` - Zeigt alle Spieler auf der Whitelist an

## 🔑 Berechtigungen

- `phantomwhitelist.command` - Erlaubt die Verwendung der Basisbefehle
- `phantomwhitelist.reload` - Erlaubt das Neuladen der Konfiguration
- `phantomwhitelist.admin` - Erlaubt alle administrativen Aktionen

## 🤖 Discord-Bot Einrichtung

1. Besuche das [Discord Developer Portal](https://discord.com/developers/applications)
2. Klicke auf "New Application" und gib deinem Bot einen Namen
3. Gehe zum "Bot" Tab und klicke auf "Add Bot"
4. Unter "Privileged Gateway Intents" aktiviere "SERVER MEMBERS INTENT"
5. Kopiere das Bot-Token und füge es in deine config.yml ein
6. Gehe zum "OAuth2" Tab > URL Generator
7. Wähle die Scopes "bot" und "applications.commands"
8. Bei Bot Permissions wähle mindestens: "Read Messages/View Channels", "Send Messages"
9. Kopiere die generierte URL und öffne sie in deinem Browser, um den Bot zu deinem Server hinzuzufügen
10. Aktiviere die Discord-Integration in der config.yml und starte den Server neu

## 📢 Discord Webhook Logging

Das Plugin kann wichtige Ereignisse auch direkt in einen Discord-Kanal über Webhooks loggen:

1. Erstelle einen Webhook in deinem Discord-Server:
   - Rechtsklick auf einen Kanal > Server-Einstellungen > Integrationen
   - Klicke auf "Webhook erstellen" und gib einen Namen ein
   - Kopiere die Webhook-URL

2. Füge die Webhook-URL in die Konfigurationsdatei ein:

   ```yaml
   discord:
     webhook:
       enabled: true
       url: "DEINE_DISCORD_WEBHOOK_URL_HIER"
   ```

3. Konfiguriere die Webhook-Einstellungen nach deinen Bedürfnissen:
   - `use-batching`: Sammelt Nachrichten und sendet sie in Batches
   - `batch-interval`: Intervall in Sekunden, wie oft Nachrichten gesendet werden
   - `rate-limit`: Minimale Zeit zwischen einzelnen Nachrichten (in Millisekunden)
   - `log-level`: Festlegen, welche Arten von Nachrichten gesendet werden sollen

Die Logs werden farblich formatiert im Discord-Kanal angezeigt:

- 🟢 Grün für Erfolge und positive Meldungen
- 🔴 Rot für Fehler und kritische Probleme
- 🟡 Gelb für Warnungen
- 🔵 Blau für Informationen
- 🟣 Lila für Debug-Meldungen

## 💻 Entwicklung

### Entwicklungsumgebung einrichten

1. Klone das Repository:

   ```bash
   git clone https://github.com/gerrxt/phantom-whitelist.git
   ```

2. Importiere das Projekt in deine IDE (z.B. IntelliJ, Eclipse, VSCode etc.).

3. Führe den Maven-Build aus:

   ```bash
   mvn clean package
   ```

Die kompilierte JAR-Datei findest du im `target`-Ordner.

## 📜 Lizenz

Dieses Projekt steht unter der [MIT-Lizenz](LICENSE).

## 👏 Credits

Entwickelt von [gerrxt](https://github.com/gerrxt07).
