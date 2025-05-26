![PhantomWhitelist Banner](https://github.com/Gerrxt07/phantomwhitelist/blob/master/assets/banner.png)

Die erste stabile Version des PhantomWhitelist Plugins für Minecraft Paper/Purpur Server

## 🚀 Release-Datum: 26. Mai 2025

---

## ✨ Hauptfeatures

### 🤖 Discord-Integration

- Verbindung mit einem Discord-Bot für rollenbasierte Whitelist
- Automatische Überprüfung von Discord-Rollen für die Whitelist-Berechtigung
- Verknüpfung von Minecraft-Konten mit Discord-Benutzern

### 📋 Whitelist-Management

- Eigene Whitelist-Funktionalität mit erweiterten Konfigurationsmöglichkeiten
- Verwalten von Spielern über einfache Befehle
- Unterstützung für die Standard-Minecraft-Whitelist als Fallback

### ⏳ Spieler-Freeze-System

- Automatisches Einfrieren von Spielern bis zur Discord-Verifizierung
- Zeitbegrenzung (2 Minuten) für die Verifizierung
- Visuelle Hinweise und Anweisungen für neue Spieler

### 🖥️ Serverseitige Validierung

- Überprüfung der Discord-Rollen bei jedem Login
- Sicheres Kicken von Spielern bei Verlust der erforderlichen Rolle
- Optimierte Leistung durch lokales Caching von Benutzerverknüpfungen

## 🔧 Technische Details

- **Unterstützte Minecraft-Versionen:** 1.21.1 oder höher
- **Java-Version:** Java 21 oder höher
- **Abhängigkeiten:**
  - JDA (Java Discord API) 5.0.0-beta.13
  - Paper/Purpur API 1.21.1-R0.1

## 🛠️ Installation

1. Lade die JAR-Datei herunter und lege sie in deinen `/plugins` Ordner
2. Starte deinen Server neu oder verwende `/reload confirm`
3. Konfiguriere das Plugin in der erzeugten `config.yml`
4. Richte deinen Discord-Bot ein und füge ihn zu deinem Server hinzu
5. Füge die Bot-Token und Server-ID in die Konfiguration ein
6. Starte den Server erneut, um die Discord-Integration zu aktivieren

## 📝 Befehle

| Befehl | Beschreibung | Berechtigung |
|--------|--------------|--------------|
| `/discord <Discord-Name>` | Verknüpft deinen Minecraft-Account mit deinem Discord-Konto | Keine |
| `/pwhitelist add <Spieler>` | Fügt einen Spieler zur Whitelist hinzu | `phantomwhitelist.admin` |
| `/pwhitelist remove <Spieler>` | Entfernt einen Spieler von der Whitelist | `phantomwhitelist.admin` |
| `/pwhitelist list` | Zeigt alle Spieler auf der Whitelist an | `phantomwhitelist.command` |
| `/pwhitelist reload` | Lädt die Konfiguration neu | `phantomwhitelist.reload` |

## ⚙️ Konfiguration

Schaue dir die [Homepage](https://github.com/gerrxt/phantomwhitelist) an.

## 🐛 Bekannte "Probleme"

- Bei sehr großen Discord-Servern (>10.000 Mitglieder) kann die erste Verbindung mehrere Sekunden dauern.
- Die Discord-Rollenüberprüfung kann leicht verzögert sein, wenn der Discord-API-Server stark ausgelastet ist.
- Das Einfrieren von Spielern funktioniert möglicherweise nicht mit allen Gameplay-Modifikationen.

## 🙏 Danksagungen

Besonderer Dank gilt:

- Den Paper/Spigot-Entwicklern für ihre hervorragende Server-Software
- Den Entwicklern der JDA-Bibliothek für die Discord-Integration
- Allen Testern, die bei der Entwicklung dieses Plugins geholfen haben

---

Bei Problemen oder Fragen eröffne bitte ein [GitHub Issue](https://github.com/gerrxt/phantomwhitelist/issues) oder tritt unserem [Discord-Server](https://discord.gg/phantomwhitelist) bei.

© 2025 gerrxt | [MIT-Lizenz](LICENSE)
