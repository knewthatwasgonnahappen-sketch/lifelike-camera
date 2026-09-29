package com.knewthatwasgonnahappen.lifelikecamera.client;

import com.knewthatwasgonnahappen.lifelikecamera.client.compat.tacz.TaczAimingBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class LifelikeCameraClient {
    public static final LifelikeCameraClient INSTANCE = new LifelikeCameraClient();

    private static final double PLAYER_SPEED_TO_MPS = 43.17D;

    private final OptimizedCamera camera = new OptimizedCamera();

    private final CameraPose pose = new CameraPose();
    private long lastFrameTimeNs = Long.MIN_VALUE;
    private double previousLocalX;
    private double previousLocalZ;

    private float lastYaw;
    private float pendingFallDistance;
    private boolean hasLastYaw;
    private boolean hasLastGrounded;
    private boolean lastGrounded;
    private boolean active;

    private LifelikeCameraClient() {}

    public boolean isActive(Entity entity, float partialTick) {
        captureFrame(entity, partialTick);
        return active;
    }

    public CameraPose getActivePose(Entity entity, float partialTick) {
        captureFrame(entity, partialTick);
        return active ? pose : null;
    }

    public void reset() {
        pose.reset();
        camera.reset();
        previousLocalX = 0.0D;
        previousLocalZ = 0.0D;
        lastFrameTimeNs = Long.MIN_VALUE;
        pendingFallDistance = 0.0F;
        lastYaw = 0.0F;
        hasLastYaw = false;
        hasLastGrounded = false;
        lastGrounded = false;
        active = false;
    }

    public void recordFallDistance(float distance) {
        if (distance > 0.0F) {
            pendingFallDistance = Math.max(pendingFallDistance, distance);
        }
    }

    public void applyHandPose(com.mojang.blaze3d.vertex.PoseStack stack, CameraPose cameraPose) {
        stack.translate(cameraPose.positionX, cameraPose.positionZ, -cameraPose.positionY);
        stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) cameraPose.yaw));
        stack.mulPose(com.mojang.math.Axis.XP.rotationDegrees((float) cameraPose.pitch));
        stack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) cameraPose.roll));
    }

    private void captureFrame(Entity entity, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        long frame = mc.getFrameTimeNs();
        if (frame == lastFrameTimeNs) {
            return;
        }
        lastFrameTimeNs = frame;

        if (!(entity instanceof LocalPlayer player)
                || !isSupportedPlayer(mc, player, entity)
                || !CameraConfig.enabled) {
            reset();
            lastFrameTimeNs = frame;
            return;
        }

        active = true;

        float yaw = player.getViewYRot(partialTick);
        net.minecraft.world.phys.Vec3 delta = player.getDeltaMovement();
        double worldX = delta.x * PLAYER_SPEED_TO_MPS;
        double worldZ = delta.z * PLAYER_SPEED_TO_MPS;

        double dt = Mth.clamp(mc.getDeltaFrameTime() / 20.0D, 1.0D / 240.0D, 0.05D);

        double phase = yaw * (Math.PI / 180.0D);
        double sin = TrigLut.sin(phase);
        double cos = TrigLut.cos(phase);
        double localX = worldX * cos + worldZ * sin;
        double localZ = -worldX * sin + worldZ * cos;

        double accelX = (localX - previousLocalX) / dt;
        double accelZ = (localZ - previousLocalZ) / dt;
        double yawRate = 0.0D;
        if (hasLastYaw) {
            yawRate = Mth.wrapDegrees(yaw - lastYaw) / dt;
        }

        previousLocalX = localX;
        previousLocalZ = localZ;
        lastYaw = yaw;
        hasLastYaw = true;

        boolean grounded = player.onGround();
        float landingFallDistance = 0.0F;
        if (hasLastGrounded && !lastGrounded && grounded) {
            landingFallDistance = pendingFallDistance;
        }

        double selectedSpeed = resolveSelectedSpeed(player);
        boolean realWalk = RealWalkController.isActive();
        boolean speedIsEnhanced = player.isSprinting() || realWalk;
        boolean crouching = player.isCrouching();
        boolean weaponActive = player.isUsingItem() || crouching;
        CameraPose generatedPose = camera.update(
            dt,
            localX,
            localZ,
            accelX,
            accelZ,
            grounded,
            delta.y * PLAYER_SPEED_TO_MPS,
            landingFallDistance,
            selectedSpeed,
            yawRate,
            crouching,
            weaponActive,
            speedIsEnhanced);

        double adsWeight = 1.0D - TaczAimingBridge.getProgress(player, partialTick)
                * (1.0D - CameraConfig.taczBlend());
        if (adsWeight <= 0.0001D) {
            pose.reset();
        } else if (adsWeight < 0.9999D) {
            pose.scaleInPlace(adsWeight);
        }

        if (landingFallDistance > 0.0F) {
            pendingFallDistance = 0.0F;
        }

        lastGrounded = grounded;
        hasLastGrounded = true;
    }

    private static boolean isSupportedPlayer(Minecraft mc, LocalPlayer player, Entity entity) {
        return mc.level != null
                && mc.player == player
                && mc.getCameraEntity() == entity
                && mc.options.getCameraType().isFirstPerson()
                && !mc.isPaused()
                && !player.isPassenger()
                && !player.isSleeping()
                && !player.isSpectator()
                && !player.isFallFlying()
                && !player.isAutoSpinAttack()
                && !player.getAbilities().flying;
    }

    private static double resolveSelectedSpeed(LocalPlayer player) {
        double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED) * PLAYER_SPEED_TO_MPS;
        if (player.isSprinting()) {
            speed *= 1.3D;
        }
        if (player.isCrouching()) {
            speed *= 0.3D;
        }
        return Math.max(0.1D, speed);
    }


}
