package traderush.game.shop;

import java.util.List;
import java.util.Optional;

public interface ShopRepository {
    Shop put(Shop shop);
    Optional<Shop> getById(ShopId id);
    Optional<Shop> getByLocation(ShopLocation location);
    List<Shop> getAll();
    List<OfferShop> getAllOfferShops();
    List<ContractShop> getAllContractShops();
}
