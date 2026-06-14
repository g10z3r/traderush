package traderush.game.team;

import java.util.List;
import java.util.Optional;
import traderush.game.player.PlayerId;

public interface TeamRepository {
    Team put(Team team);
    Optional<Team> getById(TeamId id);
    Optional<Team> getByName(String name);
    Optional<Team> getByPlayerId(PlayerId playerId);
    List<Team> getAll();
    Optional<Team> rename(TeamId id, String name);
    void removeAll();
    void remove(TeamId id);
}
