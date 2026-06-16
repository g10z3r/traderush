package traderush.platform.storage.world;

import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import traderush.game.world.WorldStateSnapshot;
import traderush.game.world.WorldStateStore;

public final class MinecraftWorldStateStore implements WorldStateStore {
    private final MinecraftServer server;

    public MinecraftWorldStateStore(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Optional<WorldStateSnapshot> load() {
        return WorldSavedData.find(server).map(WorldSavedData::snapshot);
    }

    @Override
    public void save(WorldStateSnapshot snapshot) {
        WorldSavedData.getOrCreate(server).setSnapshot(snapshot);
    }
}
