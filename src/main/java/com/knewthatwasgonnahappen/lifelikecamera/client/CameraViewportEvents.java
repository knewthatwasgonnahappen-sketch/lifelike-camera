package com.knewthatwasgonnahappen.lifelikecamera.client;

import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.knewthatwasgonnahappen.lifelikecamera.LifelikeCameraMod;

@Mod.EventBusSubscriber(
        modid = LifelikeCameraMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class CameraViewportEvents {
    private CameraViewportEvents() {}

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CameraPose pose = LifelikeCameraClient.INSTANCE.getActivePose(
                event.getCamera().getEntity(), (float) event.getPartialTick());
        if (pose != null) {
            event.setRoll(event.getRoll() + (float) pose.roll);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        CameraPose pose = LifelikeCameraClient.INSTANCE.getActivePose(
                net.minecraft.client.Minecraft.getInstance().getCameraEntity(),
                (float) event.getPartialTick());
        if (pose != null) {
            event.setFOV(event.getFOV() + pose.fovOffset);
        }
    }
}
