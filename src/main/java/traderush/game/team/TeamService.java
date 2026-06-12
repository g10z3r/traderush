package traderush.game.team;

import traderush.game.player.PlayerId;
import java.util.Optional;

public final class TeamService {
    private static final int MIN_TEAM_NAME_LENGTH = 3;
    private static final int MAX_TEAM_NAME_LENGTH = 64;

    private final TeamRepository teamRepository;
    private final Runnable onStateChanged;

    public TeamService(TeamRepository teamRepository, Runnable onStateChanged) {
        this.teamRepository = teamRepository;
        this.onStateChanged = onStateChanged;
    }

    public TeamOperationResult<Team> createTeam(String name) {
        if (name == null || name.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_INVALID_NAME);
        }

        if (name.length() < MIN_TEAM_NAME_LENGTH || name.length() > MAX_TEAM_NAME_LENGTH) {
            return TeamOperationResult.error(TeamError.TEAM_INVALID_NAME);
        }

        if (teamRepository.getByName(name).isPresent()) {
            return TeamOperationResult.error(TeamError.TEAM_ALREADY_EXISTS);
        }

        Team team = new Team(name);
        teamRepository.put(team);
        onStateChanged.run();


        return TeamOperationResult.success(team);
    }

    public TeamOperationResult<Team> joinTeam(PlayerId playerId, TeamId teamId) {
        Optional<Team> targetTeam = teamRepository.getById(teamId);

        if (targetTeam.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        Team team = targetTeam.get();
        Optional<Team> currentTeam = teamRepository.getByPlayerId(playerId);

        if (currentTeam.isPresent() && currentTeam.get().getId().equals(teamId)) {
            return TeamOperationResult.error(TeamError.PLAYER_ALREADY_IN_TEAM);
        }

        currentTeam.ifPresent(existingTeam -> {
            existingTeam.removePlayer(playerId);
            teamRepository.put(existingTeam);
        });

        team.addPlayer(playerId);
        teamRepository.put(team);

        onStateChanged.run();

        return TeamOperationResult.success(team);
    }
}
