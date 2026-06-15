package traderush.game.team;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import traderush.game.player.PlayerId;

public final class InMemoryTeamRepository implements TeamRepository {

    private final Map<TeamId, Team> teamsById = new LinkedHashMap<>();
    private final Map<String, TeamId> teamsByName = new LinkedHashMap<>();
    private final Map<PlayerId, TeamId> teamsByPlayerId = new LinkedHashMap<>();

    @Override
    public Team put(Team team) {
        TeamId id = team.getId();
        String nameKey = normalizeName(team.getName());

        teamsById.put(id, team);
        teamsByName.entrySet().removeIf(entry -> entry.getValue().equals(id));
        teamsByName.put(nameKey, id);
        teamsByPlayerId.entrySet()
                .removeIf(entry -> entry.getValue().equals(id));

        for (PlayerId playerId : team.getPlayers()) {
            teamsByPlayerId.put(playerId, id);
        }

        return team;
    }

    @Override
    public Optional<Team> getById(TeamId id) {
        return Optional.ofNullable(teamsById.get(id));
    }

    @Override
    public Optional<Team> getByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(teamsByName.get(normalizeName(name)))
                .map(teamsById::get);
    }

    @Override
    public Optional<Team> getByPlayerId(PlayerId playerId) {
        return Optional.ofNullable(teamsByPlayerId.get(playerId))
                .map(teamsById::get);
    }

    @Override
    public List<Team> getAll() {
        return new ArrayList<>(teamsById.values());
    }

    @Override
    public void removeAll() {
        teamsById.clear();
        teamsByName.clear();
        teamsByPlayerId.clear();
    }

    @Override
    public void remove(TeamId id) {
        Team team = teamsById.remove(id);

        if (team == null) {
            return;
        }

        teamsByName.remove(normalizeName(team.getName()));
        teamsByPlayerId.entrySet()
                .removeIf(entry -> entry.getValue().equals(id));
    }

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
