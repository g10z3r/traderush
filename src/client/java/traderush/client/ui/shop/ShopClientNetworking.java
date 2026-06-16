package traderush.client.ui.shop;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import traderush.platform.ui.shop.ShopOffersPayload;
import traderush.platform.ui.shop.ShopTradeResultPayload;

public final class ShopClientNetworking {

    private static boolean registered;

    private ShopClientNetworking() {}

    public static void register() {
        if (registered) {
            return;
        }

        ClientPlayNetworking.registerGlobalReceiver(
                ShopOffersPayload.TYPE,
                (payload, context) -> context.client()
                        .execute(
                                () -> ShopOffersScreen
                                        .receiveOffers(
                                                context.client(),
                                                payload
                                        )
                        )
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ShopTradeResultPayload.TYPE,
                (payload, context) -> context.client()
                        .execute(
                                () -> ShopOffersScreen
                                        .receiveTradeResult(
                                                context.client(),
                                                payload
                                        )
                        )
        );

        registered = true;
    }
}
