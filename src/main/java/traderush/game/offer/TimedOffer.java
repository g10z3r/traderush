package traderush.game.offer;

import java.util.List;

public final class TimedOffer extends Offer {
    private final int minAvailabilityDurationSeconds;
    private final int maxAvailabilityDurationSeconds;
    private final long completionThreshold;
    private final boolean repeatable;

    public TimedOffer(
            OfferId id,
            String name,
            String description,
            RewardRange rewardRange,
            List<OfferUnit> units,
            List<ActivationEventKey> activationEvents,
            int minAvailabilityDurationSeconds,
            int maxAvailabilityDurationSeconds,
            long completionThreshold,
            boolean repeatable
    ) {
        super(id, name, description, rewardRange, units, activationEvents);

        if (minAvailabilityDurationSeconds <= 0) {
            throw new IllegalArgumentException("minimum availability duration must be positive");
        }

        if (maxAvailabilityDurationSeconds < minAvailabilityDurationSeconds) {
            throw new IllegalArgumentException(
                    "maximum availability duration cannot be less than minimum duration"
            );
        }

        if (completionThreshold <= 0) {
            throw new IllegalArgumentException("timed offer completion threshold must be positive");
        }

        this.minAvailabilityDurationSeconds = minAvailabilityDurationSeconds;
        this.maxAvailabilityDurationSeconds = maxAvailabilityDurationSeconds;
        this.completionThreshold = completionThreshold;
        this.repeatable = repeatable;
    }

    @Override
    public OfferKind getKind() {
        return OfferKind.TIMED;
    }

    public int getMinAvailabilityDurationSeconds() {
        return minAvailabilityDurationSeconds;
    }

    public int getMaxAvailabilityDurationSeconds() {
        return maxAvailabilityDurationSeconds;
    }

    public long getCompletionThreshold() {
        return completionThreshold;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public boolean allowsDurationSeconds(int durationSeconds) {
        return durationSeconds >= minAvailabilityDurationSeconds
                && durationSeconds <= maxAvailabilityDurationSeconds;
    }
}
