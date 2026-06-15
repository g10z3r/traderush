package traderush.game.offer;

import java.util.List;
import java.util.Optional;

public interface OfferRepository {
    Offer put(Offer offer);
    Optional<Offer> getById(OfferId id);
    List<Offer> getAll();
    List<LimitedOffer> getAllLimited();
    List<TimedOffer> getAllTimed();
}
