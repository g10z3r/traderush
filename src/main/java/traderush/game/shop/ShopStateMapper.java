package traderush.game.shop;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShopStateMapper {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ShopStateMapper.class);

    private ShopStateMapper() {}

    public static ShopStateSnapshot toSnapshot(ShopRepository repository) {
        List<ShopStateSnapshot.OfferShopSnapshot> offerShops = new ArrayList<>();
        List<ShopStateSnapshot.ContractShopSnapshot> contractShops = new ArrayList<>();

        for (OfferShop shop : repository.getAllOfferShops()) {
            ShopLocation location = shop.getLocation();

            offerShops.add(
                    new ShopStateSnapshot.OfferShopSnapshot(
                            shop.getId().toString(),
                            shop.getName(),
                            location.dimensionId(),
                            location.x(),
                            location.y(),
                            location.z()
                    )
            );
        }

        for (ContractShop shop : repository.getAllContractShops()) {
            ShopLocation location = shop.getLocation();

            List<String> tags = shop.getTags()
                    .stream()
                    .map(ShopTag::getSerializedName)
                    .toList();

            contractShops.add(
                    new ShopStateSnapshot.ContractShopSnapshot(
                            shop.getId().toString(),
                            shop.getName(),
                            location.dimensionId(),
                            location.x(),
                            location.y(),
                            location.z(),
                            shop.getOwner().toString(),
                            tags
                    )
            );
        }

        return new ShopStateSnapshot(
                ShopStateSnapshot.CURRENT_VERSION,
                offerShops,
                contractShops
        );
    }

    public static void restoreInto(
            ShopRepository repository,
            ShopStateSnapshot snapshot
    ) {
        if (snapshot == null) {
            return;
        }

        for (ShopStateSnapshot.OfferShopSnapshot shopSnapshot : snapshot
                .offerShops()) {
            restoreOfferShop(repository, shopSnapshot);
        }

        for (ShopStateSnapshot.ContractShopSnapshot shopSnapshot : snapshot
                .contractShops()) {
            restoreContractShop(repository, shopSnapshot);
        }
    }

    private static void restoreOfferShop(
            ShopRepository repository,
            ShopStateSnapshot.OfferShopSnapshot shopSnapshot
    ) {
        if (shopSnapshot == null) {
            return;
        }

        ShopId shopId = parseShopId(shopSnapshot.id());

        if (shopId == null) {
            return;
        }

        ShopLocation location = parseLocation(
                shopSnapshot.dimensionId(),
                shopSnapshot.x(),
                shopSnapshot.y(),
                shopSnapshot.z(),
                shopSnapshot.id()
        );

        if (location == null) {
            return;
        }

        try {
            repository
                    .put(new OfferShop(shopId, shopSnapshot.name(), location));
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(
                    "Skipping invalid offer shop snapshot. Shop id: {}",
                    shopSnapshot.id(),
                    exception
            );
        }
    }

    private static void restoreContractShop(
            ShopRepository repository,
            ShopStateSnapshot.ContractShopSnapshot shopSnapshot
    ) {
        if (shopSnapshot == null) {
            return;
        }

        ShopId shopId = parseShopId(shopSnapshot.id());

        if (shopId == null) {
            return;
        }

        ShopLocation location = parseLocation(
                shopSnapshot.dimensionId(),
                shopSnapshot.x(),
                shopSnapshot.y(),
                shopSnapshot.z(),
                shopSnapshot.id()
        );

        if (location == null) {
            return;
        }

        Set<ShopTag> tags = new LinkedHashSet<>();
        ShopOwner owner = ShopOwner.fromString(shopSnapshot.owner());

        for (String rawTag : shopSnapshot.tags()) {
            ShopTag.fromSerializedName(rawTag)
                    .ifPresentOrElse(
                            tags::add,
                            () -> LOGGER.warn(
                                    "Skipping unknown contract shop tag '{}' in shop '{}'.",
                                    rawTag,
                                    shopSnapshot.name()
                            )
                    );
        }

        if (tags.isEmpty()) {
            LOGGER.warn(
                    "Skipping contract shop '{}' because it has no valid tags.",
                    shopSnapshot.name()
            );
            return;
        }

        try {
            repository.put(
                    new ContractShop(
                            shopId,
                            shopSnapshot.name(),
                            location,
                            owner,
                            tags
                    )
            );
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(
                    "Skipping invalid contract shop snapshot. Shop id: {}",
                    shopSnapshot.id(),
                    exception
            );
        }
    }

    private static ShopId parseShopId(String rawShopId) {
        if (rawShopId == null || rawShopId.isBlank()) {
            LOGGER.warn("Skipping shop snapshot with empty id.");
            return null;
        }

        try {
            return ShopId.fromString(rawShopId);
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(
                    "Skipping shop snapshot with invalid id: {}",
                    rawShopId
            );
            return null;
        }
    }

    private static ShopLocation parseLocation(
            String dimensionId,
            int x,
            int y,
            int z,
            String shopIdForLog
    ) {
        try {
            return new ShopLocation(dimensionId, x, y, z);
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(
                    "Skipping shop snapshot with invalid location. Shop id: {}",
                    shopIdForLog
            );
            return null;
        }
    }
}
