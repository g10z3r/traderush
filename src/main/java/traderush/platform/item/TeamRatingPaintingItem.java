package traderush.platform.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import traderush.platform.entity.TeamRatingPaintingEntity;

public final class TeamRatingPaintingItem extends Item {

    public TeamRatingPaintingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockPos placePos = context.getClickedPos().relative(clickedFace);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (!mayPlace(player, clickedFace, stack, placePos)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        TeamRatingPaintingEntity painting = new TeamRatingPaintingEntity(
                level,
                placePos,
                clickedFace
        );
        EntityType.createDefaultStackConfig(level, stack, player)
                .accept(painting);

        if (!painting.survives()) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide()) {
            painting.playPlacementSound();
            level.gameEvent(
                    player,
                    GameEvent.ENTITY_PLACE,
                    painting.position()
            );
            level.addFreshEntity(painting);

            if (player == null || !player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
        }

        return level.isClientSide()
                ? InteractionResult.SUCCESS
                : InteractionResult.SUCCESS_SERVER;
    }

    private static boolean mayPlace(
            Player player,
            Direction direction,
            ItemStack stack,
            BlockPos placePos
    ) {
        if (!direction.getAxis().isHorizontal()) {
            return false;
        }

        return player == null
                || player.mayUseItemAt(placePos, direction, stack);
    }
}
