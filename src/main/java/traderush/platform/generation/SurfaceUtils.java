package traderush.platform.generation;

import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

final class SurfaceUtils {
    private SurfaceUtils() {}

    /**
     * Returns the Y at which a player would stand — one block above the topmost
     * solid, non-tree, non-flooded ground block at (x, z).
     *
     * <p>
     * Scans downward from the motion-blocking (no-leaves) height so that tree
     * trunks and bamboo are skipped and the real terrain surface is found.
     *
     * @return the spawn Y (i.e. block just above the ground), or empty when the
     *         surface is liquid, entirely below sea level, or has no valid
     *         block.
     */
    static OptionalInt findGroundY(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);

        int top = level
                .getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;

        for (int y = top; y >= level.getMinY(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);

            if (!isValidGroundBlock(level, pos, state)) {
                continue;
            }

            int spawnY = y + 1;

            if (spawnY <= level.getSeaLevel()) {
                return OptionalInt.empty();
            }

            return OptionalInt.of(spawnY);
        }

        return OptionalInt.empty();
    }

    /**
     * A block qualifies as valid ground when it is solid, not flooded, and not
     * a tree or bamboo component that would place the player above the terrain.
     */
    static boolean isValidGroundBlock(
            ServerLevel level,
            BlockPos pos,
            BlockState state
    ) {
        if (!state.isFaceSturdy(level, pos, Direction.UP)) {
            return false;
        }

        if (!state.getFluidState().isEmpty()) {
            return false;
        }

        if (state.is(BlockTags.LOGS)) {
            return false;
        }

        if (state.is(BlockTags.BAMBOO_BLOCKS)) {
            return false;
        }

        return true;
    }
}
