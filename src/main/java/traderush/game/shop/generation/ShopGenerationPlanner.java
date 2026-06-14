package traderush.game.shop.generation;

import traderush.game.shop.ShopLocation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Pure-domain planner. Given a generation plan and a location selector,
 * produces a list of placements (spec + chosen location) with no duplicates.
 * Does not touch Minecraft APIs or create shop objects.
 */
public final class ShopGenerationPlanner {
    private final ShopLocationSelector locationSelector;

    public ShopGenerationPlanner(ShopLocationSelector locationSelector) {
        this.locationSelector = Objects.requireNonNull(
                locationSelector,
                "shop location selector cannot be null"
        );
    }

    public List<ShopGenerationPlacement> planPlacements(ShopGenerationPlan plan) {
        Objects.requireNonNull(plan, "shop generation plan cannot be null");

        List<ShopGenerationPlacement> placements = new ArrayList<>();
        Set<ShopLocation> reservedLocations = new LinkedHashSet<>();

        for (ShopSpec spec : plan.shops()) {
            ShopLocation location = locationSelector
                    .selectLocation(spec, Set.copyOf(reservedLocations))
                    .orElseThrow(() -> new ShopGenerationException(
                            "Failed to find a location for shop '" + spec.generationKey() + "'"
                    ));

            if (!reservedLocations.add(location)) {
                throw new ShopGenerationException(
                        "Location selector returned a duplicate location for shop '"
                                + spec.generationKey() + "'"
                );
            }

            placements.add(new ShopGenerationPlacement(spec, location));
        }

        return List.copyOf(placements);
    }
}
