package traderush.game.shop;

import java.util.Optional;

public interface ShopStateStore {
    Optional<ShopStateSnapshot> load();
    void save(ShopStateSnapshot snapshot);
}
