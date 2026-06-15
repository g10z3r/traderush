package traderush.platform.storage.shop;

import net.minecraft.server.MinecraftServer;
import traderush.game.shop.ShopStateSnapshot;
import traderush.game.shop.ShopStateStore;

import java.util.Optional;

public final class MinecraftShopStateStore implements ShopStateStore {
    private final MinecraftServer server;

    public MinecraftShopStateStore(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Optional<ShopStateSnapshot> load() {
        return ShopSavedData.find(server).map(ShopSavedData::snapshot);
    }

    @Override
    public void save(ShopStateSnapshot snapshot) {
        ShopSavedData.getOrCreate(server).setSnapshot(snapshot);
    }
}
