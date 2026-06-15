package traderush.game.sale;

import java.util.Objects;
import traderush.game.item.ItemId;

public record SaleItemStack(ItemId itemId, int quantity) {
    public SaleItemStack {
        Objects.requireNonNull(itemId, "sale item id cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "sale item quantity must be positive"
            );
        }
    }

    public static SaleItemStack of(String itemId, int quantity) {
        return new SaleItemStack(ItemId.fromString(itemId), quantity);
    }
}
