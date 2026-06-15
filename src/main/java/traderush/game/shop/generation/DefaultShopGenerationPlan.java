package traderush.game.shop.generation;

import java.util.List;

/**
 * Defines the shops that should be created when a new world is first started.
 * Applied once — if saved shop state already exists, generation is skipped.
 *
 * <p>
 * Current plan: 1 offer shop within 100 blocks of spawn.
 *
 * <p>
 * Future plan (when ready): - 1 to 4 offer shops, each with its own spawn area
 * rule - 2 to 6 contract shops, each with its own spawn area rule and tags
 *
 * <p>
 * To add more shops: add extra OfferShopSpec / ContractShopSpec entries here.
 * Old worlds will NOT receive new shops — their saved state already exists.
 */
public final class DefaultShopGenerationPlan {
    private static final String OVERWORLD = "minecraft:overworld";
    private static final int MIN_DISTANCE_FROM_SPAWN = 5;
    private static final int MAX_DISTANCE_FROM_SPAWN = 10;
    private static final int MAX_PLACEMENT_ATTEMPTS = 8;

    private DefaultShopGenerationPlan() {}

    public static ShopGenerationPlan create() {
        return new ShopGenerationPlan(
                List.of(
                        new OfferShopSpec(
                                "offer_shop_1",
                                "Market",
                                ShopSpawnArea.aroundSpawn(
                                        OVERWORLD,
                                        MIN_DISTANCE_FROM_SPAWN,
                                        MAX_DISTANCE_FROM_SPAWN,
                                        MAX_PLACEMENT_ATTEMPTS
                                )
                        )
                )
        );
    }
}
