package traderush.platform.ui;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import traderush.platform.ui.rating.RatingBookNetworking;
import traderush.platform.ui.team.TeamManagementNetworking;

public final class TeamUiBroadcaster {

    private TeamUiBroadcaster() {}

    public static void broadcastTeamSnapshots(MinecraftServer server) {
        if (server == null) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            broadcastTarget(new ServerPlayerTarget(player));
        }
    }

    static void broadcastTargets(Iterable<? extends BroadcastTarget> targets) {
        if (targets == null) {
            return;
        }

        for (BroadcastTarget target : targets) {
            broadcastTarget(target);
        }
    }

    private static void broadcastTarget(BroadcastTarget target) {
        if (target == null) {
            return;
        }

        if (target.hasTeamManagementOpen()) {
            target.sendTeamManagementSnapshot();
        }

        if (target.hasRatingBookOpen()) {
            target.sendRatingBookSnapshot();
        }
    }

    interface BroadcastTarget {
        boolean hasTeamManagementOpen();

        boolean hasRatingBookOpen();

        void sendTeamManagementSnapshot();

        void sendRatingBookSnapshot();
    }

    private record ServerPlayerTarget(ServerPlayer player)
            implements BroadcastTarget {

        @Override
        public boolean hasTeamManagementOpen() {
            return TeamManagementNetworking.hasOpenManagementMenu(player);
        }

        @Override
        public boolean hasRatingBookOpen() {
            return RatingBookNetworking.hasOpenRatingBook(player);
        }

        @Override
        public void sendTeamManagementSnapshot() {
            TeamManagementNetworking.sendSnapshotIfManagementOpen(player);
        }

        @Override
        public void sendRatingBookSnapshot() {
            RatingBookNetworking.sendSnapshotIfBookOpen(player);
        }
    }
}
