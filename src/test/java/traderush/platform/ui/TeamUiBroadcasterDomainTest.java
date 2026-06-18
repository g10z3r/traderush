package traderush.platform.ui;

import java.util.List;

public final class TeamUiBroadcasterDomainTest {

    private TeamUiBroadcasterDomainTest() {}

    public static void run() {
        broadcastsSnapshotsOnlyForOpenDependentMenus();
    }

    private static void broadcastsSnapshotsOnlyForOpenDependentMenus() {
        FakeTarget noOpenMenu = new FakeTarget(false, false);
        FakeTarget managementOpen = new FakeTarget(true, false);
        FakeTarget ratingBookOpen = new FakeTarget(false, true);
        FakeTarget bothOpen = new FakeTarget(true, true);

        TeamUiBroadcaster.broadcastTargets(
                List.of(noOpenMenu, managementOpen, ratingBookOpen, bothOpen)
        );

        assertSends(
                noOpenMenu,
                0,
                0,
                "closed target should not receive snapshots"
        );
        assertSends(
                managementOpen,
                1,
                0,
                "management target should receive only management snapshot"
        );
        assertSends(
                ratingBookOpen,
                0,
                1,
                "rating book target should receive only rating snapshot"
        );
        assertSends(
                bothOpen,
                1,
                1,
                "target with both dependent menus should receive both snapshots"
        );
    }

    private static void assertSends(
            FakeTarget target,
            int expectedManagementSends,
            int expectedRatingBookSends,
            String message
    ) {
        assertEquals(
                expectedManagementSends,
                target.managementSends,
                message + " (management)"
        );
        assertEquals(
                expectedRatingBookSends,
                target.ratingBookSends,
                message + " (rating book)"
        );
    }

    private static void assertEquals(
            int expected,
            int actual,
            String message
    ) {
        if (expected != actual) {
            throw new AssertionError(
                    message + ": expected <" + expected + "> but got <"
                            + actual + ">"
            );
        }
    }

    private static final class FakeTarget
            implements TeamUiBroadcaster.BroadcastTarget {
        private final boolean managementOpen;
        private final boolean ratingBookOpen;
        private int managementSends;
        private int ratingBookSends;

        private FakeTarget(boolean managementOpen, boolean ratingBookOpen) {
            this.managementOpen = managementOpen;
            this.ratingBookOpen = ratingBookOpen;
        }

        @Override
        public boolean hasTeamManagementOpen() {
            return managementOpen;
        }

        @Override
        public boolean hasRatingBookOpen() {
            return ratingBookOpen;
        }

        @Override
        public void sendTeamManagementSnapshot() {
            managementSends++;
        }

        @Override
        public void sendRatingBookSnapshot() {
            ratingBookSends++;
        }
    }
}
