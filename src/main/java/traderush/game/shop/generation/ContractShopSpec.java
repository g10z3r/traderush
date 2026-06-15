package traderush.game.shop.generation;

import traderush.game.shop.ShopTag;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class ContractShopSpec extends ShopSpec {
    private final Set<ShopTag> tags;

    public ContractShopSpec(
            String generationKey,
            String displayName,
            ShopSpawnArea spawnArea,
            Set<ShopTag> tags
    ) {
        super(generationKey, displayName, spawnArea);

        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("contract shop spec must have at least one tag");
        }

        Set<ShopTag> copy = new LinkedHashSet<>();

        for (ShopTag tag : tags) {
            copy.add(Objects.requireNonNull(tag, "contract shop tag cannot be null"));
        }

        this.tags = Collections.unmodifiableSet(copy);
    }

    public Set<ShopTag> tags() {
        return tags;
    }
}
