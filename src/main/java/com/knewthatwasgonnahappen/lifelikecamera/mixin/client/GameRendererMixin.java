package com.knewthatwasgonnahappen.lifelikecamera.mixin.client;

import com.knewthatwasgonnahappen.lifelikecamera.client.CameraPose;
import com.knewthatwasgonnahappen.lifelikecamera.client.LifelikeCameraClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow private Minecraft minecraft;

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void lifelike$cancelVanillaBob(PoseStack stack, float partialTick, CallbackInfo ci) {
        if (minecraft.getCameraEntity() != null
                && LifelikeCameraClient.INSTANCE.getActivePose(
                minecraft.getCameraEntity(), partialTick) != null) {
            ci.cancel();
        }
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void lifelike$cancelVanillaHurtBob(PoseStack stack, float partialTick, CallbackInfo ci) {
        if (minecraft.getCameraEntity() != null
                && LifelikeCameraClient.INSTANCE.getActivePose(
                minecraft.getCameraEntity(), partialTick) != null) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    private void lifelike$applyHandPose(
            PoseStack stack, Camera camera, float partialTick, CallbackInfo ci) {
        CameraPose pose = LifelikeCameraClient.INSTANCE.getActivePose(
                camera.getEntity(), partialTick);
        if (pose != null) {
            LifelikeCameraClient.INSTANCE.applyHandPose(stack, pose);
        }
    }
}
