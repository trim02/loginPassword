# LoginPassword Plugin

## Overview

This plugin is an alternative way to control access to a minecraft server, without using a whitelist.

This plugin is a Velocity plugin, and must be installed on your proxy!

This is NOT a plugin that adds user registration or any of that other stuff you'll find by other authentication plugins on the web. This is a simple plugin to restrict access to your server, without having to maintain a whitelist or using discord integrations and whatnot.

## Features

- **Password Authentication**: Players must enter a password to access the server.
- **One-Time Login**: Optionally allows players to authenticate only once.
- **Plugin granted bypass permissions**: Integrates with LuckPerms to grant bypass permissions.
- **Configurable Settings**: Various settings can be configured through a YAML configuration file.
- **Kick on Timeout**: Players who fail to provide the correct password within a specified timeout are kicked from the server.
- **Customizable Messages**: Custom messages for kick, no password, and wrong password.

## Configuration

**LuckPerms is required if you want the plugin to grant bypass permissions! Install it on your Velocity Proxy!**

**You will need a server to function as a login server. Any minecraft server will do, but I recommend [NanoLimbo](https://github.com/Nan1t/NanoLimbo), as it is extremely lightweight, and will only exist to facilitate login requests**

The plugin uses a YAML configuration file (`config.yml`) to manage its settings. Below are some of the configurable options, comments are included in the file:

- `loginServer`: The server where players are redirected for login.
- `hubServer`: The main server.
- `serverPassword`: The password required for login.
- `oneTimeLogin`: Boolean to enable/disable one-time login. Default is `true`.
- `bypassNode`: The permission node for bypassing login.
- `pluginGrantsBypass`: Boolean to enable/disable plugin-granted bypass. Default is `true`.
- `bypassMethod`: Method to grant bypass permissions. Options are `user` or `group`.
- `bypassGroup`: The group to add the player to if `bypassMethod` is `group`.
- `disableLoginCommandOnBypass`: Boolean to disable login command if bypass is granted. Default is `true`.
- `kickMessage`: Message displayed when a player is kicked.
- `kickTimeout`: Timeout duration before a player is kicked.
- `noPassword`: Message displayed when no password is provided.
- `wrongPassword`: Message displayed when the wrong password is provided.
- `loginCommandGrantedToEveryone`: Boolean to grant the login command to everyone. Default is `true`.
- `loginCommandNode`: The permission node for the login command. Default is `loginpassword.login`.

## Installation

- LuckPerms is optional (but recommended)
- A server to act as a login server is required!
- It is recommended to negate the permission node `velocity.command.server` for all players in your permissions plugin, otherwise players will be able to bypass the plugin by directly transferring to the hub server.

1. Download the plugin JAR file.
2. Place the JAR file in the `plugins` directory of your Velocity server.
3. Start the server to generate the default configuration file.
4. Edit the `config.yml` file in the `plugins/LoginPassword` directory to suit your needs.
5. Configure `velocity.toml` by adding the login server to `[servers]` table. If you want `ping-passthrough = "all"` to work correctly, have only the hub server in the `try = []` array, do not add the login server to the array. The plugin will handle redirecting players to the login server.
6. Restart the server or run `velocity reload` to reload all plugins.

## Usage

Once installed and configured, the plugin will prompt players to enter a password upon connecting to the server. Depending on the configuration, players may only need to authenticate once. Administrators can manage permissions using LuckPerms or another permissions plugin to grant or revoke bypass permissions.

## Contributing

Open an issue or a pull request if you want.

## License

This project is licensed under the GNU Lesser General Public License v2.1. See the `LICENSE` file for details.

## Misc
Logo borrowed from [twemoji](https://github.com/twitter/twemoji), CC-BY 4.0.
