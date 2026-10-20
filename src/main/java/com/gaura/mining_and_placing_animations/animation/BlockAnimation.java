package com.gaura.mining_and_placing_animations.animation;

import com.gaura.mining_and_placing_animations.animation.data.AnimationModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockAnimation {

    private final ClientLevel clientLevel;
    private int age;
    private final int lifetime;
    private boolean alive;
    private final BlockPos blockPos;
    private final BlockState blockState;
    private final AnimationModel animationModel;
    private final MovingBlockRenderState movingBlockRenderState;
    private final boolean loopable;
    private int ticksSinceKeepAlive;

    public BlockAnimation(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState, AnimationModel animationModel, boolean loopable) {

        this.clientLevel = clientLevel;
        this.loopable = loopable;
        this.age = 0;
        this.lifetime = Math.max(1, (int) Math.ceil(animationModel.duration * 20));
        this.alive = true;
        this.blockPos = blockPos;
        this.blockState = blockState;
        this.animationModel = animationModel;

        // 26.2 removed MultiBufferSource; blocks drawn outside the chunk mesh are submitted
        // as "moving blocks" (the same path falling blocks and pistons use).
        this.movingBlockRenderState = new MovingBlockRenderState();
        this.movingBlockRenderState.randomSeedPos = blockPos;
        this.movingBlockRenderState.blockPos = blockPos;
        this.movingBlockRenderState.blockState = blockState;
        this.movingBlockRenderState.biome = clientLevel.getBiome(blockPos);
        this.movingBlockRenderState.cardinalLighting = clientLevel.cardinalLighting();
        this.movingBlockRenderState.lightEngine = clientLevel.getLightEngine();
    }

    /**
     * Called every tick the player keeps mining this block, so the animation loops seamlessly
     * instead of putting the real block back and re-hiding it (which caused a flicker on every loop).
     */
    public void keepAlive() {

        this.ticksSinceKeepAlive = 0;
    }

    public void tick() {

        this.age++;
        this.ticksSinceKeepAlive++;

        if (this.loopable && this.age >= this.lifetime && this.ticksSinceKeepAlive <= 2) {

            this.age = 0;
            return;
        }

        if (this.age == this.lifetime) {

            BlockAnimationManager.setBlockVisible(this.clientLevel, this.blockPos, this.blockState);
        }
        else if (this.age > this.lifetime) {

            this.alive = false;
        }
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 cameraPos, float partialTick) {

        poseStack.pushPose();

        poseStack.translate(this.blockPos.getX() - cameraPos.x(), this.blockPos.getY() - cameraPos.y(), this.blockPos.getZ() - cameraPos.z());

        this.animationModel.apply(poseStack, this.getProgress(partialTick));

        submitNodeCollector.submitMovingBlock(poseStack, this.movingBlockRenderState, 0);

        poseStack.popPose();
    }

    public float getProgress(float partialTick) {

        return Math.min(1.0F, (this.age + partialTick) / this.lifetime);
    }

    public boolean isLoopable() {

        return this.loopable;
    }

    public AnimationModel getAnimationModel() {

        return animationModel;
    }

    public ClientLevel getLevel() {

        return this.clientLevel;
    }

    public boolean isAlive() {

        return this.alive;
    }
}
