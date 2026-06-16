package traderush.platform.protection;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import traderush.game.world.ManagementBlockLocation;

public final class MinecraftTeamBlockProtection {
    private static Supplier<Optional<ManagementBlockLocation>> locationSupplier = null;

    private MinecraftTeamBlockProtection() {}

    public static void register(
            Supplier<Optional<ManagementBlockLocation>> supplier
    ) {
        locationSupplier = supplier;
    }

    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        if (locationSupplier == null) {
            return false;
        }

        Optional<ManagementBlockLocation> location = locationSupplier.get();

        if (location.isEmpty()) {
            return false;
        }

        ManagementBlockLocation loc = location.get();
        String dimId = level.dimension().identifier().toString();

        return loc.dimensionId().equals(dimId)
                && loc.x() == pos.getX()
                && loc.y() == pos.getY()
                && loc.z() == pos.getZ();
    }
}
