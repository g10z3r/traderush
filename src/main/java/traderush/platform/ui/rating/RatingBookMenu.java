package traderush.platform.ui.rating;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import traderush.platform.registry.TradeRushMenus;

public final class RatingBookMenu extends AbstractContainerMenu {

    public RatingBookMenu(
            int containerId,
            Inventory inventory,
            BlockPos ignoredSourcePos
    ) {
        this(containerId, inventory);
    }

    public RatingBookMenu(int containerId, Inventory inventory) {
        super(TradeRushMenus.TEAM_RATING_BOOK, containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
