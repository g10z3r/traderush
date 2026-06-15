package traderush.platform;

import net.minecraft.network.chat.Component;
import traderush.game.team.Team;
import traderush.game.team.TeamError;

public final class TeamMessages {

    private TeamMessages() {}

    public static String errorKey(TeamError error) {
        return switch (error) {
        case TEAM_INVALID_NAME -> "message.trade-rush.team.error.invalid_name";
        case TEAM_ALREADY_EXISTS ->
            "message.trade-rush.team.error.already_exists";
        case TEAM_NOT_FOUND -> "message.trade-rush.team.error.not_found";
        case PLAYER_ALREADY_IN_TEAM ->
            "message.trade-rush.team.error.player_already_in_team";
        case PLAYER_NOT_IN_TEAM ->
            "message.trade-rush.team.error.player_not_in_team";
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
        return Component
                .translatable("message.trade-rush.team.joined", team.getName());
    }

    public static Component left(Team team) {
        return Component
                .translatable("message.trade-rush.team.left", team.getName());
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
