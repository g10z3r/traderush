package traderush.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import traderush.client.rating.RatingPaintingClientNetworking;
import traderush.client.rating.TeamRatingPaintingRenderer;
import traderush.client.ui.rating.RatingBookClientNetworking;
import traderush.client.ui.rating.RatingBookScreen;
import traderush.client.ui.shop.ShopClientNetworking;
import traderush.client.ui.shop.ShopOffersScreen;
import traderush.client.ui.team.TeamManagementClientNetworking;
import traderush.client.ui.team.TeamManagementScreen;
import traderush.platform.registry.TradeRushEntities;
import traderush.platform.registry.TradeRushMenus;

public class TradeRushClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(
                TradeRushMenus.TEAM_MANAGEMENT,
                TeamManagementScreen::new
        );
        MenuScreens.register(
                TradeRushMenus.SHOP_OFFERS,
                ShopOffersScreen::new
        );
        MenuScreens.register(
                TradeRushMenus.TEAM_RATING_BOOK,
                RatingBookScreen::new
        );
        EntityRendererRegistry.register(
                TradeRushEntities.TEAM_RATING_PAINTING,
                TeamRatingPaintingRenderer::new
        );
        TeamManagementClientNetworking.register();
        RatingBookClientNetworking.register();
        ShopClientNetworking.register();
        RatingPaintingClientNetworking.register();
    }
}
