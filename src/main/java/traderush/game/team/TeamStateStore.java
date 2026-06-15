package traderush.game.team;

import java.io.IOException;
import java.util.Optional;

public interface TeamStateStore {
    Optional<TeamStateSnapshot> load() throws IOException;

    void save(TeamStateSnapshot snapshot) throws IOException;
}
