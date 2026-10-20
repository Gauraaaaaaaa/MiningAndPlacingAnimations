package com.gaura.mining_and_placing_animations.mixin;

import com.gaura.mining_and_placing_animations.animation.BlockAnimationManager;
import com.gaura.mining_and_placing_animations.animation.data.AnimationModel;
import com.gaura.mining_and_placing_animations.animation.data.AnimationResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void onDestroyBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {

        ClientLevel clientLevel = this.minecraft.level;

        if (clientLevel != null && BlockAnimationManager.isBlockInvisible(blockPos)) {

            BlockAnimationManager.removeAnimation(clientLevel, blockPos, clientLevel.getBlockState(blockPos));
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void onContinueDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {

        ClientLevel clientLevel = this.minecraft.level;

        if (clientLevel == null) {

            return;
        }

        if (BlockAnimationManager.isBlockInvisible(blockPos)) {

            BlockAnimationManager.keepAlive(blockPos);
            return;
        }

        BlockState blockState = clientLevel.getBlockState(blockPos);

        AnimationModel animationModel = AnimationResourceManager.getAnimationModel(AnimationResourceManager.getMiningAnimationId(blockState));

        if (animationModel != null) {

            BlockAnimationManager.addAnimation(clientLevel, blockPos, blockState, animationModel, true);
        }
    }
}