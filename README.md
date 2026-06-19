Emblem Plugin — Customizable Cosmetics for Your Server

Add a cosmetic emblem system to your server inspired by MinemenClub! Let your players customize and display unique emblems to stand out in-game.

Features:
<br>
• Easy-to-use emblem GUI for players to browse and equip emblems
<br>
• Fully customizable — control the prefix, messages, and GUI design through config
<br>
• Economy integration — tie emblems to your server's economy system via Vault
<br>
• PxCOSMETICS support — works with Phoenix Cosmetics by Refine Development
<br>
• PlaceholderAPI support — display emblem info in other plugins
<br>
• Admin tools — give/take emblems, manage player cosmetics
<br>

Dependencies:
<br>
• Vault (required) Phoenix (optional) — Economy for emblem purchases
<br>
• PlaceholderAPI (optional) — Placeholders
<br>

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
<br>
• emblem.use — Use commands & GUI (default: all)
<br>
• emblem.admin — Admin commands (default: op)
<br>
•emblem.all — Access all emblems without buying (default: op)
<br>

PlaceholderAPI Placeholders:
<br>
• %emblems_display% — Active emblem symbol
<br>
• %emblems_id% — Active emblem ID
<br>
• %emblems_name% — Active emblem name
<br>

Installation:
Drop the JAR in plugins/, restart server, configure config.yml.

Need help? DM rp_gamerzs on Discord!