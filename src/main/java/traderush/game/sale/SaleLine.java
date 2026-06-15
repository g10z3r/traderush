package traderush.game.sale;

import traderush.game.offer.ActiveOfferId;
import traderush.game.offer.ItemRequirement;
import traderush.game.offer.OfferId;

import java.util.List;
import java.util.Objects;

public record SaleLine(
        ActiveOfferId activeOfferId,
        OfferId offerId,
        int units,
        long points,
        List<ItemRequirement> consumedItems
) {
    public SaleLine {
        Objects.requireNonNull(activeOfferId, "active offer id cannot be null");
        Objects.requireNonNull(offerId, "offer id cannot be null");

        if (units <= 0) {
            throw new IllegalArgumentException("sale line units must be positive");
        }

        if (points <= 0) {
            throw new IllegalArgumentException("sale line points must be positive");
        }

        if (consumedItems == null || consumedItems.isEmpty()) {
            throw new IllegalArgumentException("sale line must contain consumed items");
        }

        consumedItems = List.copyOf(consumedItems);
    }
}