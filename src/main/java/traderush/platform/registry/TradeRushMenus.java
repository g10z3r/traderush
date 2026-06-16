package traderush.platform.registry;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import traderush.TradeRush;
import traderush.platform.ui.shop.ShopOffersMenu;
import traderush.platform.ui.team.TeamManagementMenu;

public final class TradeRushMenus {
    public static final ExtendedMenuType<TeamManagementMenu, BlockPos> TEAM_MANAGEMENT = Registry
            .register(
                    BuiltInRegistries.MENU,
                    TradeRush.id("team_management"),
                    new ExtendedMenuType<>(
                            TeamManagementMenu::new,
                            BlockPos.STREAM_CODEC.cast()
                    )
            );

    public static final ExtendedMenuType<ShopOffersMenu, BlockPos> SHOP_OFFERS = Registry
            .register(
                    BuiltInRegistries.MENU,
                    TradeRush.id("shop_offers"),
                    new ExtendedMenuType<>(
                            ShopOffersMenu::new,
                            BlockPos.STREAM_CODEC.cast()
                    )
            );

    private TradeRushMenus() {}

    public static void register() {
        // Static field initialization performs the registry call.
    }
}
