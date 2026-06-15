package traderush.game.team;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traderush.game.player.PlayerId;

public final class TeamStateMapper {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TeamStateMapper.class);

    private TeamStateMapper() {}

    public static TeamStateSnapshot toSnapshot(TeamRepository repository) {
        List<TeamStateSnapshot.TeamSnapshot> teams = repository.getAll()
                .stream()
                .map(TeamStateMapper::toTeamSnapshot)
                .toList();

        return new TeamStateSnapshot(TeamStateSnapshot.CURRENT_VERSION, teams);
    }

    public static void restoreInto(
            TeamRepository repository,
            TeamStateSnapshot snapshot
    ) {
        if (snapshot == null) {
            return;
        }

        repository.removeAll();

        for (TeamStateSnapshot.TeamSnapshot teamSnapshot : snapshot.teams()) {
            restoreTeam(repository, teamSnapshot);
        }
    }

    private static TeamStateSnapshot.TeamSnapshot toTeamSnapshot(Team team) {
        List<String> members = team.getPlayers()
                .stream()
                .map(PlayerId::toString)
                .toList();

        return new TeamStateSnapshot.TeamSnapshot(
                team.getId().toString(),
                team.getName(),
                team.getScore(),
                members
        );
    }

    private static void restoreTeam(
            TeamRepository repository,
            TeamStateSnapshot.TeamSnapshot snapshot
    ) {
        if (snapshot.id() == null || snapshot.id().isBlank()) {
            LOGGER.warn("Skipping team snapshot with empty id.");
            return;
        }

        if (snapshot.name() == null || snapshot.name().isBlank()) {
            LOGGER.warn(
                    "Skipping team snapshot with empty name. Team id: {}",
                    snapshot.id()
            );
            return;
        }

        TeamId teamId;
        try {
            teamId = TeamId.fromString(snapshot.id());
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(
                    "Skipping team snapshot with invalid id: {}",
                    snapshot.id()
            );
            return;
        }

        Set<PlayerId> members = new LinkedHashSet<>();

        for (String memberId : snapshot.members()) {
            if (memberId == null || memberId.isBlank()) {
                continue;
            }

            try {
                members.add(PlayerId.fromString(memberId));
            } catch (IllegalArgumentException exception) {
                LOGGER.warn(
                        "Skipping invalid member id '{}' in team '{}'.",
                        memberId,
                        snapshot.name()
                );
            }
        }

        try {
            repository.put(
                    new Team(teamId, snapshot.name(), members, snapshot.score())
            );
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(
                    "Skipping invalid team snapshot. Team id: {}",
                    snapshot.id(),
                    exception
            );
        }
    }
}
