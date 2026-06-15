package traderush.game.offer;

import java.util.List;
import java.util.Optional;
import traderush.game.shop.ShopId;

public interface ActiveOfferRepository {
    ActiveOffer put(ActiveOffer activeOffer);

    Optional<ActiveOffer> getById(ActiveOfferId id);

    List<ActiveOffer> getAllByShopId(ShopId shopId);

    List<ActiveOffer> getAll();
}
