package traderush.game.sale;

import traderush.game.offer.ActiveOffer;
import traderush.game.offer.Offer;
import traderush.game.offer.OfferRepository;
import traderush.game.offer.OfferUnit;
import traderush.game.shop.ShopId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SaleCalculator {
    public SalePreview calculate(
            ShopId shopId,
            List<SaleItemStack> items,
            List<ActiveOffer> activeOffers,
            OfferRepository offerRepository,
            long currentTick
    ) {
        Objects.requireNonNull(shopId, "shop id cannot be null");
        Objects.requireNonNull(offerRepository, "offer repository cannot be null");

        SaleInventory inventory = SaleInventory.fromItems(items);
        List<SaleLine> lines = new ArrayList<>();

        if (activeOffers == null || activeOffers.isEmpty()) {
            return new SalePreview(shopId, List.of(), inventory.remainingItems(), 0);
        }

        for (ActiveOffer activeOffer : activeOffers) {
            if (activeOffer == null || !activeOffer.isAccepting(currentTick)) {
                continue;
            }

            Offer offer = offerRepository.getById(activeOffer.getOfferId())
                    .orElse(null);

            if (offer == null) {
                continue;
            }

            int remainingCapacity = activeOffer.remainingUnitCapacity(currentTick);

            if (remainingCapacity <= 0) {
                continue;
            }

            for (OfferUnit unit : offer.getUnits()) {
                if (remainingCapacity <= 0) {
                    break;
                }

                int availableUnits = inventory.maxUnitsFor(unit);
                int unitsToSell = Math.min(availableUnits, remainingCapacity);

                if (unitsToSell <= 0) {
                    continue;
                }

                List<traderush.game.offer.ItemRequirement> consumedItems = unit
                        .requirementsForUnits(unitsToSell);

                if (!inventory.consume(consumedItems)) {
                    continue;
                }

                long points = unitsToSell * activeOffer.getRewardPerUnit();

                lines.add(
                        new SaleLine(
                                activeOffer.getId(),
                                activeOffer.getOfferId(),
                                unitsToSell,
                                points,
                                consumedItems
                        )
                );

                remainingCapacity -= unitsToSell;
            }
        }

        long totalPoints = lines.stream()
                .mapToLong(SaleLine::points)
                .sum();

        return new SalePreview(
                shopId,
                lines,
                inventory.remainingItems(),
                totalPoints
        );
    }
}