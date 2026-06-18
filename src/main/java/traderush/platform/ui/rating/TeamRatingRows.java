package traderush.platform.ui.rating;

import java.util.ArrayList;
import java.util.List;
import traderush.game.team.Team;

public final class TeamRatingRows {

    private TeamRatingRows() {}

    public static List<TeamRatingRow> fromTeams(List<Team> teams) {
        return fromTeams(teams, Integer.MAX_VALUE);
    }

    public static List<TeamRatingRow> fromTeams(List<Team> teams, int limit) {
        if (teams == null || teams.isEmpty() || limit <= 0) {
            return List.of();
        }

        int rowCount = Math.min(teams.size(), limit);
        List<TeamRatingRow> rows = new ArrayList<>(rowCount);

        for (int index = 0; index < rowCount; index++) {
            Team team = teams.get(index);
            rows.add(
                    new TeamRatingRow(
                            index + 1,
                            team.getName(),
                            team.getScore()
                    )
            );
        }

        return rows;
    }
}
