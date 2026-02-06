# Highlight Me Now

[![Release](https://img.shields.io/github/v/release/leonardokr/highlight-me-now)](https://github.com/leonardokr/highlight-me-now/releases)
[![License](https://img.shields.io/badge/license-Custom-blue.svg)](LICENSE)

Pattern-based text highlighting for IntelliJ-based IDEs.

## Overview

**Highlight Me Now** is a plugin that automatically highlights lines containing specific text patterns in your code editor. Perfect for making TODO comments, warnings, and important notes stand out visually while you work.

### Supported IDEs

Works with all JetBrains IDEs:
- **IntelliJ IDEA** (Community & Ultimate)
- **PyCharm** (Community & Professional)
- **WebStorm**
- **PhpStorm**
- **CLion**
- **Rider**
- **GoLand**
- **RubyMine**
- And other IntelliJ-based IDEs

## Features

- **Real-time Highlighting:** Instantly highlights matching patterns as you type or open files.
- **Customizable Patterns:** Define your own regex patterns with custom foreground and background colors.
- **Color Picker:** Built-in color chooser for easy color selection in settings.
- **Flexible Modes:** Choose between highlighting the entire line or just the text content.
- **Pastel Defaults:** Ships with soft, eye-friendly pastel colors for common patterns.
- **Persistent Settings:** All configurations are saved and restored between IDE sessions.

### Default Patterns

| Pattern | Background | Purpose |
|---------|------------|---------|
| `FIX` | Pink | Critical fixes needed |
| `TODO` | Yellow | Tasks to complete |
| `WARN` | Orange | Warnings and cautions |
| `OBS` | Blue | Observations and notes |
| `QUESTION` | Purple | Questions to address |

## Installation

The plugin can be installed via the JetBrains Marketplace within any compatible IDE:

    Navigate to Settings/Preferences | Plugins.
    Search for Highlight Me Now.
    Click Install.


### From Source

See [Building from Source](#building-from-source) below.

## Configuration

Settings are located under **Settings/Preferences | Highlight Me Now**.

### Pattern Settings

- **Pattern:** Regex pattern to match (case-insensitive).
- **Color (Hex):** Foreground text color in hex format (e.g., `#FFFFFF`).
- **Background (Hex):** Background color in hex format (e.g., `#FF0000`).

Click on color cells to open the color picker dialog.

### Display Options

- **Highlight entire line:** When enabled, highlights the full line. When disabled, highlights only the text content (excluding leading/trailing whitespace).

## Usage

1. Open any file in the editor.
2. Lines containing configured patterns will be automatically highlighted.
3. Add or modify patterns in Settings to customize highlighting behavior.
4. Changes take effect immediately when settings are applied.

### Examples

```python
# TODO: Implement user authentication
def login():
    pass

# FIX: Memory leak in this function
def process_data():
    # WARN: This operation is slow
    # OBS: Consider caching results
    # QUESTION: Should we use async here?
    pass
```

## Building from Source

### Prerequisites

- Java 21
- Gradle (provided via wrapper)

### Build Steps

To build the plugin distribution:
```bash
./gradlew buildPlugin
```

The resulting ZIP file will be located in `build/distributions/`.

### Running Tests

```bash
./gradlew test
```

### Running IDE with Plugin

To launch an IDE instance with the plugin installed:
```bash
./gradlew runIde
```

## License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.
