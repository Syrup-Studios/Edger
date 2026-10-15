package net.syrupstudios.edger.mixin;

import net.syrupstudios.edger.callbacks.BlockPlaceCallback;
import net.syrupstudios.edger.utility.Sources;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PortalShape.class)
public abstract class PortalShapeMixin {

    @Shadow
    @Final
    private LevelAccessor level;

    @Inject(method = "method_30488", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void logPortalPlacement(BlockState state, BlockPos pos, CallbackInfo ci) {
        if (this.level instanceof ServerLevel world) {
            BlockPlaceCallback.EVENT.invoker().place(world, pos.immutable(), state, null, Sources.PORTAL);
        }
    }
}
