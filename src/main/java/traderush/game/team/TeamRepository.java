package traderush.game.team;

import java.util.List;
import java.util.Optional;

public interface TeamRepository {
    Team put(Team team);
    Optional<Team> getById(TeamId id);
    List<Team> getAll();
    void remove(TeamId id);
}
