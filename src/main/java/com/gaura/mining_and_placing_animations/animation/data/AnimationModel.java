package com.gaura.mining_and_placing_animations.animation.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AnimationModel {

    /** How values move between keyframes. */
    public enum Interpolation {
        /** Straight lines between keyframes (the original behaviour). */
        LINEAR,
        /** A Catmull-Rom style curve through the keyframes, so motion doesn't snap at each keyframe. */
        SMOOTH
    }

    /** Remaps the animation's overall progress, e.g. to start fast and settle gently. */
    public enum Easing {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT;

        public float apply(float x) {

            return switch (this) {
                case LINEAR -> x;
                case EASE_IN -> x * x * x;
                case EASE_OUT -> 1.0F - (1.0F - x) * (1.0F - x) * (1.0F - x);
                case EASE_IN_OUT -> x < 0.5F ? 4.0F * x * x * x : 1.0F - (float) Math.pow(-2.0F * x + 2.0F, 3) / 2.0F;
            };
        }
    }

    // Channels: translation xyz, rotation xyz, scale xyz.
    private static final int CHANNELS = 9;

    public final float duration;
    public final float[] pivotPoint;
    public final List<Keyframe> keyframes;
    public final Interpolation interpolation;
    public final Easing easing;

    private final float[] times;
    private final float[][] values;
    private final float[][] tangents;

    private AnimationModel(float duration, float[] pivotPoint, List<Keyframe> keyframes, Interpolation interpolation, Easing easing) {

        this.duration = duration;
        this.pivotPoint = pivotPoint;
        this.keyframes = keyframes;
        this.interpolation = interpolation;
        this.easing = easing;

        int count = keyframes.size();

        this.times = new float[count];
        this.values = new float[count][];
        this.tangents = new float[count][CHANNELS];

        for (int i = 0; i < count; i++) {

            Keyframe keyframe = keyframes.get(i);

            this.times[i] = keyframe.time();
            this.values[i] = new float[]{
                    keyframe.translation()[0], keyframe.translation()[1], keyframe.translation()[2],
                    keyframe.rotation()[0], keyframe.rotation()[1], keyframe.rotation()[2],
                    keyframe.scale()[0], keyframe.scale()[1], keyframe.scale()[2]
            };
        }

        // Tangents (rate of change) at each keyframe: central difference inside, one-sided at the ends.
        // With only two keyframes this reduces exactly to linear motion.
        for (int i = 0; i < count; i++) {

            int before = Math.max(0, i - 1);
            int after = Math.min(count - 1, i + 1);
            float dt = this.times[after] - this.times[before];

            for (int c = 0; c < CHANNELS; c++) {

                this.tangents[i][c] = dt > 0.0F ? (this.values[after][c] - this.values[before][c]) / dt : 0.0F;
            }
        }
    }

    public static AnimationModel fromJson(JsonObject jsonObject) {

        float duration = jsonObject.get("duration").getAsFloat();

        JsonArray pivot = jsonObject.getAsJsonArray("pivot_point");
        float[] pivotPoint = new float[]{ pivot.get(0).getAsFloat(), pivot.get(1).getAsFloat(), pivot.get(2).getAsFloat() };

        List<Keyframe> keyframes = new ArrayList<>();

        for (JsonElement jsonElement : jsonObject.getAsJsonArray("keyframes")) {

            keyframes.add(Keyframe.fromJson(jsonElement.getAsJsonObject()));
        }

        if (keyframes.isEmpty()) {

            throw new IllegalArgumentException("Animation has no keyframes");
        }

        keyframes.sort(Comparator.comparingDouble(Keyframe::time));

        Interpolation interpolation = jsonObject.has("interpolation")
                ? Interpolation.valueOf(jsonObject.get("interpolation").getAsString().toUpperCase(Locale.ROOT))
                : Interpolation.SMOOTH;

        Easing easing = jsonObject.has("easing")
                ? Easing.valueOf(jsonObject.get("easing").getAsString().toUpperCase(Locale.ROOT))
                : Easing.LINEAR;

        return new AnimationModel(duration, pivotPoint, keyframes, interpolation, easing);
    }

    public void apply(PoseStack poseStack, float progress) {

        float[] v = this.sample(this.easing.apply(Math.clamp(progress, 0.0F, 1.0F)) * this.duration);

        float px = this.pivotPoint[0];
        float py = this.pivotPoint[1];
        float pz = this.pivotPoint[2];

        poseStack.translate(px + v[0], py + v[1], pz + v[2]);
        poseStack.mulPose(Axis.XP.rotationDegrees(v[3]));
        poseStack.mulPose(Axis.YP.rotationDegrees(v[4]));
        poseStack.mulPose(Axis.ZP.rotationDegrees(v[5]));
        poseStack.scale(v[6], v[7], v[8]);
        poseStack.translate(-px, -py, -pz);
    }

    private float[] sample(float time) {

        int last = this.times.length - 1;

        if (last == 0 || time <= this.times[0]) {

            return this.values[0];
        }

        if (time >= this.times[last]) {

            return this.values[last];
        }

        int i = 0;

        while (i < last - 1 && time > this.times[i + 1]) {

            i++;
        }

        float t0 = this.times[i];
        float t1 = this.times[i + 1];
        float dt = t1 - t0;

        if (dt <= 0.0F) {

            return this.values[i + 1];
        }

        float s = (time - t0) / dt;
        float[] from = this.values[i];
        float[] to = this.values[i + 1];
        float[] result = new float[CHANNELS];

        if (this.interpolation == Interpolation.LINEAR) {

            for (int c = 0; c < CHANNELS; c++) {

                result[c] = from[c] + (to[c] - from[c]) * s;
            }

            return result;
        }

        // Cubic Hermite between the two keyframes using the precomputed tangents.
        float s2 = s * s;
        float s3 = s2 * s;
        float h00 = 2.0F * s3 - 3.0F * s2 + 1.0F;
        float h10 = s3 - 2.0F * s2 + s;
        float h01 = -2.0F * s3 + 3.0F * s2;
        float h11 = s3 - s2;

        float[] m0 = this.tangents[i];
        float[] m1 = this.tangents[i + 1];

        for (int c = 0; c < CHANNELS; c++) {

            result[c] = h00 * from[c] + h10 * dt * m0[c] + h01 * to[c] + h11 * dt * m1[c];
        }

        return result;
    }
}
