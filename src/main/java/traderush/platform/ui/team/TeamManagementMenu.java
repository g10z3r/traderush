package traderush.platform.ui.team;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import traderush.platform.registry.TradeRushBlocks;
import traderush.platform.registry.TradeRushMenus;

public final class TeamManagementMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final BlockPos sourcePos;

    public TeamManagementMenu(
            int containerId,
            Inventory inventory,
            BlockPos sourcePos
    ) {
        this(containerId, inventory, ContainerLevelAccess.NULL, sourcePos);
    }

    public TeamManagementMenu(
            int containerId,
            Inventory inventory,
            ContainerLevelAccess access,
            BlockPos sourcePos
    ) {
        super(TradeRushMenus.TEAM_MANAGEMENT, containerId);
        this.access = access == null ? ContainerLevelAccess.NULL : access;
        this.sourcePos = sourcePos == null ? BlockPos.ZERO : sourcePos;
    }

    public BlockPos sourcePos() {
        return sourcePos;
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

        return stillValid(
                access,
                player,
                TradeRushBlocks.TEAM_MANAGEMENT_BLOCK
        );
    }
}
