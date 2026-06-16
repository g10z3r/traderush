package traderush.platform.ui.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import traderush.game.shop.ShopId;
import traderush.platform.registry.TradeRushBlocks;
import traderush.platform.registry.TradeRushMenus;

public final class ShopOffersMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final BlockPos sourcePos;
    private final ShopId shopId;

    /**
     * Client-side factory used by
     * {@link traderush.platform.registry.TradeRushMenus} (no {@link ShopId}
     * available on the client).
     */
    public ShopOffersMenu(
            int containerId,
            Inventory inventory,
            BlockPos sourcePos
    ) {
        this(
                containerId, inventory, ContainerLevelAccess.NULL, sourcePos,
                null
        );
    }

    public ShopOffersMenu(
            int containerId,
            Inventory inventory,
            BlockPos sourcePos,
            ShopId shopId
    ) {
        this(
                containerId,
                inventory,
                ContainerLevelAccess.NULL,
                sourcePos,
                shopId
        );
    }

    public ShopOffersMenu(
            int containerId,
            Inventory inventory,
            ContainerLevelAccess access,
            BlockPos sourcePos,
            ShopId shopId
    ) {
        super(TradeRushMenus.SHOP_OFFERS, containerId);
        this.access = access == null ? ContainerLevelAccess.NULL : access;
        this.sourcePos = sourcePos == null ? BlockPos.ZERO : sourcePos;
        this.shopId = shopId; // may be null on client side
    }

    public BlockPos sourcePos() {
        return sourcePos;
    }

    public ShopId shopId() {
        return shopId;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (access == ContainerLevelAccess.NULL) {
            return true;
        }

        return stillValid(access, player, TradeRushBlocks.SHOP_BLOCK);
    }
}
