package traderush.game.shop.generation;

import traderush.game.shop.ShopLocation;

import java.util.Objects;

/**
 * The result of planning: a specific spec paired with the location where it should be placed.
 */
public record ShopGenerationPlacement(
        ShopSpec spec,
        ShopLocation location
) {
    public ShopGenerationPlacement {
        Objects.requireNonNull(spec, "shop spec cannot be null");
        Objects.requireNonNull(location, "shop generation location cannot be null");
    }
}
