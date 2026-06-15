package traderush.game.offer;

import traderush.game.shop.ShopId;

public final class LimitedActiveOffer extends ActiveOffer {
    private final int maxAcceptedUnits;

    public LimitedActiveOffer(
            ActiveOfferId id,
            ShopId shopId,
            OfferId offerId,
            long rewardPerUnit,
            int acceptedUnits,
            int maxAcceptedUnits
    ) {
        super(id, shopId, offerId, rewardPerUnit, acceptedUnits);

        if (maxAcceptedUnits <= 0) {
            throw new IllegalArgumentException(
                    "limited active offer max accepted units must be positive"
            );
        }

        if (acceptedUnits > maxAcceptedUnits) {
            throw new IllegalArgumentException(
                    "limited active offer accepted units cannot exceed max accepted units"
            );
        }

        this.maxAcceptedUnits = maxAcceptedUnits;
    }

    @Override
    public OfferKind kind() {
        return OfferKind.LIMITED;
    }

    @Override
    public boolean isAccepting(long currentTick) {
        return getAcceptedUnits() < maxAcceptedUnits;
    }

    @Override
    public int remainingUnitCapacity(long currentTick) {
        return maxAcceptedUnits - getAcceptedUnits();
    }

    public boolean isCompleted() {
        return getAcceptedUnits() >= maxAcceptedUnits;
    }
}
