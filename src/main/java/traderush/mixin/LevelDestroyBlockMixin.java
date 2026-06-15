package traderush.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traderush.platform.protection.MinecraftShopBlockProtection;
import traderush.platform.protection.ShopProtectionBypass;

@Mixin(Level.class)
public abstract class LevelDestroyBlockMixin {

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void tradeRush$cancelDestroyBlock(
            BlockPos pos,
            boolean dropResources,
            Entity breakingEntity,
            int maxUpdateDepth,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (isProtectedServer(pos)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            at = @At("HEAD"), cancellable = true)
    private void tradeRush$protectShopArea(
            BlockPos pos,
            BlockState state,
            int flags,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!((Object) this instanceof ServerLevel level)) {
            return;
        }

        if (ShopProtectionBypass.isActive()) {
            return;
        }

        if (!MinecraftShopBlockProtection.isProtected(level, pos)) {
            return;
        }

        callback.setReturnValue(false);
    }

    private boolean isProtectedServer(BlockPos pos) {
        if (!((Object) this instanceof ServerLevel level)) {
            return false;
        }

        return MinecraftShopBlockProtection.isProtected(level, pos);
    }
}
