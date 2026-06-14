package traderush.game.shop.generation;

import java.util.Objects;

public abstract class ShopSpec {
    private final String generationKey;
    private final String displayName;
    private final ShopSpawnArea spawnArea;

    protected ShopSpec(
            String generationKey,
            String displayName,
            ShopSpawnArea spawnArea
    ) {
        if (generationKey == null || generationKey.isBlank()) {
            throw new IllegalArgumentException("shop generation key cannot be blank");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("shop display name cannot be blank");
        }

        this.generationKey = generationKey.trim();
        this.displayName = displayName.trim();
        this.spawnArea = Objects.requireNonNull(spawnArea, "shop spawn area cannot be null");
    }

    public String generationKey() {
        return generationKey;
    }

    public String displayName() {
        return displayName;
    }

    public ShopSpawnArea spawnArea() {
        return spawnArea;
    }
}
