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
import traderush.TradeRush;
import traderush.game.shop.Shop;
import traderush.game.shop.ShopId;
import traderush.game.shop.ShopLocation;
import traderush.platform.ui.shop.ShopNetworking;
import traderush.platform.ui.shop.ShopOffersMenu;

public final class ShopBlock extends Block {

    public ShopBlock(Properties properties) {
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

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        ShopId shopId = findShopId(level, pos);

        if (shopId == null) {
            return InteractionResult.SUCCESS_SERVER;
        }

        serverPlayer.openMenu(new Provider(level, pos, shopId));
        ShopNetworking.sendOffers(serverPlayer, shopId);

        return InteractionResult.SUCCESS_SERVER;
    }

    private ShopId findShopId(Level level, BlockPos pos) {
        String dimensionId = level.dimension().identifier().toString();
        ShopLocation location = ShopLocation
                .of(dimensionId, pos.getX(), pos.getY(), pos.getZ());

        try {
            return TradeRush.runtime()
                    .shopService()
                    .findByLocation(location)
                    .map(Shop::getId)
                    .orElse(null);
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    private record Provider(Level level, BlockPos pos, ShopId shopId)
            implements ExtendedMenuProvider<BlockPos> {

        @Override
        public BlockPos getScreenOpeningData(ServerPlayer player) {
            return pos;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("container.trade-rush.shop");
        }

        @Override
        public AbstractContainerMenu createMenu(
                int containerId,
                Inventory inventory,
                Player player
        ) {
            return new ShopOffersMenu(
                    containerId,
                    inventory,
                    ContainerLevelAccess.create(level, pos),
                    pos,
                    shopId
            );
        }
    }
}
