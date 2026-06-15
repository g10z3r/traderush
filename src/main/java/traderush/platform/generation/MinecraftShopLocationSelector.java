package traderush.platform.generation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traderush.game.shop.ShopLocation;
import traderush.game.shop.generation.ShopLocationSelector;
import traderush.game.shop.generation.ShopSpec;
import traderush.game.shop.generation.ShopSpawnArea;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Finds a surface position within the spec's spawn area.
 *
 * Tries {@code maxPlacementAttempts} evenly-spaced angles around the world
 * spawn at the midpoint distance, returning the first candidate not already
 * reserved by another shop.
 *
 * A candidate is accepted only when:
 * - The chunk is fully generated (force-loaded before height query)
 * - The surface height is above the sea level (no deep ravines or underwater)
 * - The surface block is solid and contains no fluid (water, lava, etc.)
 */
public final class MinecraftShopLocationSelector implements ShopLocationSelector {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(MinecraftShopLocationSelector.class);

    private final MinecraftServer server;

    public MinecraftShopLocationSelector(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Optional<ShopLocation> selectLocation(
            ShopSpec spec,
            Set<ShopLocation> reservedLocations
    ) {
        ShopSpawnArea area = spec.spawnArea();
        ServerLevel level = getLevel(area.dimensionId());

        if (level == null) {
            LOGGER.warn("Dimension '{}' not found for shop '{}'.", area.dimensionId(), spec.generationKey());
            return Optional.empty();
        }

        BlockPos spawn = level.getLevelData().getRespawnData().pos();
        int midDistance = (area.minDistanceFromSpawn() + area.maxDistanceFromSpawn()) / 2;
        int attempts = area.maxPlacementAttempts();

        LOGGER.debug(
                "Searching for '{}' location around spawn {} with mid-distance={}.",
                spec.generationKey(),
                spawn,
                midDistance
        );

        for (int i = 0; i < attempts; i++) {
            double angle = i * (2.0 * Math.PI / attempts);
            int dx = (int) (midDistance * Math.cos(angle));
            int dz = (int) (midDistance * Math.sin(angle));
            int x = spawn.getX() + dx;
            int z = spawn.getZ() + dz;

            OptionalInt y = findSolidSurfaceY(level, x, z);

            if (y.isEmpty()) {
                LOGGER.debug(
                        "  Attempt {}: ({}, ?, {}) rejected — no solid surface.",
                        i + 1, x, z
                );
                continue;
            }

            ShopLocation candidate = new ShopLocation(area.dimensionId(), x, y.getAsInt(), z);

            if (reservedLocations.contains(candidate)) {
                LOGGER.debug(
                        "  Attempt {}: {} rejected — location already reserved.",
                        i + 1, candidate.toBlockPosString()
                );
                continue;
            }

            LOGGER.debug("  Attempt {}: {} accepted.", i + 1, candidate.toBlockPosString());
            return Optional.of(candidate);
        }

        return Optional.empty();
    }

    /**
     * Force-loads the chunk, then returns the Y of the first solid, non-fluid
     * surface block at (x, z) that is at or above sea level.
     * Returns empty if the surface is liquid, underground, or not yet generated.
     */
    private OptionalInt findSolidSurfaceY(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        if (y <= level.getMinY() + 1) {
            return OptionalInt.empty();
        }

        if (y <= level.getSeaLevel()) {
            return OptionalInt.empty();
        }

        BlockState surface = level.getBlockState(new BlockPos(x, y - 1, z));

        if (!surface.getFluidState().isEmpty()) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(y);
    }

    private ServerLevel getLevel(String dimensionId) {
        Identifier id = Identifier.parse(dimensionId);
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, id);
        return server.getLevel(key);
    }
}
