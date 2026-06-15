package traderush.game.offer;

import traderush.game.shop.ShopId;

public final class TimedActiveOffer extends ActiveOffer {
    private final long startsAtTick;
    private final long endsAtTick;
    private final long completionThreshold;
    private final boolean repeatable;

    public TimedActiveOffer(
            ActiveOfferId id,
            ShopId shopId,
            OfferId offerId,
            long rewardPerUnit,
            int acceptedUnits,
            long startsAtTick,
            long endsAtTick,
            long completionThreshold,
            boolean repeatable
    ) {
        super(id, shopId, offerId, rewardPerUnit, acceptedUnits);

        if (startsAtTick < 0) {
            throw new IllegalArgumentException(
                    "timed active offer start tick cannot be negative"
            );
        }

        if (endsAtTick <= startsAtTick) {
            throw new IllegalArgumentException(
                    "timed active offer end tick must be greater than start tick"
            );
        }

        if (completionThreshold <= 0) {
            throw new IllegalArgumentException(
                    "timed active offer completion threshold must be positive"
            );
        }

        this.startsAtTick = startsAtTick;
        this.endsAtTick = endsAtTick;
        this.completionThreshold = completionThreshold;
        this.repeatable = repeatable;
    }

    @Override
    public OfferKind kind() {
        return OfferKind.TIMED;
    }

    @Override
    public boolean isAccepting(long currentTick) {
        return currentTick >= startsAtTick && currentTick <= endsAtTick;
    }

    @Override
    public int remainingUnitCapacity(long currentTick) {
        return (int) (endsAtTick - currentTick);
    }

    public boolean isCompleted(long currentTick) {
        return currentTick > endsAtTick;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public boolean isExpired(long currentTick) {
        return currentTick >= endsAtTick;
    }

    public boolean isCompletedByThreshold() {
        return getAcceptedUnits() * getRewardPerUnit() >= completionThreshold;
    }
}
