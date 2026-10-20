package com.gaura.mining_and_placing_animations.animation;

import com.gaura.mining_and_placing_animations.animation.data.AnimationModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class BlockAnimationManager {

    private static final Map<BlockPos, BlockAnimation> ANIMATED_BLOCKS = new HashMap<>();

    // Read from chunk-meshing worker threads, so it must be thread-safe.
    private static final Set<BlockPos> INVISIBLE_BLOCKS = ConcurrentHashMap.newKeySet();

    // Block.UPDATE_IMMEDIATE (8): asks the renderer to rebuild the chunk right away, when the
    // "Chunk Updates" video setting allows it, so the real block disappears/reappears without a gap.
    private static final int UPDATE_IMMEDIATE = 8;

    public static void addAnimation(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState, AnimationModel animationModel, boolean loopable) {

        BlockPos pos = blockPos.immutable();

        ANIMATED_BLOCKS.put(pos, new BlockAnimation(clientLevel, pos, blockState, animationModel, loopable));
        setBlockInvisible(clientLevel, pos, blockState);
    }

    /**
     * Keeps a looping (mining) animation running while the player is still mining the block.
     * @return true if there was a loopable animation to keep alive
     */
    public static boolean keepAlive(BlockPos blockPos) {

        BlockAnimation blockAnimation = ANIMATED_BLOCKS.get(blockPos);

        if (blockAnimation != null && blockAnimation.isLoopable()) {

            blockAnimation.keepAlive();
            return true;
        }

        return false;
    }

    public static void removeAnimation(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState) {

        ANIMATED_BLOCKS.remove(blockPos);
        setBlockVisible(clientLevel, blockPos, blockState);
    }

    @Nullable
    public static BlockAnimation getAnimation(BlockPos blockPos) {

        return ANIMATED_BLOCKS.get(blockPos);
    }

    public static void tick(ClientLevel clientLevel) {

        // Drop animations left over from a previous level (e.g. after changing dimension).
        ANIMATED_BLOCKS.values().removeIf(blockAnimation -> {

            if (blockAnimation.getLevel() != clientLevel) {

                INVISIBLE_BLOCKS.clear();
                return true;
            }

            return false;
        });

        ANIMATED_BLOCKS.values().forEach(BlockAnimation::tick);
        ANIMATED_BLOCKS.values().removeIf(Predicate.not(BlockAnimation::isAlive));
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 cameraPos, float partialTick) {

        ANIMATED_BLOCKS.values().forEach(blockAnimation -> blockAnimation.submit(poseStack, submitNodeCollector, cameraPos, partialTick));
    }

    /**
     * Applies the current animation transform of the block at {@code blockPos} (if any) to {@code poseStack},
     * which must already be translated to that block's origin.
     */
    public static void applyAnimation(PoseStack poseStack, BlockPos blockPos) {

        if (isBlockInvisible(blockPos)) {

            BlockAnimation blockAnimation = getAnimation(blockPos);

            if (blockAnimation != null) {

                blockAnimation.getAnimationModel().apply(poseStack, blockAnimation.getProgress(getPartialTick()));
            }
        }
    }

    public static ModelFeatureRenderer.@Nullable CrumblingOverlay applyAnimation(ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, BlockPos blockPos) {

        if (crumblingOverlay == null || !isBlockInvisible(blockPos) || getAnimation(blockPos) == null) {

            return crumblingOverlay;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(crumblingOverlay.cameraPose().pose());
        poseStack.last().normal().set(crumblingOverlay.cameraPose().normal());

        applyAnimation(poseStack, blockPos);

        return new ModelFeatureRenderer.CrumblingOverlay(crumblingOverlay.progress(), poseStack.last());
    }

    public static float getPartialTick() {

        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public static void setBlockInvisible(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState) {

        if (INVISIBLE_BLOCKS.add(blockPos.immutable())) {

            updateBlock(clientLevel, blockPos, blockState);
        }
    }

    public static void setBlockVisible(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState) {

        if (INVISIBLE_BLOCKS.remove(blockPos)) {

            updateBlock(clientLevel, blockPos, blockState);
        }
    }

    public static boolean isBlockInvisible(BlockPos blockPos) {

        return !INVISIBLE_BLOCKS.isEmpty() && INVISIBLE_BLOCKS.contains(blockPos);
    }

    private static void updateBlock(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState) {

        // Marks the section dirty so it is re-meshed with/without the block.
        clientLevel.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS | UPDATE_IMMEDIATE);
    }
}
