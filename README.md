<p align="center">
  <img width="96" height="96" src="https://www.spigotmc.org/attachments/pc-png.118068/">
  <p align="center">*Support 1.7-1.16 Proxy Server!*</p>
</p>

PremiumConnector is a Bungeecord plugin that allow you to resolve UUID for Premium user account and permite cracked player to play on your server without compromite the security of your server. You can redirect cracked players to an specified server with a register plugin like AuthMe.

# ⭐ Features ⭐
- **UUID-fix** and **Skin-fix** for premium users
- **Cracked** players using premium name can only connect to server when secondAttempt setting is enable.
- **Redirect** cracked players don't using premium username to a specified server
- Compatible with [GeyserMC](https://geysermc.org/) (Allow bedrock version to connect on BungeeCord/Paper server)

## Supported auth plugins:
- [AuthMe](https://www.spigotmc.org/resources/authmereloaded.6269/)
- [LockLogin](https://www.spigotmc.org/resources/gsa-locklogin.75156/)

You need to configure your auth plugin to use it with bungeecord.

# ⌨ Commands ⌨

|      Command      |              Permission            |           Description          | Alias  |
| :---------------: | :--------------------------------: | :----------------------------: | :----: |
| /premium (player) | *premiumconnector.command.premium* | Define player as Premium user. | /prem  |
| /cracked (player) | *premiumconnector.command.cracked* | Define player as Cracked user. | /crack |
|  /reset  (player) |  *premiumconnector.command.reset*  | Reset player.                  | /rst   |

*premiumconnector.admin* permission allow you to use these commands on other players.

# ⚙ Configuration ⚙
```# Debug level
debug: INFO

# Server name on which cracked players are redirected for register/login them.
authServer: crack
 
# Allow cracked player to use premium username
secondAttempt: true

# Time to confirm /premium command
timeToConfirm: 30
```

# 🐜 Report bugs 🐜
Please report bugs or ask for features like support for new authentification plugin on Github Issues

# ❗ Warning ❗
You must run Spigot server and BungeeCord in offline-mode. If you want fix UUID and skin you must forward informations through BungeeCord!
