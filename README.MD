# HomeSweetHome
HomeSweetHome is an advanced and customizable homes plugin. HomeSweetHome requires KatLib.

## Features
* Customizable home limit for each player
* Permissions to manage exactly what players can do
* Invite players to other homes
* Permanent invites in the form of residents for a home

## Commands

### Normal home commands
These commands are what most players will use when using homes
* `/home <home>` Teleport to a home you have created
* `/home set <home>` Set a new home
* `/home remove <home>` Remove an already existing home
* `/home list` Check your homes
* `/home limit` Check how many homes you can have
* `/home invite <player> <home>` Invite another player to your home
* `/home goto <user> <home>`Go to another user's home where you are allowed to go (you have been invited, or you are a resident). If you are not allowed to go to that home, instead sends a request
* `/home accept <player>` Accept a home request from another player

### Residents commands
Use these commands to manage the residents of a home. A resident is another player that is permanently allowed to teleort to your home.
* `/home residents <home>` Check the current residents of this home
* `/home residents add <player> <home>` Add a new resident to this home
* `/home residents remove <player> <home>` Remove a resident from a home
* `/home residents limit` Check home many residents you can have for homes

### Other commands
HomeSweetHome also gives admins/mods additional commands to manage the homes of other players. You can do almost everything you can do for the normal commands with the other commands too.
* `/home other <user> <home>` Teleport to another user's home
* `/home other set <user> <home>` Set a home for another user
* `/home other remove <user> <home>` Remove a home for another user
* `/home other list <user>` List the homes of another user
* `/home other limit <user>` Check another user's home limit
* `/home other invite <user> <player> <home>` Invite a player to another user's home. Your name will be shown in the invite

### Other residents commands
You can also manage the residents of other homes
* `/home other residents <user> <home>` Check the current residents of this home for this user
* `/home other residents add <user> <player> <home>` Adds a player as a resident to the home of another user
* `/home other residents remove <user> <player> <home>` Remove a player from the residents of another home
* `/home other residents limit <user>` Check home name residents another user can have for homes

### Misc commands
Some other commands that don't deal with homes directly
* `/homeSweetHome` Shows information about HomeSweetHome
* `/homeSweetHome help [command with subcommand]` Shows help for HomeSweetHome commands, also works with subcommands like `/home set`

## Permissions

### Normal permissions
* `homeSweetHome.home.tp` Allows a player to use `/home`
* `homeSweetHome.home.set` Allows a player to use `/home set`
* `homeSweetHome.home.remove` Allows a player to use `/home remove`
* `homeSweetHome.home.list` Allows a player to use `/home list`
* `homeSweetHome.home.limit` Allows a player to use `/home limit`
* `homeSweetHome.home.invite` Allows a player to use `/home invite`
* `homeSweetHome.home.goto` Allows a player to use `/home goto`
* `homeSweetHome.home.accept` Allows a player to use `/home accept`

### Residents permissions
* `homeSweetHome.home.residents.list` Allows a player to use `/home residents`
* `homeSweetHome.home.residents.add` Allows a player to use `/home residents add`
* `homeSweetHome.home.residents.remove` Allows a player to use `/home residents remove`
* `homeSweetHome.home.residents.limit` Allows a player to use `/home residents limit`

### Other permissions
* `homeSweetHome.homeother.tp` Allows a player to use `/home other`
* `homeSweetHome.homeother.set` Allows a player to use `/home other set`
* `homeSweetHome.homeother.remove` Allows a player to use `/home other remove`
* `homeSweetHome.homeother.list` Allows a player to use `/home other list`
* `homeSweetHome.homeother.limit` Allows a player to use `/home other limit`
* `homeSweetHome.homeother.invite` Allows a player to use `/home other invite`

### Other residents permissions
* `homeSweetHome.homeother.residents.list` Allows a player to use `/home other residents`
* `homeSweetHome.homeother.residents.add` Allows a player to use `/home other residents add`
* `homeSweetHome.homeother.residents.remove` Allows a player to use `/home other residents remove`
* `homeSweetHome.homeother.residents.limit` Allows a player to use `/home other residents limit`

### Options
To set how many homes and residents a player can have you can use asome permission options
* `homeSweetHome.homeLimit` The amount of homes a player can have
* `homeSweetHome.residentsLimit` The amount of residents a player can have for a home
