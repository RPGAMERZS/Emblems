Emblem Plugin — Customizable Cosmetics for Your Server

Add a cosmetic emblem system to your server inspired by MinemenClub! Let your players customize and display unique emblems to stand out in-game.

Features:
• Easy-to-use emblem GUI for players to browse and equip emblems
• Fully customizable — control the prefix, messages, and GUI design through config
• Economy integration — tie emblems to your server's economy system via Vault
• PxCOSMETICS support — works with Phoenix Cosmetics by Refine Development
• PlaceholderAPI support — display emblem info in other plugins
• Admin tools — give/take emblems, manage player cosmetics

Dependencies:
Vault (required) Phoenix (optional) — Economy for emblem purchases
PlaceholderAPI (optional) — Placeholders

Commands:
| Command | Description | Permission |
|---------|-------------|------------|
| /emblem | Open GUI | emblem.use |
| /emblem list | List your emblems | emblem.use |
| /emblem set <id> | Equip an emblem | emblem.use |
| /emblem reset | Unequip emblem | emblem.use |
| /emblem reload | Reload config | emblem.admin |
| /emblem admin give/take <player> <id> | Manage emblems | emblem.admin |

Aliases: /em, /emblems

Permissions:
emblem.use — Use commands & GUI (default: all)
emblem.admin — Admin commands (default: op)
emblem.all — Access all emblems without buying (default: op)

PlaceholderAPI Placeholders:
%emblems_display% — Active emblem symbol
%emblems_id% — Active emblem ID
%emblems_name% — Active emblem name

Installation:
Drop the JAR in plugins/, restart server, configure config.yml.

Need help? DM rp_gamerzs on Discord!