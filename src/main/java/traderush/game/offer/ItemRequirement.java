package traderush.game.offer;

import traderush.game.item.ItemId;

import java.util.Objects;

public record ItemRequirement(
        ItemId itemId,
        int quantity
) {
    public ItemRequirement {
        Objects.requireNonNull(itemId, "item id cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("item requirement quantity must be positive");
        }
    }

    public static ItemRequirement of(String itemId, int quantity) {
        return new ItemRequirement(ItemId.fromString(itemId), quantity);
    }

    public ItemRequirement multiply(int multiplier) {
        if (multiplier <= 0) {
            throw new IllegalArgumentException("item requirement multiplier must be positive");
        }

        return new ItemRequirement(itemId, quantity * multiplier);
    }
}