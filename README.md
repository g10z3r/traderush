# Trade Rush

A Fabric mod for Minecraft.

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## Blocks

Trade Rush adds the following blocks:

### Team Management Block

- Block ID: `trade-rush:team_management_block`
- Includes a matching block item.
- Provides an in-game UI for the team-management features available through `/traderush team`.

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


### `/traderush team rename <currentName> <newName>`

Renames an existing team.

- Also available through the short alias: `/tr team rename <currentName> <newName>`.
- The current team must exist; it may have members.
- `currentName` supports tab completion and quoted names. Use quotes when the current name contains spaces, for example: `/traderush team rename "Red Team" Blue Team`.
- `newName` is parsed as the rest of the command, so it may contain spaces without quotes.
- The new name is trimmed, must be between 3 and 64 characters, and must be unique.
- Can be run by any command source.


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
| Team not empty | Team still has members. |

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
