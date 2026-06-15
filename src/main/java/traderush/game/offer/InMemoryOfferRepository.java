package traderush.game.offer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryOfferRepository implements OfferRepository {
    private final Map<OfferId, Offer> offersById = new LinkedHashMap<>();

    @Override
    public Offer put(Offer offer) {
        offersById.put(offer.getId(), offer);
        return offer;
    }

    @Override
    public Optional<Offer> getById(OfferId id) {
        return Optional.ofNullable(offersById.get(id));
    }

    @Override
    public List<Offer> getAll() {
        return new ArrayList<>(offersById.values());
    }

    @Override
    public List<LimitedOffer> getAllLimited() {
        return offersById.values()
                .stream()
                .filter(offer -> offer instanceof LimitedOffer)
                .map(LimitedOffer.class::cast)
                .toList();
    }

    @Override
    public List<TimedOffer> getAllTimed() {
        return offersById.values()
                .stream()
                .filter(offer -> offer instanceof TimedOffer)
                .map(TimedOffer.class::cast)
                .toList();
    }
}
