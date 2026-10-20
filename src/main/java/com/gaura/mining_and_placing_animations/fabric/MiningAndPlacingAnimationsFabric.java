package com.gaura.mining_and_placing_animations.fabric;

import com.gaura.mining_and_placing_animations.MiningAndPlacingAnimations;
import com.gaura.mining_and_placing_animations.animation.BlockAnimationManager;
import com.gaura.mining_and_placing_animations.animation.data.AnimationResourceManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public final class MiningAndPlacingAnimationsFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        MiningAndPlacingAnimations.init();

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.fromNamespaceAndPath(MiningAndPlacingAnimations.MOD_ID, "animation_loader"), new AnimationResourceManager());

        FabricLoader.getInstance().getModContainer(MiningAndPlacingAnimations.MOD_ID).ifPresent(container -> {
            ResourceLoader.registerBuiltinPack(
                    Identifier.fromNamespaceAndPath(MiningAndPlacingAnimations.MOD_ID, "default_animations"),
                    container,
                    Component.literal("Default Mining & Placing Animations"),
                    PackActivationType.DEFAULT_ENABLED
            );
        });

        ClientTickEvents.END_LEVEL_TICK.register(BlockAnimationManager::tick);

        // Animated blocks are submitted alongside entities/block entities; the feature renderer draws them.
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> BlockAnimationManager.submit(
                context.poseStack(),
                context.submitNodeCollector(),
                context.levelState().cameraRenderState.pos,
                BlockAnimationManager.getPartialTick()
        ));
    }
}
