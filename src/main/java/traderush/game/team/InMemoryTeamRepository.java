package traderush.game.team;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

import traderush.game.player.PlayerId;

public final class InMemoryTeamRepository implements TeamRepository {
    private final Map<TeamId, Team> teamsById = new LinkedHashMap<>();
    private final Map<String, TeamId> teamsByName = new LinkedHashMap<>();
    private final Map<PlayerId, TeamId> teamsByPlayerId = new LinkedHashMap<>();

    @Override
    public Team put(Team team) {
        TeamId id = team.getId();
        String name = team.getName();

        teamsById.put(id, team);
        teamsByName.put(name, id);
        teamsByPlayerId.put(team.getPlayers().iterator().next(), id);

        return team;
    }

    @Override
    public Optional<Team> getById(TeamId id) {
        return Optional.ofNullable(teamsById.get(id));
    }

    @Override
    public Optional<Team> getByName(String name) {
        return Optional.ofNullable(teamsByName.get(name)).map(teamsById::get);
    }

    @Override
    public Optional<Team> getByPlayerId(PlayerId playerId) {
        return Optional.ofNullable(teamsByPlayerId.get(playerId)).map(teamsById::get);
    }

    @Override
    public List<Team> getAll() {
        return new ArrayList<>(teamsById.values());
    }

    @Override
    public void remove(TeamId id) {
        teamsById.remove(id);
    }
}
