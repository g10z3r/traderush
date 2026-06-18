package traderush.client.rating;

import traderush.platform.ui.rating.TeamRatingPaintingSnapshot;

public final class RatingPaintingClientState {
    private static TeamRatingPaintingSnapshot snapshot;

    private RatingPaintingClientState() {}

    public static TeamRatingPaintingSnapshot snapshot() {
        return snapshot;
    }

    public static void update(TeamRatingPaintingSnapshot updatedSnapshot) {
        snapshot = updatedSnapshot == null ? TeamRatingPaintingSnapshot.EMPTY
                : updatedSnapshot;
    }

    public static void clear() {
        snapshot = null;
    }
}
