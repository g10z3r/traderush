package traderush.game.shop;

import java.util.Collection;
import java.util.Objects;

/**
 * Defines the indestructible area around a shop.
 */
public final class ShopProtectionSpec {
    public static final int AREA_SIZE = 5;
    public static final int HALF_SIZE = AREA_SIZE / 2;

    private ShopProtectionSpec() {}

    public static boolean contains(ShopLocation shopCenter, ShopLocation block) {
        Objects.requireNonNull(shopCenter, "shop center cannot be null");
        Objects.requireNonNull(block, "block location cannot be null");

        if (!shopCenter.dimensionId().equals(block.dimensionId())) {
            return false;
        }

        int dx = Math.abs(block.x() - shopCenter.x());
        int dz = Math.abs(block.z() - shopCenter.z());

        return dx <= HALF_SIZE && dz <= HALF_SIZE;
    }

    public static boolean containsAnyShop(Collection<Shop> shops, ShopLocation block) {
        Objects.requireNonNull(shops, "shops cannot be null");
        Objects.requireNonNull(block, "block location cannot be null");

        for (Shop shop : shops) {
            if (contains(shop.getLocation(), block)) {
                return true;
            }
        }

        return false;
    }
}
