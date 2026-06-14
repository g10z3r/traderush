package traderush.platform.protection;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import traderush.game.shop.ShopLocation;
import traderush.game.shop.ShopService;

import java.util.function.Supplier;

public final class MinecraftShopBlockProtection {
    private MinecraftShopBlockProtection() {}

    public static void register(Supplier<ShopService> shopServiceSupplier) {
        PlayerBlockBreakEvents.BEFORE.register(
                (world, player, pos, state, blockEntity) -> !isProtectedShopBlock(
                        world,
                        pos,
                        shopServiceSupplier
                )
        );
    }

    private static boolean isProtectedShopBlock(
            Level world,
            BlockPos pos,
            Supplier<ShopService> shopServiceSupplier
    ) {
        if (!(world instanceof ServerLevel level)) {
            return false;
        }

        ShopLocation location = ShopLocation.of(
                level.dimension().identifier().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );

        return shopServiceSupplier.get().isShopLocation(location);
    }
}
