package com.gaura.mining_and_placing_animations.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Only applies the compat mixins when the mod they target is installed.
 */
public final class MixinPlugin implements IMixinConfigPlugin {

    private static final String PACKAGE = "com.gaura.mining_and_placing_animations.mixin.";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

        FabricLoader loader = FabricLoader.getInstance();

        if (mixinClassName.startsWith(PACKAGE + "sodium.")) {

            return loader.isModLoaded("sodium");
        }

        if (mixinClassName.startsWith(PACKAGE + "iris.")) {

            return loader.isModLoaded("iris");
        }

        if (mixinClassName.startsWith(PACKAGE + "indigo.")) {

            return loader.isModLoaded("fabric-renderer-indigo");
        }

        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {

        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {

        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
