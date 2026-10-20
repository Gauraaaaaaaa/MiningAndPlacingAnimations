package com.gaura.mining_and_placing_animations.animation.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record AnimationRule(List<String> blocks, @Nullable Identifier miningAnimationId, @Nullable Identifier placingAnimationId) {

    public static AnimationRule fromJson(JsonObject jsonObject) {

        List<String> blocks = new ArrayList<>();

        for (JsonElement jsonElement : jsonObject.getAsJsonArray("blocks")) {

            blocks.add(jsonElement.getAsString());
        }

        Identifier miningAnimationId = jsonObject.has("mining_animation") ? Identifier.parse(jsonObject.get("mining_animation").getAsString()) : null;

        Identifier placingAnimationId = jsonObject.has("placing_animation") ? Identifier.parse(jsonObject.get("placing_animation").getAsString()) : null;

        return new AnimationRule(blocks, miningAnimationId, placingAnimationId);
    }
}
