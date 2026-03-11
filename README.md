# ManaEnchants

Enchantment plugin for **ManaMeta**, providing advanced enchantment features and customization beyond vanilla Minecraft.

---

## Overview

ManaEnchants provides:

- **Override Enchantment View**: Upgrade enchantments based on the items used (e.g., applying `Silk Touch` via `String`).
- **Vanilla EXP Override**: Custom formulas control experience levels and progression. EXP values are stored in each user’s Persistent Data Container (PDC).
- **Dynamic Formula Updates**: Changing the EXP formula updates levels for all users on reload.  
  *Example*: If the previous formula required 100 EXP per level and is updated to 10 EXP per level, a user with 10 EXP levels will now have 100 levels.
- **Custom Enchant Support**: Third-party plugins can register custom enchantments as long as the namespace is correct (`plugin_name:enchantment_name`).

---

## Localization

- Most messages are stored in the `locale` folder and can be edited for your server.
- Add new locales by creating a file with the corresponding language key (e.g., `jp.properties` for Japanese).
- When adding new custom enchantments, update the locale so players can access information via `/enchant info <enchantment_name>`.

---

## Custom Items

- Use `/manaenchants item` to create and manage unique items.
- These items can serve as catalysts in the Enchanter UI.

---

## Plugin Uninstallation

- Removing ManaEnchants will restore EXP values to what they were prior to plugin installation.

---

## Bookshelf Mechanics

- Bookshelves now have a **+1 radius** compared to vanilla Minecraft.
- Checks a **5x5x5 area** around bookshelves instead of vanilla 3x3x3, providing more flexible enchanting setups.

---

## Modifiable Values

Most plugin behaviour and visuals can be customized, including:

- Sounds
- Chat colours
- Locale messages
- Commands

---

## Permissions

| Permission Key                | Description                            |
|-------------------------------|----------------------------------------|
| `manaenchants.core.help`      | Access general help command            |
| `manaenchants.core.reload`    | Reload plugin configuration            |
| `manaenchants.core.item`      | Create and manage custom items         |
| `manaenchants.core.config`    | View configuration settings            |
| `manaenchants.enchant.add`    | Add an enchant to an item              |
| `manaenchants.enchant.clear`  | Remove all enchantments from an item   |
| `manaenchants.enchant.help`   | Access enchant command help            |
| `manaenchants.enchant.info`   | View info about enchants on an item    |
| `manaenchants.enchant.list`   | List all available enchantments        |
| `manaenchants.enchant.remove` | Remove a specific enchant from an item |
| `manaenchants.enchant.set`    | Set a specific enchant on an item      |
| `manaenchants.xp.help`        | Access XP command help                 |
| `manaenchants.xp.add`         | Add XP to a player                     |
| `manaenchants.xp.clear`       | Reset a player’s XP                    |
| `manaenchants.xp.info`        | View a player’s XP                     |
| `manaenchants.xp.info.others` | View another player’s XP               |
| `manaenchants.xp.remove`      | Remove XP from a player                |
| `manaenchants.xp.set`         | Set a player’s XP to a specific value  |

---

## Notes

- Configure `locale` and custom items before opening the Enchanter UI for players.
- Changes to the EXP formula affect all players immediately.
- Bookshelf radius increase allows for larger and more flexible enchanting setups.
- Updates to `command_config.yml` require a server reboot to apply.
- Version **1.0.0** — please report issues on the GitHub issue tracker.
- Player suggestions can be sent via Discord: **_kronn (USERID: 208884801413971969)**.