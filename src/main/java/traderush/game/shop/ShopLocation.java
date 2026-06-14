package traderush.game.shop;

public record ShopLocation(
        String dimensionId,
        int x,
        int y,
        int z
) {
    public ShopLocation {
        if (dimensionId == null || dimensionId.isEmpty()) {
            throw new IllegalArgumentException("Dimension ID cannot be null or empty");
        }

        if (x < 0) {
            throw new IllegalArgumentException("X cannot be negative");
        }

        if (y < 0) {
            throw new IllegalArgumentException("Y cannot be negative");
        }

        if (z < 0) {
            throw new IllegalArgumentException("Z cannot be negative");
        }

        dimensionId = dimensionId.trim();
    }

    public static ShopLocation of(String dimensionId, int x, int y, int z) {
        return new ShopLocation(dimensionId, x, y, z);
    }

    public String toBlockPosString() {
        return String.format("%s:%d,%d,%d", dimensionId, x, y, z);
    }
}
