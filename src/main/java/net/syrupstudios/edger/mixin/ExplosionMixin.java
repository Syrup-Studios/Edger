package net.syrupstudios.edger.mixin;

import net.syrupstudios.edger.callbacks.BlockBreakCallback;
import net.syrupstudios.edger.callbacks.BlockPlaceCallback;
import net.syrupstudios.edger.utility.PlayerCausable;
import net.syrupstudios.edger.utility.Sources;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow
    public abstract @Nullable LivingEntity getIndirectSourceEntity();

    @Shadow
    @Final
    @Nullable
    private Entity source;

    @Shadow
    @Final
    private Level level;

    @Shadow
    @Nullable
    public abstract Entity getDirectSourceEntity();

    @Inject(
        method = "finalizeExplosion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
        )
    )
    private void ledgerExplosionFireCallback(boolean bl, CallbackInfo ci, @Local BlockPos blockPos) {
        BlockState blockState = BaseFireBlock.getState(level, blockPos);

        LivingEntity entity;
        if (this.source instanceof PlayerCausable playerCausable && playerCausable.getCausingPlayer() != null) {
            entity = playerCausable.getCausingPlayer();
        } else {
            entity = getIndirectSourceEntity();
        }

        String source;
        if (this.source != null && !(this.source instanceof Player)) {
            source = BuiltInRegistries.ENTITY_TYPE.getKey(this.source.getType()).getPath();
        } else {
            source = Sources.EXPLOSION;
        }

        BlockPlaceCallback.EVENT.invoker().place(
            level,
            blockPos,
            blockState,
            level.getBlockEntity(blockPos) != null ? level.getBlockEntity(blockPos) : null,
            source,
            entity instanceof Player player ? player : null
        );
    }

    @Inject(
        method = "finalizeExplosion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
        )
    )
    public void ledgerBlockExplodeCallback(boolean bl, CallbackInfo ci, @Local BlockPos pos, @Local BlockState state) {
        LivingEntity entity;
        if (this.source instanceof PlayerCausable playerCausable && playerCausable.getCausingPlayer() != null) {
            entity = playerCausable.getCausingPlayer();
        } else {
            entity = getIndirectSourceEntity();
        }

        String source;
        if (getDirectSourceEntity() != null && !(getDirectSourceEntity() instanceof Player)) {
            source = BuiltInRegistries.ENTITY_TYPE.getKey(getDirectSourceEntity().getType()).getPath();
        } else {
            source = Sources.EXPLOSION;
        }

        BlockBreakCallback.EVENT.invoker().breakBlock(
                level,
                pos,
                state,
                level.getBlockEntity(pos) != null ? level.getBlockEntity(pos) : null,
                source,
                entity instanceof Player player ? player : null
        );
    }
}
