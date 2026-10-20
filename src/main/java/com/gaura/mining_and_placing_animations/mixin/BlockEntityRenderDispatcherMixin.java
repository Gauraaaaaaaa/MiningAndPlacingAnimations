package com.gaura.mining_and_placing_animations.mixin;

import com.gaura.mining_and_placing_animations.animation.BlockAnimationManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    /**
     * Animates block entities (chests, banners, signs...) in place. Callers translate the pose stack to the
     * block entity's origin before calling submit, so the animation is applied on top of that.
     * This covers vanilla, Sodium and the Iris shadow pass, which all go through this method.
     */
    @WrapMethod(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V")
    private void onSubmit(BlockEntityRenderState blockEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, Operation<Void> original) {

        if (!BlockAnimationManager.isBlockInvisible(blockEntityRenderState.blockPos)) {

            original.call(blockEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);
            return;
        }

        poseStack.pushPose();

        try {

            BlockAnimationManager.applyAnimation(poseStack, blockEntityRenderState.blockPos);
            original.call(blockEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);
        }
        finally {

            poseStack.popPose();
        }
    }

    /**
     * Makes the breaking cracks on block entities follow the animation. Both vanilla's LevelExtractor and
     * Sodium build the crumbling overlay and pass it in here.
     */
    @WrapMethod(method = "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Z)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;")
    private @Nullable BlockEntityRenderState onTryExtractRenderState(BlockEntity blockEntity, float partialTicks, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, boolean isGloballyRendered, Operation<BlockEntityRenderState> original) {

        return original.call(blockEntity, partialTicks, BlockAnimationManager.applyAnimation(breakProgress, blockEntity.getBlockPos()), isGloballyRendered);
    }
}
