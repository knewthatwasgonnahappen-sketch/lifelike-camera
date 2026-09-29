package com.knewthatwasgonnahappen.lifelikecamera.client;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CameraConfig {
    private CameraConfig() {}

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.DoubleValue WALK_SWAY;
    public static final ForgeConfigSpec.DoubleValue VERTICAL_BOB;
    public static final ForgeConfigSpec.DoubleValue LATERAL_SWAY;
    public static final ForgeConfigSpec.DoubleValue YAW_SWAY;
    public static final ForgeConfigSpec.DoubleValue PITCH_TILT;
    public static final ForgeConfigSpec.DoubleValue CROUCH;
    public static final ForgeConfigSpec.DoubleValue JUMP_LANDING;
    public static final ForgeConfigSpec.DoubleValue SPEED_SENSITIVITY;
    public static final ForgeConfigSpec.DoubleValue TACZ_ADS_EFFECT;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("camera");
        ENABLED = b.comment("Replace vanilla first-person camera bob with Lifelike Camera.")
                .define("enabled", true);
        WALK_SWAY = multiplier(b, "walk_sway_strength", "Overall walking motion.");
        VERTICAL_BOB = multiplier(b, "vertical_bob_strength", "Up-and-down walking motion.");
        LATERAL_SWAY = multiplier(b, "lateral_sway_strength", "Side-to-side body sway.");
        YAW_SWAY = multiplier(b, "yaw_sway_strength", "Left-right camera rotation from movement and turns.");
        PITCH_TILT = multiplier(b, "pitch_tilt_strength", "Forward/back camera tilt from movement and impacts.");
        CROUCH = multiplier(b, "crouch_strength", "How strongly crouching changes the camera pose.");
        JUMP_LANDING = multiplier(b, "jump_landing_strength", "Jump lift and landing impact.");
        SPEED_SENSITIVITY = multiplier(b, "speed_sensitivity", "How strongly movement speed changes camera motion.");
        b.pop();

        b.push("compatibility");
        TACZ_ADS_EFFECT = b.comment("How much camera motion remains while aiming in TACZ. 0 = off, 100 = full motion.")
                .defineInRange("tacz_ads_effect_strength", 20.0D, 0.0D, 100.0D);
        b.pop();

        SPEC = b.build();
    }

    private static ForgeConfigSpec.DoubleValue multiplier(
            ForgeConfigSpec.Builder b, String key, String comment) {
        return b.comment(comment).defineInRange(key, 1.0D, 0.0D, 3.0D);
    }

    public static volatile boolean enabled = true;
    public static volatile double walkSway = 1.0D;
    public static volatile double verticalBob = 1.0D;
    public static volatile double lateralSway = 1.0D;
    public static volatile double yawSway = 1.0D;
    public static volatile double pitchTilt = 1.0D;
    public static volatile double crouch = 1.0D;
    public static volatile double jumpLanding = 1.0D;
    public static volatile double speedSensitivity = 1.0D;
    public static volatile double taczAdsEffect = 0.20D;

    public static void reloadCachedValues() {
        enabled = ENABLED.get();
        walkSway = WALK_SWAY.get();
        verticalBob = VERTICAL_BOB.get();
        lateralSway = LATERAL_SWAY.get();
        yawSway = YAW_SWAY.get();
        pitchTilt = PITCH_TILT.get();
        crouch = CROUCH.get();
        jumpLanding = JUMP_LANDING.get();
        speedSensitivity = SPEED_SENSITIVITY.get();
        taczAdsEffect = TACZ_ADS_EFFECT.get() * 0.01D;
    }

    public static double taczBlend() {
        return taczAdsEffect;
    }
}
