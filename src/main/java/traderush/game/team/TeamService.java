package traderush.game.team;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import traderush.game.player.PlayerId;

public final class TeamService {

    public static final int MIN_TEAM_NAME_LENGTH = 3;
    public static final int MAX_TEAM_NAME_LENGTH = 64;

    private final TeamRepository teamRepository;
    private final Runnable onStateChanged;

    public TeamService(TeamRepository teamRepository, Runnable onStateChanged) {
        this.teamRepository = teamRepository;
        this.onStateChanged = onStateChanged;
    }

    public TeamOperationResult<Team> createTeam(String name) {
        String trimmedName = trimName(name);
        Optional<TeamError> validationError = validateTeamName(trimmedName);

        if (validationError.isPresent()) {
            return TeamOperationResult.error(validationError.get());
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
        String trimmedName = trimName(name);

        if (trimmedName == null || trimmedName.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        Optional<Team> targetTeam = teamRepository.getByName(trimmedName);

        if (targetTeam.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        return joinTeam(playerId, targetTeam.get().getId());
    }

    public TeamOperationResult<Team> joinTeam(
        PlayerId playerId,
        TeamId teamId
    ) {
        Optional<Team> targetTeam = teamRepository.getById(teamId);

        if (targetTeam.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        Team team = targetTeam.get();
        Optional<Team> currentTeam = getTeamForPlayer(playerId);

        if (
            currentTeam.isPresent() && currentTeam.get().getId().equals(teamId)
        ) {
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
        Optional<Team> currentTeam = getTeamForPlayer(playerId);

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
        return teamRepository
            .getAll()
            .stream()
            .sorted(
                Comparator.comparingLong(Team::getScore)
                    .reversed()
                    .thenComparing(Team::getName, String.CASE_INSENSITIVE_ORDER)
            )
            .toList();
    }

    public Optional<Team> getTeamForPlayer(PlayerId playerId) {
        if (playerId == null) {
            return Optional.empty();
        }

        return teamRepository.getByPlayerId(playerId);
    }

    public TeamOperationResult<Team> renameTeam(
        String currentName,
        String newName
    ) {
        String trimmedCurrentName = trimName(currentName);

        if (trimmedCurrentName == null || trimmedCurrentName.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        Optional<Team> team = teamRepository.getByName(trimmedCurrentName);

        if (team.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        return renameTeam(team.get().getId(), newName);
    }

    public TeamOperationResult<Team> renameTeam(TeamId teamId, String newName) {
        Optional<Team> team = teamRepository.getById(teamId);

        if (team.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        Team toRename = team.get();
        String trimmedNewName = trimName(newName);
        Optional<TeamError> validationError = validateTeamName(trimmedNewName);

        if (validationError.isPresent()) {
            return TeamOperationResult.error(validationError.get());
        }

        if (sameName(toRename.getName(), trimmedNewName)) {
            return TeamOperationResult.success(toRename);
        }

        if (teamRepository.getByName(trimmedNewName).isPresent()) {
            return TeamOperationResult.error(TeamError.TEAM_ALREADY_EXISTS);
        }

        toRename.rename(trimmedNewName);
        Team renamedTeam = teamRepository.put(toRename);
        onStateChanged.run();

        return TeamOperationResult.success(renamedTeam);
    }

    public TeamOperationResult<Team> deleteTeam(TeamId teamId) {
        return deleteTeam(teamId, false);
    }

    public TeamOperationResult<Team> deleteTeam(String name, boolean force) {
        String trimmedName = trimName(name);

        if (trimmedName == null || trimmedName.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        Optional<Team> team = teamRepository.getByName(trimmedName);

        if (team.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        return deleteTeam(team.get(), force);
    }

    public TeamOperationResult<Team> deleteTeam(TeamId teamId, boolean force) {
        Optional<Team> team = teamRepository.getById(teamId);

        if (team.isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_FOUND);
        }

        return deleteTeam(team.get(), force);
    }

    private TeamOperationResult<Team> deleteTeam(Team toDelete, boolean force) {
        if (!force && !toDelete.getPlayers().isEmpty()) {
            return TeamOperationResult.error(TeamError.TEAM_NOT_EMPTY);
        }

        teamRepository.remove(toDelete.getId());
        onStateChanged.run();

        return TeamOperationResult.success(toDelete);
    }

    private static Optional<TeamError> validateTeamName(String trimmedName) {
        if (trimmedName == null || trimmedName.isEmpty()) {
            return Optional.of(TeamError.TEAM_INVALID_NAME);
        }

        if (
            trimmedName.length() < MIN_TEAM_NAME_LENGTH ||
            trimmedName.length() > MAX_TEAM_NAME_LENGTH
        ) {
            return Optional.of(TeamError.TEAM_INVALID_NAME);
        }

        return Optional.empty();
    }

    private static String trimName(String name) {
        return name == null ? null : name.trim();
    }

    private static boolean sameName(String left, String right) {
        return left
            .trim()
            .toLowerCase(Locale.ROOT)
            .equals(right.trim().toLowerCase(Locale.ROOT));
    }
}
