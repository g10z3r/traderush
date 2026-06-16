package traderush.game.shop;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ShopService {
    private final ShopRepository shopRepository;
    private final Runnable onStateChanged;

    public ShopService(ShopRepository shopRepository, Runnable onStateChanged) {
        this.shopRepository = Objects
                .requireNonNull(
                        shopRepository,
                        "shop repository cannot be null"
                );
        this.onStateChanged = Objects
                .requireNonNull(
                        onStateChanged,
                        "on state changed cannot be null"
                );
    }

    public ShopOperationResult<OfferShop> registerOfferShop(
            String name,
            ShopLocation location
    ) {
        Objects.requireNonNull(location, "shop location cannot be null");

        if (shopRepository.getByLocation(location).isPresent()) {
            return ShopOperationResult
                    .failure(ShopError.SHOP_LOCATION_ALREADY_OCCUPIED);
        }

        OfferShop shop = new OfferShop(
                ShopId.fromUuid(UUID.randomUUID()),
                name,
                location
        );

        shopRepository.put(shop);
        onStateChanged.run();

        return ShopOperationResult.success(shop);
    }

    public List<Shop> listShops() {
        return shopRepository.getAll();
    }

    public java.util.Optional<Shop> findByLocation(ShopLocation location) {
        return shopRepository.getByLocation(location);
    }
}
