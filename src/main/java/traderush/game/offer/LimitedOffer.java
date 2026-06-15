package traderush.game.offer;

import java.util.List;

public final class LimitedOffer extends Offer {
    private final int maxAcceptedUnits;

    public LimitedOffer(
            OfferId id,
            String name,
            String description,
            RewardRange rewardRange,
            List<OfferUnit> units,
            List<ActivationEventKey> activationEvents,
            int maxAcceptedUnits
    ) {
        super(id, name, description, rewardRange, units, activationEvents);

        if (maxAcceptedUnits <= 0) {
            throw new IllegalArgumentException("limited offer max accepted units must be positive");
        }

        this.maxAcceptedUnits = maxAcceptedUnits;
    }

    @Override
    public OfferKind getKind() {
        return OfferKind.LIMITED;
    }

    public int getMaxAcceptedUnits() {
        return maxAcceptedUnits;
    }
}
