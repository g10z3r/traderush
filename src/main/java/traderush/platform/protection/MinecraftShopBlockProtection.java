package traderush.platform.protection;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import traderush.game.shop.ShopLocation;
import traderush.game.shop.ShopProtectionSpec;
import traderush.game.shop.ShopService;

import java.util.function.Supplier;

public final class MinecraftShopBlockProtection {
    private static Supplier<ShopService> shopServiceSupplier = null;

    private MinecraftShopBlockProtection() {}

    public static void register(Supplier<ShopService> supplier) {
        shopServiceSupplier = supplier;
        ShopBlockBreakProtection.register(MinecraftShopBlockProtection::isProtected);
    }

    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        if (shopServiceSupplier == null) {
            return false;
        }

        ShopLocation location = ShopLocation.of(
                level.dimension().identifier().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );

        return ShopProtectionSpec.containsAnyShop(
                shopServiceSupplier.get().listShops(),
                location
        );
    }
}
