package com.gaura.mining_and_placing_animations.animation.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public record Keyframe(float time, float[] translation, float[] rotation, float[] scale) {

    public static Keyframe fromJson(JsonObject jsonObject) {

        float time = jsonObject.get("time").getAsFloat();

        float[] translation = jsonArrayToFloats(jsonObject.getAsJsonArray("translation"));
        float[] rotation = jsonArrayToFloats(jsonObject.getAsJsonArray("rotation"));
        float[] scale = jsonArrayToFloats(jsonObject.getAsJsonArray("scale"));

        return new Keyframe(time, translation, rotation, scale);
    }

    private static float[] jsonArrayToFloats(JsonArray jsonArray) {

        return new float[]{ jsonArray.get(0).getAsFloat(), jsonArray.get(1).getAsFloat(), jsonArray.get(2).getAsFloat() };
    }
}
