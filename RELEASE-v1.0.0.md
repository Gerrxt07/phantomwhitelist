# PhantomWhitelist v1.0.0

![PhantomWhitelist Banner](https://github.com/Gerrxt07/phantomwhitelist/blob/master/assets/banner.png)

The first stable version of the PhantomWhitelist Plugin for Minecraft Paper/Purpur Server

## 🚀 Release Date: May 26, 2025

---

## ✨ Main Features

### 🤖 Discord Integration

- Connection with a Discord bot for role-based whitelist
- Automatic verification of Discord roles for whitelist permission
- Linking Minecraft accounts with Discord users

### 📋 Whitelist Management

- Custom whitelist functionality with advanced configuration options
- Manage players through simple commands
- Support for standard Minecraft whitelist as fallback

### ⏳ Player Freeze System

- Automatic freezing of players until Discord verification
- Time limit (2 minutes) for verification
- Visual hints and instructions for new players

### 🖥️ Server-side Validation

- Verification of Discord roles at each login
- Secure kicking of players when required role is lost
- Optimized performance through local caching of user links

## 🔧 Technical Details

- **Supported Minecraft Versions:** 1.21.1 or higher
- **Java Version:** Java 21 or higher
- **Dependencies:**
  - JDA (Java Discord API) 5.0.0-beta.13
  - Paper/Purpur API 1.21.1-R0.1

## 🛠️ Installation

1. Download the JAR file and place it in your `/plugins` folder
2. Restart your server or use `/reload confirm`
3. Configure the plugin in the generated `config.yml`
4. Set up your Discord bot and add it to your server
5. Add the bot token and server ID to the configuration
6. Restart the server to activate Discord integration

## 📝 Commands

| Command | Description | Permission |
|--------|--------------|--------------|
| `/discord <Discord-Name>` | Links your Minecraft account with your Discord account | None |
| `/pwhitelist add <Player>` | Adds a player to the whitelist | `phantomwhitelist.admin` |
| `/pwhitelist remove <Player>` | Removes a player from the whitelist | `phantomwhitelist.admin` |
| `/pwhitelist list` | Shows all players on the whitelist | `phantomwhitelist.command` |
| `/pwhitelist reload` | Reloads the configuration | `phantomwhitelist.reload` |

## ⚙️ Configuration

Check out the [Homepage](https://github.com/gerrxt/phantomwhitelist).

## 🐛 Known "Issues"

- For very large Discord servers (>10,000 members), the initial connection may take several seconds.
- Discord role verification may be slightly delayed when the Discord API server is under heavy load.
- Player freezing may not work with all gameplay modifications.

## 🙏 Acknowledgements

Special thanks to:

- The Paper/Spigot developers for their excellent server software
- The developers of the JDA library for Discord integration
- All testers who helped during the development of this plugin

---

For problems or questions, please open a [GitHub Issue](https://github.com/gerrxt/phantomwhitelist/issues) or join our [Discord Server](https://discord.gg/phantomwhitelist).

© 2025 gerrxt
