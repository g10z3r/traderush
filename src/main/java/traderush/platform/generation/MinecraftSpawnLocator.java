package traderush.platform.generation;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecraftSpawnLocator {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MinecraftSpawnLocator.class);

    private static final int MAX_SCAN_RADIUS = 200;
    private static final int SCAN_STEP = 16;
    private static final int FLAT_AREA_SIZE = 20;
    private static final int FLAT_AREA_HALF = FLAT_AREA_SIZE / 2;
    private static final int FLATNESS_SAMPLE_STEP = 2;
    private static final int MAX_HEIGHT_VARIANCE = 2;

    public void findAndSetSpawn(ServerLevel overworld) {
        BlockPos currentSpawn = overworld.getLevelData().getRespawnData().pos();

        Optional<BlockPos> flatSpawn = findFlatSpawn(overworld, currentSpawn);

        if (flatSpawn.isPresent()) {
            BlockPos newSpawn = flatSpawn.get();
            ResourceKey<Level> dimension = overworld.dimension();
            overworld.setRespawnData(
                    LevelData.RespawnData.of(dimension, newSpawn, 0.0f, 0.0f)
            );
            LOGGER.info(
                    "Spawn set to flat area at {} (was {}).",
                    newSpawn,
                    currentSpawn
            );
        } else {
            LOGGER.warn(
                    "No flat area found within {} blocks, keeping default spawn at {}.",
                    MAX_SCAN_RADIUS,
                    currentSpawn
            );
        }
    }

    private Optional<BlockPos> findFlatSpawn(
            ServerLevel level,
            BlockPos origin
    ) {
        int ox = origin.getX();
        int oz = origin.getZ();

        if (isFlatEnough(level, ox, oz)) {
            return Optional.of(getSurface(level, ox, oz));
        }

        for (int r = SCAN_STEP; r <= MAX_SCAN_RADIUS; r += SCAN_STEP) {
            for (int i = -r; i <= r; i += SCAN_STEP) {
                if (isFlatEnough(level, ox + i, oz - r)) {
                    return Optional.of(getSurface(level, ox + i, oz - r));
                }
                if (isFlatEnough(level, ox + i, oz + r)) {
                    return Optional.of(getSurface(level, ox + i, oz + r));
                }
            }
            for (int i = -r + SCAN_STEP; i < r; i += SCAN_STEP) {
                if (isFlatEnough(level, ox - r, oz + i)) {
                    return Optional.of(getSurface(level, ox - r, oz + i));
                }
                if (isFlatEnough(level, ox + r, oz + i)) {
                    return Optional.of(getSurface(level, ox + r, oz + i));
                }
            }
        }

        return Optional.empty();
    }

    private boolean isFlatEnough(ServerLevel level, int centerX, int centerZ) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int dx = -FLAT_AREA_HALF; dx < FLAT_AREA_HALF; dx += FLATNESS_SAMPLE_STEP) {
            for (int dz = -FLAT_AREA_HALF; dz < FLAT_AREA_HALF; dz += FLATNESS_SAMPLE_STEP) {
                int x = centerX + dx;
                int z = centerZ + dz;

                level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);

                int h = level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        x,
                        z
                );

                if (h <= level.getMinY() + 1 || h <= level.getSeaLevel()) {
                    return false;
                }

                BlockState surface = level
                        .getBlockState(new BlockPos(x, h - 1, z));
                if (!surface.getFluidState().isEmpty()) {
                    return false;
                }

                if (h < min) {
                    min = h;
                }
                if (h > max) {
                    max = h;
                }
            }
        }

        return max - min <= MAX_HEIGHT_VARIANCE;
    }

    private BlockPos getSurface(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);
        int y = level
                .getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }
}
