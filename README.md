# LoginPassword Plugin

[![Modrinth](https://img.shields.io/badge/Modrinth-grey?logo=modrinth)](https://modrinth.com/plugin/loginpassword)
[![Hangar](https://img.shields.io/badge/Hangar-grey)](https://hangar.papermc.io/trim02/loginPassword)

## Overview

This plugin is an alternative way to control access to a minecraft server, without using a whitelist.

This plugin can be installed either on your Velocity proxy or backend Paper server. If used on Velocity, a login server is required. If used on Paper, the server and client versions must be 1.21.7+

This is NOT a plugin that adds user registration or any of that other stuff you'll find by other authentication plugins on the web. This is a simple plugin to restrict access to your server, without having to maintain a whitelist or using discord integrations and whatnot.

## Features

- **Password Authentication**: Players must enter a password to access the server.
- **One-Time Login**: Optionally allows players to authenticate only once.
- **Plugin granted bypass permissions**: Integrates with LuckPerms to grant bypass permissions.
- **Kick on Timeout**: Players who fail to provide the correct password within a specified timeout are kicked from the server.
- **Customizable Messages**: Custom messages for kick, no password, and wrong password.

## Configuration

**If used on Velocity, you will need a server to function as a login server. Any minecraft server will do, but I recommend [NanoLimbo](https://github.com/Nan1t/NanoLimbo), as it is extremely lightweight, and will only exist to facilitate login requests, you should also set the permission node `velocity.command.server` to false in your permission plugins default everyone group, otherwise players will be able to bypass the plugin**


The plugin uses a TOML configuration file (`config.toml`) to manage its settings. Below are some of the configurable options, comments are included in the file:
#### core
- `loginServer`: The server where players are redirected for login. [Velocity Only]
- `hubServer`: The main server. [Velocity Only]
- `serverPassword`: The password(s) required for login. Can be multiple.
- `oneTimeLogin`: Boolean to enable/disable one-time login. Default is `true`.
#### core.bypass
- `bypassNode`: The permission node for bypassing login.
- `pluginGrantsBypass`: Boolean to enable/disable plugin-granted bypass. Default is `true`.
- `disableLoginCommandOnBypass`: Boolean to disable login command if bypass is granted. Default is `true`. [Velocity Only]
- `bypasserLoginExitMethod`: What happens if a player with bypass transfers to the login server. Options are `auto`, `manual`, `deny-entry`. Check config file for more info. [Velocity Only]
#### core.bypass.methods
- `bypassMethod`: Method to grant bypass permissions. Options are `user` or `group`.
- `bypassGroup`: The group to add the player to if `bypassMethod` is `group`.
#### core.kick
- `kickMessage`: Message displayed when a player is kicked.
- `kickTimeout`: Timeout duration before a player is kicked, in seconds.
#### messages
- `noPassword`: Message displayed when no password is provided.
- `wrongPassword`: Message displayed when the wrong password is provided.
- `welcomeMessage`: Message to be displayed when someone joins the login server. If on Velocity, it will be a message sent to the player upon joing the login server. If on Paper, it will be the title of the dialog login prompt. Accepts MiniMessage formatting. [See PaperMC for info](https://docs.papermc.io/adventure/minimessage/format/).
#### misc
- `loginCommandGrantedToEveryone`: Boolean to grant the login command to everyone. Default is `true`. [Velocity Only]
- `loginCommandNode`: The permission node for the login command. Default is `loginpassword.login`. [Velocity Only]
- `pluginEnabled`: Boolean to enable/disable the plugin. Default is `true`.
- `debugMode`: Boolean to enable/disable debug logging. Default is `false`.
- `configVersion`: Version of the config file. Do not touch.

## Installation

- LuckPerms is optional (but recommended)
- A server to act as a login server is required if installed on Velocity.
- It is recommended to negate the permission node `velocity.command.server` for all players in your permissions plugin, otherwise players will be able to bypass the plugin by directly transferring to the hub server.

### Velocity
1. Download the plugin JAR file.
2. Place the JAR file in the `plugins` directory of your Velocity server.
3. Start the server to generate the default configuration file.
4. Edit the `config.toml` file in the `plugins/LoginPassword` directory to suit your needs.
5. Configure `velocity.toml` by adding the login server to `[servers]` table. If you want `ping-passthrough = "all"` to work correctly, have only the hub server in the `try = []` array, do not add the login server to the array. The plugin will handle redirecting players to the login server.
6. Restart the server or run `velocity reload` to reload all plugins and apply the new `velocity.toml`.
7. For future changes to `config.toml`, apply the changes using `loginpassword reload`

### Paper
1. Download the plugin JAR file.
2. Place the JAR file in the `plugins` directory of your paper server.
3. Start the server to generate the default configuration file.
4. Edit the `config.toml` file in the `plugins/LoginPassword` directory to suit your needs.
5. Restart the server or reload using `/loginpassword reload` to apply the new configuration.

## Usage

Once installed and configured, the plugin will prompt players to enter a password upon connecting to the server. Depending on the configuration, players may only need to authenticate once. Administrators can manage permissions using LuckPerms or another permissions plugin to grant or revoke bypass permissions, or use the commands.

### Commands
- `/login <password>` Command used to login when at the login server [Velocity Only]
- `/loginpassword(velocity) <subcommand>` 
  - Must be run by a player with permission node `loginpassword.admin` or from the console.
  - `reload` reload the config file and bypass list.
  - `add <uuid|player>` add player to the bypass list
  - `remove <uuid|player>` remove player from the bypass list
  - `list` list uuids in the bypass list
  - `toggle` enable or disable the plugin

I encourage you to look at NanoLimbo's `settings.yml` as well, to see if there are any changes you want to make to the default join messages that appear.

## Contributing

Open an issue or a pull request if you want.

## License

This project is licensed under the GNU Lesser General Public License v2.1. See the `LICENSE` file for details.

## Misc
Logo borrowed from [twemoji](https://github.com/twitter/twemoji), CC-BY 4.0.
