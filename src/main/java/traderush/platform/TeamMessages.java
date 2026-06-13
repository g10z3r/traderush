package traderush.platform;

import net.minecraft.network.chat.Component;
import traderush.game.team.TeamError;

public final class TeamMessages {
    private TeamMessages() {}

    public static String forError(TeamError error) {
        return switch (error) {
        case TEAM_INVALID_NAME -> "Team name must be between 3 and 64 characters.";
        case TEAM_ALREADY_EXISTS -> "Team already exists.";
        case TEAM_NOT_FOUND -> "Team does not exist.";
        case PLAYER_ALREADY_IN_TEAM -> "You are already in a team.";
        case PLAYER_NOT_IN_TEAM -> "You are not in a team.";
        };
    }

    public static Component componentFor(TeamError error) {
        return Component.literal(forError(error));
    }
}
