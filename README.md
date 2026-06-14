# Trade Rush

A Fabric mod for Minecraft.

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## Team Management Block

Trade Rush adds a `trade-rush:team_management_block` block and block item.

The Team Management Block provides a UI for managing TradeRush teams through the team features described below.

## Commands

Team commands are available under `/traderush` and the short alias `/tr`.

### `/traderush team create <name>`

Creates a new team.

- Team names are trimmed and must be between 3 and 64 characters.
- Team names must be unique.
- Can be run by any command source (players, operators, or the server console).


### `/traderush team join <name>`

Adds the executing player to the named team.

- If the player is already in another team, they are moved to the new one.
- Team names support tab completion.
- Must be run by a player.


### `/traderush team leave`

Removes the executing player from their current team.

- Must be run by a player.


### `/traderush team delete <name>`

Deletes a team.

- The team must be empty. Use `delete force` to remove a team that still has members.
- Team names support tab completion.
- Can be run by any command source.


### `/traderush team delete force <name>`

Force-deletes a team, even if it still has members.

- Team names support tab completion.
- Can be run by any command source.


### `/traderush team list`

Lists all teams, sorted by score (highest first), then by name.


### Error messages

| Error | Message |
| --- | --- |
| Invalid team name | Team name must be between 3 and 64 characters. |
| Duplicate team | Team already exists. |
| Unknown team | Team does not exist. |
| Already in team | You are already in a team. |
| Not in a team | You are not in a team. |
| Team not empty | Team cannot be deleted while it has members. |

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
