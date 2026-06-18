package traderush.platform.item;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import traderush.platform.ui.rating.RatingBookMenu;
import traderush.platform.ui.rating.RatingBookNetworking;

public final class TeamRatingBook extends Item {

    public TeamRatingBook(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new Provider());
            RatingBookNetworking.sendInitialSnapshot(serverPlayer);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    private record Provider() implements ExtendedMenuProvider<BlockPos> {

        @Override
        public BlockPos getScreenOpeningData(ServerPlayer player) {
            return BlockPos.ZERO;
        }

        @Override
        public Component getDisplayName() {
            return Component
                    .translatable("container.trade-rush.team_rating_book");
        }

        @Override
        public AbstractContainerMenu createMenu(
                int containerId,
                Inventory inventory,
                Player player
        ) {
            return new RatingBookMenu(containerId, inventory);
        }
    }
}
