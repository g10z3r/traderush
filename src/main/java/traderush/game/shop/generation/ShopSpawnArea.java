package traderush.game.shop.generation;

public record ShopSpawnArea(
        String dimensionId,
        int minDistanceFromSpawn,
        int maxDistanceFromSpawn,
        int maxPlacementAttempts
) {
    public ShopSpawnArea {
        if (dimensionId == null || dimensionId.isBlank()) {
            throw new IllegalArgumentException(
                    "shop spawn area dimension id cannot be blank"
            );
        }

        if (minDistanceFromSpawn < 0) {
            throw new IllegalArgumentException(
                    "minimum distance from spawn cannot be negative"
            );
        }

        if (maxDistanceFromSpawn < minDistanceFromSpawn) {
            throw new IllegalArgumentException(
                    "maximum distance from spawn cannot be less than minimum distance"
            );
        }

        if (maxPlacementAttempts <= 0) {
            throw new IllegalArgumentException(
                    "maximum placement attempts must be positive"
            );
        }

        dimensionId = dimensionId.trim();
    }

    public static ShopSpawnArea aroundSpawn(
            String dimensionId,
            int minDistanceFromSpawn,
            int maxDistanceFromSpawn,
            int maxPlacementAttempts
    ) {
        return new ShopSpawnArea(
                dimensionId,
                minDistanceFromSpawn,
                maxDistanceFromSpawn,
                maxPlacementAttempts
        );
    }
}
