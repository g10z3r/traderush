package traderush.game.world;

import java.util.Optional;

/**
 * Snapshot of world-level setup state — structures and blocks placed in the
 * world during initial game setup. This is independent of game-mode-specific
 * state (teams, shops, etc.).
 */
public record WorldStateSnapshot(
        Optional<ManagementBlockSnapshot> managementBlock
) {
    public WorldStateSnapshot {
        managementBlock = managementBlock == null ? Optional.empty()
                : managementBlock;
    }

    public static WorldStateSnapshot empty() {
        return new WorldStateSnapshot(Optional.empty());
    }

    public record ManagementBlockSnapshot(
            String dimensionId,
            int x,
            int y,
            int z
    ) {
        public ManagementBlockSnapshot {
            if (dimensionId == null || dimensionId.isBlank()) {
                throw new IllegalArgumentException(
                        "Dimension ID cannot be null or empty"
                );
            }

            dimensionId = dimensionId.trim();
        }
    }
}
