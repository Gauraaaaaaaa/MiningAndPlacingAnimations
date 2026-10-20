package com.gaura.mining_and_placing_animations.mixin;

import com.gaura.mining_and_placing_animations.animation.BlockAnimationManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    /**
     * Makes the block-breaking cracks follow the animated block. Runs right after the pose stack is moved to the
     * block (and its model offset), just before the breaking model is submitted.
     */
    @Inject(
            method = "submitBlockDestroyAnimation(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;setSeed(J)V")
    )
    private void onSubmitBlockDestroyAnimation(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci, @Local(name = "state") BlockBreakingRenderState state) {

        BlockAnimationManager.applyAnimation(poseStack, state.blockPos());
    }

    /**
     * Makes the block outline follow the animated block, and hides it while a block is hidden without an animation.
     */
    @WrapMethod(method = "submitBlockOutline")
    private void onSubmitBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, Operation<Void> original) {

        BlockOutlineRenderState outlineState = levelRenderState.blockOutlineRenderState;

        if (outlineState == null || !BlockAnimationManager.isBlockInvisible(outlineState.pos())) {

            original.call(poseStack, submitNodeCollector, levelRenderState);
            return;
        }

        if (BlockAnimationManager.getAnimation(outlineState.pos()) == null) {

            return;
        }

        BlockPos blockPos = outlineState.pos();
        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        Vec3 offset = new Vec3(blockPos.getX() - cameraPos.x(), blockPos.getY() - cameraPos.y(), blockPos.getZ() - cameraPos.z());

        poseStack.pushPose();

        try {

            poseStack.translate(offset);
            BlockAnimationManager.applyAnimation(poseStack, blockPos);
            poseStack.translate(offset.reverse());

            original.call(poseStack, submitNodeCollector, levelRenderState);
        }
        finally {

            poseStack.popPose();
        }
    }
}
