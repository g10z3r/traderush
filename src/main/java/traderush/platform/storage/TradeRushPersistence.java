package traderush.platform.storage;

import net.minecraft.server.MinecraftServer;
import traderush.game.shop.ShopStateStore;
import traderush.game.team.TeamStateStore;
import traderush.platform.storage.shop.MinecraftShopStateStore;
import traderush.platform.storage.team.MinecraftTeamStateStore;

public class TradeRushPersistence {
    private final TeamStateStore teamStateStore;
    private final ShopStateStore shopStateStore;

    private TradeRushPersistence(MinecraftServer server) {
        this.teamStateStore = new MinecraftTeamStateStore(server);
        this.shopStateStore = new MinecraftShopStateStore(server);
    }

    public static TradeRushPersistence create(MinecraftServer server) {
        return new TradeRushPersistence(server);
    }

    public TeamStateStore teamStateStore() {
        return teamStateStore;
    }

    public ShopStateStore shopStateStore() {
        return shopStateStore;
    }
}
