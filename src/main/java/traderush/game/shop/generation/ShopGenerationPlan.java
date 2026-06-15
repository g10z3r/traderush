package traderush.game.shop.generation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ShopGenerationPlan {
    private final List<ShopSpec> shops;

    public ShopGenerationPlan(List<ShopSpec> shops) {
        if (shops == null || shops.isEmpty()) {
            throw new IllegalArgumentException(
                    "shop generation plan must contain at least one shop"
            );
        }

        this.shops = List.copyOf(shops);

        validateGenerationKeys(this.shops);
    }

    public List<ShopSpec> shops() {
        return shops;
    }

    public static ShopGenerationPlan of(ShopSpec first, ShopSpec... rest) {
        List<ShopSpec> all = new ArrayList<>();
        all.add(first);

        if (rest != null) {
            all.addAll(List.of(rest));
        }

        return new ShopGenerationPlan(all);
    }

    private static void validateGenerationKeys(List<ShopSpec> shops) {
        Set<String> seen = new HashSet<>();

        for (ShopSpec spec : shops) {
            if (spec == null) {
                throw new IllegalArgumentException(
                        "shop generation plan cannot contain null specs"
                );
            }

            if (!seen.add(spec.generationKey())) {
                throw new IllegalArgumentException(
                        "duplicate shop generation key: " + spec.generationKey()
                );
            }
        }
    }
}
