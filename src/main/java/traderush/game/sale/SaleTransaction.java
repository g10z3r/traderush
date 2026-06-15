package traderush.game.sale;

import java.util.List;
import java.util.Objects;
import traderush.game.player.PlayerId;
import traderush.game.shop.ShopId;
import traderush.game.team.TeamId;

public record SaleTransaction(
        SaleId id,
        ShopId shopId,
        TeamId teamId,
        PlayerId playerId,
        List<SaleLine> lines,
        long totalPoints
) {
    public SaleTransaction {
        Objects.requireNonNull(id, "sale id cannot be null");
        Objects.requireNonNull(shopId, "shop id cannot be null");
        Objects.requireNonNull(teamId, "team id cannot be null");
        Objects.requireNonNull(playerId, "player id cannot be null");

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "sale transaction must contain at least one line"
            );
        }

        lines = List.copyOf(lines);

        if (totalPoints <= 0) {
            throw new IllegalArgumentException(
                    "sale transaction total points must be positive"
            );
        }
    }
}
