package traderush.platform.ui.rating;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import traderush.TradeRush;
import traderush.game.team.TeamService;

public final class RatingPaintingNetworking {

    private static boolean payloadTypesRegistered;
    private static boolean serverHandlersRegistered;

    private RatingPaintingNetworking() {}

    public static void registerPayloadTypes() {
        if (payloadTypesRegistered) {
            return;
        }

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TeamRatingPaintingStatePayload.TYPE,
                        TeamRatingPaintingStatePayload.CODEC
                );
        payloadTypesRegistered = true;
    }

    public static void registerServerHandlers() {
        if (serverHandlersRegistered) {
            return;
        }

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> sendInitialSnapshot(
                        handler.getPlayer()
                )
        );
        serverHandlersRegistered = true;
    }

    public static void sendInitialSnapshot(ServerPlayer player) {
        sendSnapshot(player);
    }

    public static void sendSnapshot(ServerPlayer player) {
        if (player == null) {
            return;
        }

        sendPayloadToTargets(
                new TeamRatingPaintingStatePayload(snapshotFromRuntime()),
                List.of(new ServerPlayerTarget(player))
        );
    }

    public static void broadcastSnapshot(MinecraftServer server) {
        if (server == null) {
            return;
        }

        TeamRatingPaintingStatePayload payload = new TeamRatingPaintingStatePayload(
                snapshotFromRuntime()
        );
        sendPayloadToTargets(payload, targetsFor(server));
    }

    static void sendPayloadToTargets(
            TeamRatingPaintingStatePayload payload,
            Iterable<? extends SnapshotTarget> targets
    ) {
        if (payload == null || targets == null) {
            return;
        }

        for (SnapshotTarget target : targets) {
            if (target != null && target.canSendPaintingSnapshot()) {
                target.sendPaintingSnapshot(payload);
            }
        }
    }

    private static TeamRatingPaintingSnapshot snapshotFromRuntime() {
        try {
            TeamService teamService = TradeRush.runtime().teamService();
            return TeamRatingPaintingSnapshots.create(teamService);
        } catch (IllegalStateException exception) {
            return TeamRatingPaintingSnapshot.RUNTIME_NOT_READY;
        }
    }

    private static List<ServerPlayerTarget> targetsFor(MinecraftServer server) {
        List<ServerPlayerTarget> targets = new ArrayList<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            targets.add(new ServerPlayerTarget(player));
        }

        return targets;
    }

    interface SnapshotTarget {
        boolean canSendPaintingSnapshot();

        void sendPaintingSnapshot(TeamRatingPaintingStatePayload payload);
    }

    private record ServerPlayerTarget(ServerPlayer player)
            implements SnapshotTarget {

        @Override
        public boolean canSendPaintingSnapshot() {
            return ServerPlayNetworking
                    .canSend(player, TeamRatingPaintingStatePayload.TYPE);
        }

        @Override
        public void sendPaintingSnapshot(
                TeamRatingPaintingStatePayload payload
        ) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
