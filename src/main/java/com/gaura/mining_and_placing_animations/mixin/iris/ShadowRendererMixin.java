package com.gaura.mining_and_placing_animations.mixin.iris;

import com.gaura.mining_and_placing_animations.animation.BlockAnimationManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds the animated blocks to the shader shadow pass. Optional: if a future Iris build changes this method,
 * the injection is skipped (no shadows for animated blocks) instead of crashing.
 */
@Mixin(value = ShadowRenderer.class, remap = false)
public class ShadowRendererMixin {

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @Inject(method = "renderEntities", at = @At("HEAD"), require = 0)
    private void onRenderEntities(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, PoseStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Integer> cir) {

        BlockAnimationManager.submit(modelView, this.submitNodeStorage, new Vec3(cameraX, cameraY, cameraZ), tickDelta);
    }
}
