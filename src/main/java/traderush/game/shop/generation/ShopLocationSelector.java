package traderush.game.shop.generation;

import traderush.game.shop.ShopLocation;

import java.util.Optional;
import java.util.Set;

/**
 * Selects a concrete world location for a shop spec.
 * Implemented in the platform layer (MinecraftShopLocationSelector).
 */
public interface ShopLocationSelector {
    Optional<ShopLocation> selectLocation(
            ShopSpec spec,
            Set<ShopLocation> reservedLocations
    );
}
