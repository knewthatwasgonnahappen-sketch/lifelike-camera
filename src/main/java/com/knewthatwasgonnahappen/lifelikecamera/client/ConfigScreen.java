package com.knewthatwasgonnahappen.lifelikecamera.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ConfigScreen {
    private ConfigScreen() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.lifelikecamera.config"));

        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory camera = builder.getOrCreateCategory(
                Component.translatable("category.lifelikecamera.camera"));

        camera.addEntry(entries.startBooleanToggle(
                        Component.translatable("option.lifelikecamera.enabled"),
                        CameraConfig.ENABLED.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.lifelikecamera.enabled"))
                .setSaveConsumer(CameraConfig.ENABLED::set)
                .build());

        addNumberField(camera, entries, "walk_sway_strength",
                CameraConfig.WALK_SWAY, "tooltip.lifelikecamera.walk_sway_strength");
        addNumberField(camera, entries, "vertical_bob_strength",
                CameraConfig.VERTICAL_BOB, "tooltip.lifelikecamera.vertical_bob_strength");
        addNumberField(camera, entries, "lateral_sway_strength",
                CameraConfig.LATERAL_SWAY, "tooltip.lifelikecamera.lateral_sway_strength");
        addNumberField(camera, entries, "yaw_sway_strength",
                CameraConfig.YAW_SWAY, "tooltip.lifelikecamera.yaw_sway_strength");
        addNumberField(camera, entries, "pitch_tilt_strength",
                CameraConfig.PITCH_TILT, "tooltip.lifelikecamera.pitch_tilt_strength");
        addNumberField(camera, entries, "crouch_strength",
                CameraConfig.CROUCH, "tooltip.lifelikecamera.crouch_strength");
        addNumberField(camera, entries, "jump_landing_strength",
                CameraConfig.JUMP_LANDING, "tooltip.lifelikecamera.jump_landing_strength");
        addNumberField(camera, entries, "speed_sensitivity",
                CameraConfig.SPEED_SENSITIVITY, "tooltip.lifelikecamera.speed_sensitivity");

        ConfigCategory compatibility = builder.getOrCreateCategory(
                Component.translatable("category.lifelikecamera.compatibility"));

        compatibility.addEntry(entries.startDoubleField(
                        Component.translatable("option.lifelikecamera.tacz_ads_effect_strength"),
                        CameraConfig.TACZ_ADS_EFFECT.get())
                .setDefaultValue(20.0D)
                .setMin(0.0D)
                .setMax(100.0D)
                .setSaveConsumer(CameraConfig.TACZ_ADS_EFFECT::set)
                .setTooltip(Component.translatable("tooltip.lifelikecamera.tacz_ads_effect_strength"))
                .build());

        return builder.setSavingRunnable(() -> {
            CameraConfig.SPEC.save();
            CameraConfig.reloadCachedValues();
        }).build();
    }

    private static void addNumberField(
            ConfigCategory category,
            ConfigEntryBuilder entries,
            String key,
            net.minecraftforge.common.ForgeConfigSpec.DoubleValue value,
            String tooltip) {
        category.addEntry(entries.startDoubleField(
                        Component.translatable("option.lifelikecamera." + key),
                        value.get())
                .setDefaultValue(1.0D)
                .setMin(0.0D)
                .setMax(3.0D)
                .setSaveConsumer(value::set)
                .setTooltip(Component.translatable(tooltip))
                .build());
    }
}
