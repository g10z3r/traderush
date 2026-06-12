package traderush.game.team;

import traderush.game.player.PlayerId;

import java.util.List;
import java.util.Optional;

public interface TeamRepository {
    Team put(Team team);
    Optional<Team> getById(TeamId id);
    Optional<Team> getByName(String name);
    Optional<Team> getByPlayerId(PlayerId playerId);
    List<Team> getAll();
    void remove(TeamId id);
}
