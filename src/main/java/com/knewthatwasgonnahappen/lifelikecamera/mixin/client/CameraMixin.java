package com.knewthatwasgonnahappen.lifelikecamera.mixin.client;

import com.knewthatwasgonnahappen.lifelikecamera.client.CameraPose;
import com.knewthatwasgonnahappen.lifelikecamera.client.LifelikeCameraClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "setup", at = @At("TAIL"))
    private void lifelike$applyPose(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo ci) {
        if (detached) {
            return;
        }

        CameraPose pose = LifelikeCameraClient.INSTANCE.getActivePose(entity, partialTick);
        if (pose == null) {
            return;
        }

        this.move(pose.positionY, pose.positionZ, -pose.positionX);
        this.setRotation(getYRot() + (float) pose.yaw, getXRot() + (float) pose.pitch);
    }

    @Shadow
    protected abstract void move(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract float getXRot();

    @Shadow
    protected abstract float getYRot();
}
