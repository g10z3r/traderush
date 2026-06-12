package traderush.game.team;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

public final class InMemoryTeamRepository implements TeamRepository {
    private final Map<TeamId, Team> teamsById = new LinkedHashMap<>();

    @Override
    public Team put(Team team) {
        return teamsById.put(team.getId(), team);
    }

    @Override
    public Optional<Team> getById(TeamId id) {
        return Optional.ofNullable(teamsById.get(id));
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
