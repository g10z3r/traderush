package traderush.game.offer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import traderush.game.shop.ShopId;

public final class InMemoryActiveOfferRepository implements ActiveOfferRepository {
    private final Map<ActiveOfferId, ActiveOffer> activeOffersById = new LinkedHashMap<>();

    @Override
    public ActiveOffer put(ActiveOffer activeOffer) {
        activeOffersById.put(activeOffer.getId(), activeOffer);
        return activeOffer;
    }

    @Override
    public Optional<ActiveOffer> getById(ActiveOfferId id) {
        return Optional.ofNullable(activeOffersById.get(id));
    }

    @Override
    public List<ActiveOffer> getAll() {
        return new ArrayList<>(activeOffersById.values());
    }

    @Override
    public List<ActiveOffer> getAllByShopId(ShopId shopId) {
        return new ArrayList<>(
                activeOffersById.values()
                        .stream()
                        .filter(activeOffer -> activeOffer.getShopId().equals(shopId))
                        .toList()
        );
    }
}
