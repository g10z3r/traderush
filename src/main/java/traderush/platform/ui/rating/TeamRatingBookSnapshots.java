package traderush.platform.ui.rating;

import java.util.ArrayList;
import java.util.List;
import traderush.game.team.Team;
import traderush.game.team.TeamService;
import traderush.platform.ui.rating.TeamRatingBookSnapshot.Row;

public final class TeamRatingBookSnapshots {

    private TeamRatingBookSnapshots() {}

    public static TeamRatingBookSnapshot create(TeamService teamService) {
        if (teamService == null) {
            return TeamRatingBookSnapshot.RUNTIME_NOT_READY;
        }

        List<Team> teams = teamService.listTeams();
        List<Row> rows = new ArrayList<>(teams.size());

        for (int index = 0; index < teams.size(); index++) {
            Team team = teams.get(index);
            rows.add(new Row(index + 1, team.getName(), team.getScore()));
        }

        return new TeamRatingBookSnapshot(rows, true);
    }
}
