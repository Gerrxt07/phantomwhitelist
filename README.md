# PhantomWhitelist

![PhantomWhitelist Banner](assets/banner.png)

*A Minecraft Paper/Purpur server plugin for whitelist management with Discord integration and multi-language support.*

---

## ✨ Features

- 📋 Custom whitelist management with configuration options
- 🤖 Discord integration with role-based whitelist
- 📢 Discord webhook logging for important server events
- ⏳ Automatic player freeze for players without Discord verification
- 🌐 Multi-language support (English, German, French, Spanish, Portuguese, Polish, Russian, Chinese)
- 🎮 Support for Minecraft 1.21.1+
- ☕ Compatible with Java 21+

## 📋 Requirements

- 📄 Paper-based server (Version 1.21.1 or higher - PaperMC, Purpur, Pufferfish, CanvasMC etc.)
- ☕ Java 21 or higher
- 🤖 Discord bot with appropriate permissions (for Discord integration)

## 🚀 Installation

1. Download the latest version of the plugin from the [Releases](https://github.com/gerrxt07/phantomwhitelist/releases) section.
2. Place the JAR file in your server folder under `/plugins`.
3. Restart your server or use a plugin manager to load the plugin.
4. Configure the plugin to your preferences in the `config.yml`.
5. For Discord integration:
   - Create a Discord bot on the [Discord Developer Portal](https://discord.com/developers/applications)
   - Enable "Server Members Intent" under Bot > Privileged Gateway Intents
   - Add your bot to your Discord server
   - Enter the bot token and server ID in the config.yml

## 🔄 How It Works

1. When a player joins the server, the plugin checks if they are already linked with Discord
2. If not, the player is frozen and prompted to enter their Discord name
3. The player has 2 minutes to execute the `/discord <Discord-Name>` command
4. The plugin checks if the specified Discord user exists on the server and has the required role
5. Upon successful verification, the player can play normally
6. On each subsequent login, the plugin verifies that the linked Discord account still has the required role

## 💬 Commands

- `/discord <Discord-Name>` - Links your Minecraft account with your Discord account
- `/pwhitelist reload` - Reloads configuration and language files
- `/pwhitelist add <Player-Name>` - Adds a player to the whitelist
- `/pwhitelist remove <Player-Name>` - Removes a player from the whitelist
- `/pwhitelist list` - Shows all players on the whitelist

## 🔑 Permissions

- `phantomwhitelist.command` - Allows the use of basic commands
- `phantomwhitelist.reload` - Allows reloading the configuration
- `phantomwhitelist.admin` - Allows all administrative actions

## 🤖 Discord Bot Setup

1. Visit the [Discord Developer Portal](https://discord.com/developers/applications)
2. Click on "New Application" and name your bot
3. Go to the "Bot" tab and click "Add Bot"
4. Under "Privileged Gateway Intents", enable "SERVER MEMBERS INTENT"
5. Copy the bot token and paste it into your config.yml
6. Go to the "OAuth2" tab > URL Generator
7. Select the scopes "bot" and "applications.commands"
8. For Bot Permissions, select at minimum: "Read Messages/View Channels", "Send Messages"
9. Copy the generated URL and open it in your browser to add the bot to your server
10. Enable Discord integration in the config.yml and restart your server

## ⚙️ Configuration

After the first start of the plugin, a configuration file will be created under `plugins/PhantomWhitelist/config.yml`. Here you can adjust all settings:

```yaml
# Plugin language (en, de, fr, es, pt, pl, ru, zh)
language: "en"

# Whitelist settings
whitelist:
  enabled: true
  storage-method: 'file'

# Discord integration
discord:
  enabled: true
  token: "YOUR_DISCORD_BOT_TOKEN_HERE"
  guild-id: "YOUR_DISCORD_SERVER_ID_HERE"
  whitelisted-role-ids:
    - "ROLE_ID_1"
    - "ROLE_ID_2"
```

## 🌐 Multi-language Support

The plugin supports 8 different languages:

- 🇬🇧 English (en)
- 🇩🇪 German (de)
- 🇫🇷 French (fr)
- 🇪🇸 Spanish (es)
- 🇵🇹 Portuguese (pt)
- 🇵🇱 Polish (pl)
- 🇷🇺 Russian (ru)
- 🇨🇳 Chinese (simplified) (zh)

To change the language, adjust the `language` parameter in the config.yml. The language files are located in the `plugins/PhantomWhitelist/lang` folder and can be customized if needed.

## 📢 Discord Webhook Logging

The plugin can log important events directly to a Discord channel via webhooks:

1. Create a webhook in your Discord server:
   - Right-click on a channel > Server Settings > Integrations
   - Click "Create Webhook" and enter a name
   - Copy the webhook URL

2. Add the webhook URL to the configuration file:

   ```yaml
   # Discord Webhook Logging
   webhook:
      enabled: false
      url: "YOUR_DISCORD_WEBHOOK_URL_HERE"
      use-batching: true
      batch-interval: 30
      rate-limit: 5000
      log-level: "WARNING"
   ```

3. Configure the webhook settings according to your needs:
   - `use-batching`: Collects messages and sends them in batches
   - `batch-interval`: Interval in seconds for how often messages are sent
   - `rate-limit`: Minimum time between individual messages (in milliseconds)
   - `log-level`: Specify which types of messages should be sent

The logs are displayed with color formatting in the Discord channel:

- 🟢 Green for successes and positive messages
- 🔴 Red for errors and critical issues
- 🟡 Yellow for warnings
- 🔵 Blue for information
- 🟣 Purple for debug messages

## 🗺️ Roadmap

Here is our roadmap for the future development of PhantomWhitelist:

| Status | Feature | Description | Planned for |
|:------:|---------|-------------|:-----------:|
| ✅ | Discord Integration | Basic Discord role checking and verification | v1.0.0 |
| ✅ | Player Freeze | Freeze players until Discord verification | v1.0.0 |
| ✅ | Discord Webhook | Event logging via Discord webhooks | v1.0.0 |
| ✅ | Multi-language Support | Support for 8 languages with configurable language files | v1.1.0 |
| 📅 | (Automatic) Updates | In-game notification and automatic updates | v1.2.0 |
| 💡 | MySQL Integration | Full support for MySQL databases | v1.3.0 |
| 🔮 | Bungeecord/Velocity | Network-wide whitelist synchronization | v2.0.0 |
| 🔮 | Web Dashboard | Web-based interface for whitelist management | v3.0.0 |
| 🔮 | API for Developers | API for other plugins to integrate with | v4.0.0 |

### Legend

- ✅ Implemented and available
- 🔜 In development
- 📅 Planned for the near future
- 💡 Concept phase
- 🔮 Long-term vision

We also have an interactive and updated [Trello Board](https://trello.com/b/PEWsQBMy/phantom-whitelist)!

Would you like to participate in development or have feature requests? [Open an Issue](https://github.com/gerrxt07/phantom-whitelist/issues) or join our [Discord Server](https://phantomcommunity.de/discord)!

## 💻 Development

### Setting Up the Development Environment

1. Clone the repository:

   ```bash
   git clone https://github.com/gerrxt/phantom-whitelist.git
   ```

2. Import the project into your IDE (e.g., IntelliJ, Eclipse, VSCode, etc.).

3. Run the Maven build:

   ```bash
   mvn clean package
   ```

The compiled JAR file can be found in the `target` folder.

## 📜 License

This project is licensed under the [MIT License](LICENSE).

## 👏 Credits

Developed by [gerrxt](https://github.com/gerrxt07).

Developer team from [Phantom Community](https://phantomcommunity.de).
