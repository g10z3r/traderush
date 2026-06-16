package traderush.game.world;

import java.io.IOException;
import java.util.Optional;

public interface WorldStateStore {
    Optional<WorldStateSnapshot> load() throws IOException;

    void save(WorldStateSnapshot snapshot) throws IOException;
}
