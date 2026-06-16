package traderush.client.ui.rating;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import traderush.platform.ui.rating.TeamRatingBookStatePayload;

public final class RatingBookClientNetworking {

    private static boolean registered;

    private RatingBookClientNetworking() {}

    public static void register() {
        if (registered) {
            return;
        }

        ClientPlayNetworking.registerGlobalReceiver(
                TeamRatingBookStatePayload.TYPE,
                (payload, context) -> context.client()
                        .execute(
                                () -> RatingBookScreen
                                        .receiveState(context.client(), payload)
                        )
        );
        registered = true;
    }
}
