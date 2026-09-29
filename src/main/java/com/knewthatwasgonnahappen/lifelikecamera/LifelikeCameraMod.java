package com.knewthatwasgonnahappen.lifelikecamera;

import com.knewthatwasgonnahappen.lifelikecamera.client.CameraConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod(LifelikeCameraMod.MOD_ID)
public final class LifelikeCameraMod {
    public static final String MOD_ID = "lifelikecamera";

    public LifelikeCameraMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CameraConfig.SPEC);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ConfigEvents {
        private ConfigEvents() {}

        @SubscribeEvent
        public static void onConfigLoad(ModConfigEvent.Loading event) {
            if (event.getConfig().getSpec() == CameraConfig.SPEC) {
                CameraConfig.reloadCachedValues();
            }
        }

        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            if (event.getConfig().getSpec() == CameraConfig.SPEC) {
                CameraConfig.reloadCachedValues();
            }
        }
    }
}
