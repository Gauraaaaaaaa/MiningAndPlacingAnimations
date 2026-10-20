package com.gaura.mining_and_placing_animations.mixin;

import com.gaura.mining_and_placing_animations.animation.BlockAnimationManager;
import com.gaura.mining_and_placing_animations.animation.data.AnimationModel;
import com.gaura.mining_and_placing_animations.animation.data.AnimationResourceManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "updateBlockStateFromTag", at = @At("HEAD"))
    private void onUpdateBlockStateFromTag(BlockPos blockPos, Level level, ItemStack itemStack, BlockState blockState, CallbackInfoReturnable<BlockState> cir) {

        if (level instanceof ClientLevel clientLevel && !BlockAnimationManager.isBlockInvisible(blockPos)) {

            AnimationModel animationModel = AnimationResourceManager.getAnimationModel(AnimationResourceManager.getPlacingAnimationId(blockState));

            if (animationModel != null) {

                BlockAnimationManager.addAnimation(clientLevel, blockPos, blockState, animationModel, false);
            }
        }
    }
}