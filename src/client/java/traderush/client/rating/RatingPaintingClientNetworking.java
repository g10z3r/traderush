package traderush.client.rating;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import traderush.platform.ui.rating.TeamRatingPaintingStatePayload;

public final class RatingPaintingClientNetworking {

    private static boolean registered;

    private RatingPaintingClientNetworking() {}

    public static void register() {
        if (registered) {
            return;
        }

        ClientPlayNetworking.registerGlobalReceiver(
                TeamRatingPaintingStatePayload.TYPE,
                (payload, context) -> context.client()
                        .execute(
                                () -> RatingPaintingClientState
                                        .update(payload.snapshot())
                        )
        );
        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> RatingPaintingClientState.clear()
        );
        registered = true;
    }
}
