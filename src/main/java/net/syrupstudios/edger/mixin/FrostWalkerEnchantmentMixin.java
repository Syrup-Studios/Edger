package net.syrupstudios.edger.mixin;

import net.syrupstudios.edger.callbacks.BlockPlaceCallback;
import net.syrupstudios.edger.utility.Sources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FrostWalkerEnchantment.class)
public abstract class FrostWalkerEnchantmentMixin {
    @ModifyArgs(method = "onEntityMoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static void logFrostWalkerPlacement(Args args, LivingEntity entity, Level world, BlockPos entityPos, int level) {
        // Frosted ice block is hardcoded in target class
        BlockPos pos = args.get(0);
        BlockState state = args.get(1);
        pos = pos.immutable();
        BlockPlaceCallback.EVENT.invoker().place(world, pos, state, null, Sources.FROST_WALKER,
                entity instanceof Player ? (Player) entity : null);
    }
}
