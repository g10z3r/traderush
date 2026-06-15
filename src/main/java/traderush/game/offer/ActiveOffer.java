package traderush.game.offer;

import java.util.Objects;
import traderush.game.shop.ShopId;

public abstract class ActiveOffer {
    private final ActiveOfferId id;
    private final ShopId shopId;
    private final OfferId offerId;
    private final long rewardPerUnit;
    private int acceptedUnits;

    protected ActiveOffer(
            ActiveOfferId id,
            ShopId shopId,
            OfferId offerId,
            long rewardPerUnit,
            int acceptedUnits
    ) {
        this.id = Objects.requireNonNull(id, "active offer id cannot be null");
        this.shopId = Objects.requireNonNull(shopId, "shop id cannot be null");
        this.offerId = Objects
                .requireNonNull(offerId, "offer id cannot be null");

        if (rewardPerUnit <= 0) {
            throw new IllegalArgumentException(
                    "active offer reward per unit must be positive"
            );
        }

        if (acceptedUnits < 0) {
            throw new IllegalArgumentException(
                    "active offer accepted units cannot be negative"
            );
        }

        this.rewardPerUnit = rewardPerUnit;
        this.acceptedUnits = acceptedUnits;
    }

    public ActiveOfferId getId() {
        return id;
    }

    public ShopId getShopId() {
        return shopId;
    }

    public OfferId getOfferId() {
        return offerId;
    }

    public long getRewardPerUnit() {
        return rewardPerUnit;
    }

    public int getAcceptedUnits() {
        return acceptedUnits;
    }

    public int acceptUnits(int requestedUnits, long currentTick) {
        if (requestedUnits <= 0) {
            return 0;
        }

        int accepted = Math
                .min(requestedUnits, remainingUnitCapacity(currentTick));

        if (accepted <= 0) {
            return 0;
        }

        acceptedUnits += accepted;

        return accepted;
    }

    public abstract OfferKind kind();

    public abstract boolean isAccepting(long currentTick);

    public abstract int remainingUnitCapacity(long currentTick);
}
