# PhantomWhitelist v1.1.0

![PhantomWhitelist Banner](assets/banner.png)

Updated version of the Phantom Whitelist Plugin for Minecraft Paper/Purpur Server with multi-language support, numerous stability and performance improvements. Text has been rewritten and translations for 8 languages have been added.

## 🚀 Release Date: May 27, 2025

---

## 🔄 Improvements in v1.1.0

### 🌐 Multi-language Support

- Full support for 8 languages: English, German, French, Spanish, Portuguese, Polish, Russian, and Chinese (simplified)
- New LanguageManager for easy management of translations
- Configurable language setting in config.yml
- All plugin messages are managed through language files
- External language files can be customized

### 🛡️ Improved Error Handling

- Thread-safe implementation of the Singleton pattern for data structures
- More comprehensive exception handling in critical components
- More robust shutdown routines for all plugin components
- Improved error detection for file and directory creation

### ⚡ Performance Optimizations

- Optimized Discord bot configuration with better resource management
- Prevention of duplicate initializations in plugin components
- Improved resource release through automatic cleanup of scheduler tasks
- More efficient whitelist data processing with revised error handling

### 🔧 Technical Updates

- Updated JDA dependency to version 5.0.0-beta.20
- Updated Maven Compiler Plugin to version 3.12.1
- Fixed XML structure in the pom.xml file
- Added timeout configurations for Discord connections

### ⚙️ Configuration Enhancements

- New parameter `connection-timeout` for Discord connection
- Better documentation of configuration options
- Checks for incomplete or incorrect configurations
- Additional directory checks for optimized data storage

## 🔧 Technical Details

- **Supported Minecraft Versions:** 1.21.1 or higher
- **Java Version:** Java 21 or higher
- **Dependencies:**
  - JDA (Java Discord API) 5.0.0-beta.20
  - Paper API 1.21.1-R0.1

## 🛠️ Updating from v1.0.0

1. Stop your server
2. Replace the old JAR file with the new v1.1.0 version
2.1 We recommend a complete reinstallation of the plugin
3. Restart your server
4. The configuration will be automatically updated, existing settings will remain. Please check your config file!

## ⚙️ Configuration

New parameters have been added to the `config.yml`:

```yaml
# Plugin language (en, de, fr, es, pt, pl, ru, zh)
# en - English
# de - Deutsch
# fr - Français
# es - Español
# pt - Português
# pl - Polski
# ru - Русский
# zh - 简体中文
language: "en"

discord:
  # Connection timeout in seconds
  connection-timeout: 10
```

These parameters can be used to adjust the language of the plugin and the timeout setting for the Discord connection. The language files are located in the `plugins/PhantomWhitelist/lang` folder and can be customized if needed.

## 🐛 Fixed Issues

- Improved stability with Discord connection problems
- More efficient resource usage during plugin deactivation
- Optimized error handling for incomplete or incorrect configurations
- Prevention of potential memory leaks through proper resource release

For problems or questions, please open a [GitHub Issue](https://github.com/gerrxt07/phantomwhitelist/issues) or join our [Discord Server](https://discord.gg/phantomwhitelist).

© 2025 gerrxt | [MIT License](LICENSE)
