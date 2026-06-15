package traderush.platform.storage;

import net.minecraft.server.MinecraftServer;
import traderush.game.shop.ShopStateStore;
import traderush.game.team.TeamStateStore;
import traderush.game.world.WorldStateStore;
import traderush.platform.storage.shop.MinecraftShopStateStore;
import traderush.platform.storage.team.MinecraftTeamStateStore;
import traderush.platform.storage.world.MinecraftWorldStateStore;

public class TradeRushPersistence {
    private final TeamStateStore teamStateStore;
    private final WorldStateStore worldStateStore;
    private final ShopStateStore shopStateStore;

    private TradeRushPersistence(MinecraftServer server) {
        this.teamStateStore = new MinecraftTeamStateStore(server);
        this.worldStateStore = new MinecraftWorldStateStore(server);
        this.shopStateStore = new MinecraftShopStateStore(server);
    }

    public static TradeRushPersistence create(MinecraftServer server) {
        return new TradeRushPersistence(server);
    }

    public TeamStateStore teamStateStore() {
        return teamStateStore;
    }

    public WorldStateStore worldStateStore() {
        return worldStateStore;
    }

    public ShopStateStore shopStateStore() {
        return shopStateStore;
    }
}
