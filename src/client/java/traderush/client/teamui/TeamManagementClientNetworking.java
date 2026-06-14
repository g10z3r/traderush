package traderush.client.teamui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import traderush.platform.teamui.TeamManagementStatePayload;

public final class TeamManagementClientNetworking {
    private static boolean registered;

    private TeamManagementClientNetworking() {}

    public static void register() {
        if (registered) {
            return;
        }

        ClientPlayNetworking.registerGlobalReceiver(
            TeamManagementStatePayload.TYPE,
            (payload, context) -> context.client()
                .execute(() -> TeamManagementScreen.receiveState(context.client(), payload))
        );
        registered = true;
    }
}
