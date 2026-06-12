package traderush.game;

import java.io.IOException;
import java.util.Optional;

public interface GameStateStore {
    Optional<GameStateSnapshot> load() throws IOException;
    void save(GameStateSnapshot snapshot) throws IOException;
}
