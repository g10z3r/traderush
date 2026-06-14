package traderush.game.shop;

import java.io.IOException;
import java.util.Optional;

public interface ShopStateStore {
    Optional<ShopStateSnapshot> load() throws IOException;
    void save(ShopStateSnapshot snapshot) throws IOException;

}
