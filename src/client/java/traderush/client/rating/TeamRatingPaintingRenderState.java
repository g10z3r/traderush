package traderush.client.rating;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import traderush.platform.ui.rating.TeamRatingPaintingSnapshot;

public final class TeamRatingPaintingRenderState extends EntityRenderState {
    public Direction direction = Direction.SOUTH;
    public TeamRatingPaintingSnapshot snapshot;
}
