package traderush.platform.protection;

import java.util.function.BiPredicate;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

final class ShopBlockBreakProtection {
    private ShopBlockBreakProtection() {}

    static void register(BiPredicate<ServerLevel, BlockPos> isProtected) {
        PlayerBlockBreakEvents.BEFORE.register(
                (world,
                        player,
                        pos,
                        state,
                        blockEntity) -> !isProtectedBlock(
                                world,
                                pos,
                                isProtected
                        )
        );
    }

    private static boolean isProtectedBlock(
            Level world,
            BlockPos pos,
            BiPredicate<ServerLevel, BlockPos> isProtected
    ) {
        if (!(world instanceof ServerLevel level)) {
            return false;
        }

        return isProtected.test(level, pos);
    }
}
