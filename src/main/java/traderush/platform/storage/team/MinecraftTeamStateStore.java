package traderush.platform.storage.team;

import traderush.game.team.TeamStateStore;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import traderush.game.team.TeamStateSnapshot;

public final class MinecraftTeamStateStore implements TeamStateStore {
    private final MinecraftServer server;

    public MinecraftTeamStateStore(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Optional<TeamStateSnapshot> load() throws IOException {
        return TeamSavedData.find(server).map(TeamSavedData::snapshot);
    }

    @Override
    public void save(TeamStateSnapshot snapshot) throws IOException {
        TeamSavedData.getOrCreate(server).setSnapshot(snapshot);
    }

}
