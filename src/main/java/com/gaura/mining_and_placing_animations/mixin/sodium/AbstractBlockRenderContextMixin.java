package com.gaura.mining_and_placing_animations.mixin.sodium;

import com.gaura.mining_and_placing_animations.animation.BlockAnimationManager;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public class AbstractBlockRenderContextMixin {

    @Inject(
            method = "shouldDrawSide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;setWithOffset(Lnet/minecraft/core/Vec3i;Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos$MutableBlockPos;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onShouldDrawSide(Direction direction, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) BlockPos.MutableBlockPos neighborBlockPos) {

        if (BlockAnimationManager.isBlockInvisible(neighborBlockPos)) {

            cir.setReturnValue(true);
        }
    }
}
