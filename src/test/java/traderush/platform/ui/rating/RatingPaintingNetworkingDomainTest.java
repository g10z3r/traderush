package traderush.platform.ui.rating;

import java.util.List;

public final class RatingPaintingNetworkingDomainTest {

    private RatingPaintingNetworkingDomainTest() {}

    public static void run() {
        sendsOnePaintingSnapshotPerSupportedTarget();
        skipsTargetsWithoutPaintingPayloadSupport();
    }

    private static void sendsOnePaintingSnapshotPerSupportedTarget() {
        TeamRatingPaintingStatePayload payload = payload();
        FakeTarget oneVisiblePainting = new FakeTarget(true, 1);
        FakeTarget manyVisiblePaintings = new FakeTarget(true, 24);

        RatingPaintingNetworking.sendPayloadToTargets(
                payload,
                List.of(oneVisiblePainting, manyVisiblePaintings)
        );

        assertSends(
                oneVisiblePainting,
                1,
                "target with one visible painting should receive one payload"
        );
        assertSends(
                manyVisiblePaintings,
                1,
                "target with many visible paintings should still receive one payload"
        );
        assertSame(
                payload,
                oneVisiblePainting.lastPayload,
                "target should receive the shared snapshot payload"
        );
        assertSame(
                payload,
                manyVisiblePaintings.lastPayload,
                "targets should receive the same shared snapshot payload"
        );
    }

    private static void skipsTargetsWithoutPaintingPayloadSupport() {
        FakeTarget unsupported = new FakeTarget(false, 12);

        RatingPaintingNetworking.sendPayloadToTargets(
                payload(),
                List.of(unsupported)
        );

        assertSends(
                unsupported,
                0,
                "unsupported target should not receive painting payloads"
        );
    }

    private static TeamRatingPaintingStatePayload payload() {
        return new TeamRatingPaintingStatePayload(
                new TeamRatingPaintingSnapshot(
                        List.of(new TeamRatingRow(1, "Alpha", 10L)),
                        true
                )
        );
    }

    private static void assertSends(
            FakeTarget target,
            int expectedSends,
            String message
    ) {
        assertEquals(
                expectedSends,
                target.sends,
                message + " (visible paintings: " + target.visiblePaintings
                        + ")"
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

    private static void assertSame(
            Object expected,
            Object actual,
            String message
    ) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected same instance");
        }
    }

    private static final class FakeTarget
            implements RatingPaintingNetworking.SnapshotTarget {
        private final boolean canSend;
        private final int visiblePaintings;
        private int sends;
        private TeamRatingPaintingStatePayload lastPayload;

        private FakeTarget(boolean canSend, int visiblePaintings) {
            this.canSend = canSend;
            this.visiblePaintings = visiblePaintings;
        }

        @Override
        public boolean canSendPaintingSnapshot() {
            return canSend;
        }

        @Override
        public void sendPaintingSnapshot(
                TeamRatingPaintingStatePayload payload
        ) {
            sends++;
            lastPayload = payload;
        }
    }
}
