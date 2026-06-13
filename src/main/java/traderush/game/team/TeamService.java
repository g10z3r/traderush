package traderush.game.team;

import traderush.game.player.PlayerId;
import java.util.Optional;
import java.util.List;
import java.util.Comparator;

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
        String trimmedName = name == null ? null : name.trim();

        if (trimmedName == null || trimmedName.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_INVALID_NAME);
        }

        if (trimmedName.length() < MIN_TEAM_NAME_LENGTH || trimmedName.length() > MAX_TEAM_NAME_LENGTH) {
            return TeamOperationResult.error(TeamError.TEAM_INVALID_NAME);
        }

        if (teamRepository.getByName(trimmedName).isPresent()) {
            return TeamOperationResult.error(TeamError.TEAM_ALREADY_EXISTS);
        }

        Team team = new Team(trimmedName);
        teamRepository.put(team);
        onStateChanged.run();

        return TeamOperationResult.success(team);
    }

    public TeamOperationResult<Team> joinTeam(PlayerId playerId, String name) {
        String trimmedName = name == null ? null : name.trim();

        if (trimmedName == null || trimmedName.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        Optional<Team> targetTeam = teamRepository.getByName(trimmedName);

        if (targetTeam.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        return joinTeam(playerId, targetTeam.get().getId());
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

    public TeamOperationResult<Team> leaveTeam(PlayerId playerId) {
        Optional<Team> currentTeam = teamRepository.getByPlayerId(playerId);

        if (currentTeam.isEmpty()) {
            return TeamOperationResult.error(TeamError.PLAYER_NOT_IN_TEAM);
        }

        Team team = currentTeam.get();

        team.removePlayer(playerId);
        teamRepository.put(team);

        onStateChanged.run();

        return TeamOperationResult.success(team);
    }

    public List<Team> listTeams() {
        return teamRepository.getAll().stream().sorted(
                Comparator.comparingLong(Team::getScore)
                        .reversed()
                        .thenComparing(Team::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
