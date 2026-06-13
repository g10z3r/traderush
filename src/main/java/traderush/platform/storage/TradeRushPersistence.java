package traderush.platform.storage;

import traderush.game.team.TeamStateStore;
import net.minecraft.server.MinecraftServer;
import traderush.platform.storage.team.MinecraftTeamStateStore;

public class TradeRushPersistence {
    private final TeamStateStore teamStateStore;

    private TradeRushPersistence(MinecraftServer server) {
        this.teamStateStore = new MinecraftTeamStateStore(server);
    }

    public static TradeRushPersistence create(MinecraftServer server) {
        return new TradeRushPersistence(server);
    }

    public TeamStateStore teamStateStore() {
        return teamStateStore;
    }
}
