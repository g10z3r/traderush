package traderush.game.shop;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public final class InMemoryShopRepository implements ShopRepository {
    private final Map<ShopId, Shop> shopsById = new LinkedHashMap<>();

    @Override
    public Shop put(Shop shop) {
        ShopId id = shop.getId();

        shopsById.put(id, shop);
        return shop;
    }

    @Override
    public Optional<Shop> getById(ShopId id) {
        return Optional.ofNullable(shopsById.get(id));
    }

    @Override
    public Optional<Shop> getByLocation(ShopLocation location) {
        return shopsById.values()
                .stream()
                .filter(shop -> shop.getLocation().equals(location))
                .findFirst();
    }

    @Override
    public List<Shop> getAll() {
        return new ArrayList<>(shopsById.values());
    }

    @Override
    public List<OfferShop> getAllOfferShops() {
        return shopsById.values()
                .stream()
                .filter(OfferShop.class::isInstance)
                .map(OfferShop.class::cast)
                .toList();
    }

    @Override
    public List<ContractShop> getAllContractShops() {
        return shopsById.values()
                .stream()
                .filter(ContractShop.class::isInstance)
                .map(ContractShop.class::cast)
                .toList();
    }
}
