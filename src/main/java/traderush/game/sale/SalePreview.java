package traderush.game.sale;

import traderush.game.shop.ShopId;

import java.util.List;
import java.util.Objects;

public record SalePreview(
        ShopId shopId,
        List<SaleLine> lines,
        List<SaleItemStack> remainingItems,
        long totalPoints
) {
    public SalePreview {
        Objects.requireNonNull(shopId, "shop id cannot be null");

        lines = lines == null ? List.of() : List.copyOf(lines);
        remainingItems = remainingItems == null ? List.of() : List.copyOf(remainingItems);

        if (totalPoints < 0) {
            throw new IllegalArgumentException("sale preview total points cannot be negative");
        }
    }

    public boolean hasSellableItems() {
        return !lines.isEmpty() && totalPoints > 0;
    }
}