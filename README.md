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

| [ICODE]/emblem[/ICODE] | Open GUI | [ICODE]emblem.use[/ICODE] |

| [ICODE]/emblem list[/ICODE] | List your emblems | [ICODE]emblem.use[/ICODE] |

| [ICODE]/emblem set <id>[/ICODE] | Equip an emblem | [ICODE]emblem.use[/ICODE] |

| [ICODE]/emblem reset[/ICODE] | Unequip emblem | [ICODE]emblem.use[/ICODE] |

| [ICODE]/emblem reload[/ICODE] | Reload config | [ICODE]emblem.admin[/ICODE] |

| [ICODE]/emblem admin give/take <player> <id>[/ICODE] | Manage emblems | [ICODE]emblem.admin[/ICODE] |



Aliases: [ICODE]/em[/ICODE], [ICODE]/emblems[/ICODE]



Permissions:

[ICODE]emblem.use[/ICODE] — Use commands & GUI (default: all)
[ICODE]emblem.admin[/ICODE] — Admin commands (default: op)
[ICODE]emblem.all[/ICODE] — Access all emblems without buying (default: op)


PlaceholderAPI Placeholders:

[ICODE]%emblems_display%[/ICODE] — Active emblem symbol
[ICODE]%emblems_id%[/ICODE] — Active emblem ID
[ICODE]%emblems_name%[/ICODE] — Active emblem name


Installation:

Drop the JAR in [ICODE]plugins/[/ICODE], restart server, configure [ICODE]config.yml[/ICODE].



Need help? DM rp_gamerzs on Discord!