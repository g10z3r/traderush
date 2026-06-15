package traderush.game.world;

public record ManagementBlockLocation(String dimensionId, int x, int y, int z) {
    public ManagementBlockLocation {
        if (dimensionId == null || dimensionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Dimension ID cannot be null or empty"
            );
        }

        dimensionId = dimensionId.trim();
    }

    public static ManagementBlockLocation of(
            String dimensionId,
            int x,
            int y,
            int z
    ) {
        return new ManagementBlockLocation(dimensionId, x, y, z);
    }
}
