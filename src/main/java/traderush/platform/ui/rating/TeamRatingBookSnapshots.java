package traderush.platform.ui.rating;

import traderush.game.team.TeamService;

public final class TeamRatingBookSnapshots {

    private TeamRatingBookSnapshots() {}

    public static TeamRatingBookSnapshot create(TeamService teamService) {
        if (teamService == null) {
            return TeamRatingBookSnapshot.RUNTIME_NOT_READY;
        }

        return new TeamRatingBookSnapshot(
                TeamRatingRows.fromTeams(teamService.listTeams()),
                true
        );
    }
}
