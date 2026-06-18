package traderush.platform.ui.rating;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import traderush.TradeRush;
import traderush.game.team.TeamService;

public final class RatingBookNetworking {

    private static boolean payloadTypesRegistered;

    private RatingBookNetworking() {}

    public static void registerPayloadTypes() {
        if (payloadTypesRegistered) {
            return;
        }

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TeamRatingBookStatePayload.TYPE,
                        TeamRatingBookStatePayload.CODEC
                );
        payloadTypesRegistered = true;
    }

    public static void sendInitialSnapshot(ServerPlayer player) {
        sendSnapshot(player);
    }

    public static void sendSnapshotIfBookOpen(ServerPlayer player) {
        if (!hasOpenRatingBook(player)) {
            return;
        }

        sendSnapshot(player);
    }

    public static boolean hasOpenRatingBook(ServerPlayer player) {
        return player.containerMenu instanceof RatingBookMenu menu
                && menu.stillValid(player);
    }

    public static void sendSnapshot(ServerPlayer player) {
        if (!ServerPlayNetworking
                .canSend(player, TeamRatingBookStatePayload.TYPE)) {
            return;
        }

        TeamRatingBookSnapshot snapshot;

        try {
            TeamService teamService = TradeRush.runtime().teamService();
            snapshot = TeamRatingBookSnapshots.create(teamService);
        } catch (IllegalStateException exception) {
            snapshot = TeamRatingBookSnapshot.RUNTIME_NOT_READY;
        }

        ServerPlayNetworking.send(
                player,
                new TeamRatingBookStatePayload(snapshot)
        );
    }
}
