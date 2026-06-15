package traderush.game.offer;

import java.util.List;
import java.util.Objects;

public abstract class Offer {
    private final OfferId id;
    private final String name;
    private final String description;
    private final RewardRange rewardRange;
    private final List<OfferUnit> units;
    private final List<ActivationEventKey> activationEvents;

    protected Offer(
            OfferId id,
            String name,
            String description,
            RewardRange rewardRange,
            List<OfferUnit> units,
            List<ActivationEventKey> activationEvents
    ) {
        this.id = Objects.requireNonNull(id, "offer id cannot be null");
        this.name = Objects.requireNonNull(name, "offer name cannot be null");
        this.description = Objects.requireNonNull(description, "offer description cannot be null");
        this.rewardRange = Objects.requireNonNull(rewardRange, "reward range cannot be null");
        this.units = Objects.requireNonNull(List.copyOf(units), "units cannot be null");
        this.activationEvents = Objects
                .requireNonNull(List.copyOf(activationEvents), "activation events cannot be null");
    }

    public OfferId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RewardRange getRewardRange() {
        return rewardRange;
    }

    public List<OfferUnit> getUnits() {
        return units;
    }

    public List<ActivationEventKey> getActivationEvents() {
        return activationEvents;
    }

    public abstract OfferKind getKind();
}
