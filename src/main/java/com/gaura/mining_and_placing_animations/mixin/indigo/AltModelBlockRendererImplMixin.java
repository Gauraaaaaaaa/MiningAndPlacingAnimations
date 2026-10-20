package com.gaura.mining_and_placing_animations.mixin.indigo;

import com.gaura.mining_and_placing_animations.animation.BlockAnimationManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * On 26.2 Fabric API reroutes vanilla chunk meshing (SectionCompiler) through Indigo's AltModelBlockRendererImpl,
 * replacing the old TerrainRenderContext / BlockRenderInfo classes.
 */
@Mixin(value = AltModelBlockRendererImpl.class, remap = false)
public class AltModelBlockRendererImplMixin {

    @Shadow
    @Final
    private BlockPos.MutableBlockPos scratchPos;

    /**
     * Leaves the animated block out of the chunk mesh. Only applies to chunk meshing (level is a
     * RenderSectionRegion), so the animated "moving block" itself, which uses the same renderer, still draws.
     */
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void onTesselateBlock(QuadEmitter output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, CallbackInfo ci) {

        if (level instanceof RenderSectionRegion && BlockAnimationManager.isBlockInvisible(pos)) {

            ci.cancel();
        }
    }

    /**
     * Draws the faces of neighbours that the hidden block would normally cover.
     */
    @WrapOperation(
            method = "shouldCullFace",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"
            )
    )
    private boolean onShouldRenderFace(BlockState blockState, BlockState neighborState, Direction direction, Operation<Boolean> original) {

        if (BlockAnimationManager.isBlockInvisible(this.scratchPos)) {

            return true;
        }

        return original.call(blockState, neighborState, direction);
    }
}
