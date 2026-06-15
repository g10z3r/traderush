package traderush.game.shop;

import java.util.List;

public record ShopStateSnapshot(
        int version,
        List<OfferShopSnapshot> offerShops,
        List<ContractShopSnapshot> contractShops
) {
    public static final int CURRENT_VERSION = 1;

    public ShopStateSnapshot {
        offerShops = offerShops == null ? List.of() : List.copyOf(offerShops);
        contractShops = contractShops == null ? List.of()
                : List.copyOf(contractShops);
    }

    public static ShopStateSnapshot empty() {
        return new ShopStateSnapshot(CURRENT_VERSION, List.of(), List.of());
    }

    public record OfferShopSnapshot(
            String id,
            String name,
            String dimensionId,
            int x,
            int y,
            int z
    ) {
    }

    public record ContractShopSnapshot(
            String id,
            String name,
            String dimensionId,
            int x,
            int y,
            int z,
            String owner,
            List<String> tags
    ) {
        public ContractShopSnapshot {
            tags = tags == null ? List.of() : List.copyOf(tags);
        }
    }
}
