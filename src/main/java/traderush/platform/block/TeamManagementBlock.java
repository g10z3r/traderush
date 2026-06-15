package traderush.platform.block;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import traderush.platform.ui.team.TeamManagementMenu;
import traderush.platform.ui.team.TeamManagementNetworking;

public final class TeamManagementBlock extends Block {

    public TeamManagementBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new Provider(level, pos));
            TeamManagementNetworking.sendInitialSnapshot(serverPlayer);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    private record Provider(
        Level level,
        BlockPos pos
    ) implements ExtendedMenuProvider<BlockPos> {
        @Override
        public BlockPos getScreenOpeningData(ServerPlayer player) {
            return pos;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable(
                "container.trade-rush.team_management"
            );
        }

        @Override
        public AbstractContainerMenu createMenu(
            int containerId,
            Inventory inventory,
            Player player
        ) {
            return new TeamManagementMenu(
                containerId,
                inventory,
                ContainerLevelAccess.create(level, pos),
                pos
            );
        }
    }
}
