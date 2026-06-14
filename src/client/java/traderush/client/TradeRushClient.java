package traderush.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import traderush.client.teamui.TeamManagementClientNetworking;
import traderush.client.teamui.TeamManagementScreen;
import traderush.platform.registry.TradeRushMenus;

public class TradeRushClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(
            TradeRushMenus.TEAM_MANAGEMENT,
            TeamManagementScreen::new
        );
        TeamManagementClientNetworking.register();
    }
}
