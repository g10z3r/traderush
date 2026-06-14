package traderush.platform;

import net.minecraft.network.chat.Component;
import traderush.game.team.Team;
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
            case TEAM_NOT_EMPTY -> "Team still has members.";
        };
    }

    public static String errorKey(TeamError error) {
        return switch (error) {
            case TEAM_INVALID_NAME -> "message.trade-rush.team.error.invalid_name";
            case TEAM_ALREADY_EXISTS -> "message.trade-rush.team.error.already_exists";
            case TEAM_NOT_FOUND -> "message.trade-rush.team.error.not_found";
            case PLAYER_ALREADY_IN_TEAM -> "message.trade-rush.team.error.player_already_in_team";
            case PLAYER_NOT_IN_TEAM -> "message.trade-rush.team.error.player_not_in_team";
            case TEAM_NOT_EMPTY -> "message.trade-rush.team.error.team_not_empty";
        };
    }

    public static Component componentFor(TeamError error) {
        return Component.translatable(errorKey(error));
    }

    public static Component created(Team team) {
        return Component.translatable(
            "message.trade-rush.team.created",
            team.getName()
        );
    }

    public static Component joined(Team team) {
        return Component.translatable(
            "message.trade-rush.team.joined",
            team.getName()
        );
    }

    public static Component left(Team team) {
        return Component.translatable(
            "message.trade-rush.team.left",
            team.getName()
        );
    }

    public static Component deleted(Team team) {
        return Component.translatable(
            "message.trade-rush.team.deleted",
            team.getName()
        );
    }

    public static Component forceDeleted(Team team) {
        return Component.translatable(
            "message.trade-rush.team.force_deleted",
            team.getName()
        );
    }

    public static Component renamed(Team team) {
        return Component.translatable(
            "message.trade-rush.team.renamed",
            team.getName()
        );
    }
}
