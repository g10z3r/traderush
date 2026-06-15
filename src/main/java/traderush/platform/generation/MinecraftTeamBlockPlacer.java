package traderush.platform.generation;

import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traderush.game.world.ManagementBlockLocation;
import traderush.platform.protection.ShopProtectionBypass;
import traderush.platform.registry.TradeRushBlocks;

/**
 * Places the team management block 5 blocks south of the world spawn. Runs only
 * once — on new world creation.
 */
public final class MinecraftTeamBlockPlacer {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MinecraftTeamBlockPlacer.class);
    private static final int DISTANCE_FROM_SPAWN = 5;

    private final MinecraftServer server;

    public MinecraftTeamBlockPlacer(MinecraftServer server) {
        this.server = server;
    }

    public ManagementBlockLocation place() {
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getLevelData().getRespawnData().pos();

        int x = spawn.getX();
        int z = spawn.getZ() + DISTANCE_FROM_SPAWN;

        OptionalInt groundY = SurfaceUtils.findGroundY(overworld, x, z);

        if (groundY.isEmpty()) {
            LOGGER.warn(
                    "Could not find valid ground surface at ({}, ?, {}); "
                            + "falling back to raw heightmap.",
                    x,
                    z
            );
        }

        int y = groundY.isPresent()
                ? groundY.getAsInt()
                : overworld.getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        x,
                        z
                );

        BlockPos pos = new BlockPos(x, y, z);

        ShopProtectionBypass.run(
                () -> overworld.setBlock(
                        pos,
                        TradeRushBlocks.TEAM_MANAGEMENT_BLOCK
                                .defaultBlockState(),
                        3
                )
        );

        String dimensionId = overworld.dimension().identifier().toString();
        ManagementBlockLocation location = ManagementBlockLocation
                .of(dimensionId, x, y, z);

        LOGGER.info("Placed team management block at {}.", pos);

        return location;
    }
}
