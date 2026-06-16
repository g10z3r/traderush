package traderush.game.offer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import traderush.game.shop.ShopId;

public final class OfferService {
    private static final int TICKS_PER_SECOND = 20;

    private final OfferRepository offerRepository;
    private final ActiveOfferRepository activeOfferRepository;
    private final Runnable onStateChanged;

    public OfferService(
            OfferRepository offerRepository,
            ActiveOfferRepository activeOfferRepository,
            Runnable onStateChanged
    ) {
        this.offerRepository = Objects
                .requireNonNull(
                        offerRepository,
                        "offer repository cannot be null"
                );
        this.activeOfferRepository = Objects
                .requireNonNull(
                        activeOfferRepository,
                        "active offer repository cannot be null"
                );
        this.onStateChanged = Objects
                .requireNonNull(
                        onStateChanged,
                        "state change callback cannot be null"
                );
    }

    public Offer registerOffer(Offer offer) {
        return offerRepository.put(offer);
    }

    public OfferOperationResult<TimedActiveOffer> activateTimedOffer(
            ShopId shopId,
            OfferId offerId,
            long rewardPerUnit,
            long currentTick,
            int durationSeconds
    ) {
        return offerRepository.getById(offerId).map(offer -> {
            if (!(offer instanceof TimedOffer timedOffer)) {
                return OfferOperationResult
                        .<TimedActiveOffer>failure(OfferError.WRONG_OFFER_TYPE);
            }

            if (!offer.getRewardRange().contains(rewardPerUnit)) {
                return OfferOperationResult
                        .<TimedActiveOffer>failure(
                                OfferError.REWARD_OUT_OF_RANGE
                        );
            }

            if (!timedOffer.allowsDurationSeconds(durationSeconds)) {
                return OfferOperationResult
                        .<TimedActiveOffer>failure(
                                OfferError.DURATION_OUT_OF_RANGE
                        );
            }

            TimedActiveOffer activeOffer = new TimedActiveOffer(
                    ActiveOfferId.newId(),
                    shopId,
                    offer.getId(),
                    rewardPerUnit,
                    0,
                    currentTick,
                    currentTick + ((long) durationSeconds * TICKS_PER_SECOND),
                    timedOffer.getCompletionThreshold(),
                    timedOffer.isRepeatable()
            );

            activeOfferRepository.put(activeOffer);
            onStateChanged.run();

            return OfferOperationResult.success(activeOffer);
        })
                .orElseGet(
                        () -> OfferOperationResult
                                .failure(OfferError.OFFER_NOT_FOUND)
                );
    }

    public OfferOperationResult<LimitedActiveOffer> activateLimitedOffer(
            ShopId shopId,
            OfferId offerId,
            long rewardPerUnit
    ) {
        return offerRepository.getById(offerId).map(offer -> {
            if (!(offer instanceof LimitedOffer limitedOffer)) {
                return OfferOperationResult
                        .<LimitedActiveOffer>failure(
                                OfferError.WRONG_OFFER_TYPE
                        );
            }

            if (!offer.getRewardRange().contains(rewardPerUnit)) {
                return OfferOperationResult
                        .<LimitedActiveOffer>failure(
                                OfferError.REWARD_OUT_OF_RANGE
                        );
            }

            LimitedActiveOffer activeOffer = new LimitedActiveOffer(
                    ActiveOfferId.newId(),
                    shopId,
                    offer.getId(),
                    rewardPerUnit,
                    0,
                    limitedOffer.getMaxAcceptedUnits()
            );

            activeOfferRepository.put(activeOffer);
            onStateChanged.run();

            return OfferOperationResult.success(activeOffer);
        })
                .orElseGet(
                        () -> OfferOperationResult
                                .failure(OfferError.OFFER_NOT_FOUND)
                );
    }

    public List<ActiveOffer> activeOffersForShop(
            ShopId shopId,
            long currentTick
    ) {
        return activeOfferRepository.getAllByShopId(shopId)
                .stream()
                .filter(activeOffer -> activeOffer.isAccepting(currentTick))
                .toList();
    }

    public Optional<ActiveOffer> findAcceptingActiveOffer(
            ShopId shopId,
            OfferId offerId,
            long currentTick
    ) {
        return activeOfferRepository.getAllByShopId(shopId)
                .stream()
                .filter(activeOffer -> activeOffer.getOfferId().equals(offerId))
                .filter(activeOffer -> activeOffer.isAccepting(currentTick))
                .findFirst();
    }

    /**
     * Returns an accepting active offer for the shop, creating one with a
     * random reward if none exists yet.
     */
    public OfferOperationResult<ActiveOffer> ensureActiveOffer(
            ShopId shopId,
            OfferId offerId,
            long currentTick
    ) {
        Optional<ActiveOffer> existing = findAcceptingActiveOffer(
                shopId,
                offerId,
                currentTick
        );

        if (existing.isPresent()) {
            return OfferOperationResult.success(existing.get());
        }

        return offerRepository.getById(offerId)
                .<OfferOperationResult<ActiveOffer>>map(offer -> {
                    long reward = randomRewardInRange(offer.getRewardRange());

                    if (offer instanceof TimedOffer timedOffer) {
                        int duration = randomDurationSeconds(timedOffer);
                        OfferOperationResult<TimedActiveOffer> result = activateTimedOffer(
                                shopId,
                                offerId,
                                reward,
                                currentTick,
                                duration
                        );

                        if (result.isFailure()) {
                            return OfferOperationResult
                                    .<ActiveOffer>failure(result.error());
                        }

                        return OfferOperationResult
                                .<ActiveOffer>success(result.value());
                    }

                    if (offer instanceof LimitedOffer) {
                        OfferOperationResult<LimitedActiveOffer> result = activateLimitedOffer(
                                shopId,
                                offerId,
                                reward
                        );

                        if (result.isFailure()) {
                            return OfferOperationResult
                                    .<ActiveOffer>failure(result.error());
                        }

                        return OfferOperationResult
                                .<ActiveOffer>success(result.value());
                    }

                    return OfferOperationResult
                            .<ActiveOffer>failure(OfferError.WRONG_OFFER_TYPE);
                })
                .orElseGet(
                        () -> OfferOperationResult
                                .failure(OfferError.OFFER_NOT_FOUND)
                );
    }

    private static long randomRewardInRange(RewardRange range) {
        int min = range.minReward();
        int max = range.maxReward();

        if (min >= max) {
            return min;
        }

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static int randomDurationSeconds(TimedOffer timedOffer) {
        int min = timedOffer.getMinAvailabilityDurationSeconds();
        int max = timedOffer.getMaxAvailabilityDurationSeconds();

        if (min >= max) {
            return min;
        }

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
