package traderush.game.world;

import java.util.Optional;

public final class WorldStateMapper {
    private WorldStateMapper() {}

    public static WorldStateSnapshot toSnapshot(
            Optional<ManagementBlockLocation> managementBlock
    ) {
        Optional<WorldStateSnapshot.ManagementBlockSnapshot> blockSnapshot = managementBlock
                .map(
                        loc -> new WorldStateSnapshot.ManagementBlockSnapshot(
                                loc.dimensionId(),
                                loc.x(),
                                loc.y(),
                                loc.z()
                        )
                );

        return new WorldStateSnapshot(blockSnapshot);
    }

    public static Optional<ManagementBlockLocation> extractManagementBlock(
            WorldStateSnapshot snapshot
    ) {
        if (snapshot == null) {
            return Optional.empty();
        }

        return snapshot.managementBlock()
                .map(
                        b -> ManagementBlockLocation
                                .of(b.dimensionId(), b.x(), b.y(), b.z())
                );
    }
}
