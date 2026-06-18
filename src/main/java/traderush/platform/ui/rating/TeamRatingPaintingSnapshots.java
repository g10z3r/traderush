package traderush.platform.ui.rating;

import java.util.List;
import traderush.game.team.Team;
import traderush.game.team.TeamService;

public final class TeamRatingPaintingSnapshots {

    private TeamRatingPaintingSnapshots() {}

    public static TeamRatingPaintingSnapshot create(TeamService teamService) {
        if (teamService == null) {
            return TeamRatingPaintingSnapshot.RUNTIME_NOT_READY;
        }

        List<Team> teams = teamService.listTeams();
        return new TeamRatingPaintingSnapshot(
                TeamRatingRows.fromTeams(
                        teams,
                        TeamRatingPaintingSnapshot.MAX_VISIBLE_ROWS
                ),
                true
        );
    }
}
