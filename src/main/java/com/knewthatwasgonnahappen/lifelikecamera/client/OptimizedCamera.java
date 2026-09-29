package com.knewthatwasgonnahappen.lifelikecamera.client;

import net.minecraft.util.Mth;

/**
 * Procedural camera rewritten around scalar state:
 * - no per-component Vec3 arithmetic inside the hot loop
 * - no exp(), hypot(), sin(), or cos() in the hot loop
 * - LUT-backed sine/cosine for gait, idle motion, and yaw transforms
 * - only the final pose vectors allocate
 */
public final class OptimizedCamera {
    private static final double TAU = Math.PI * 2.0D;
    private static final double HARD_FALL_SPEED = -8.0D;

    private final Spring3 positionSpring = new Spring3();
    private final Spring3 rotationSpring = new Spring3();
    private final Spring3 bodySpring = new Spring3();
    private final Spring1 fovSpring = new Spring1();
    private final CameraPose outputPose = new CameraPose();

    private double gaitPhase;
    private double idlePhase;
    private double gaitEnergy;
    private double jumpKick;
    private double landingKick;
    private double heavyFall;
    private double footImpact;
    private double lastVerticalVelocity;
    private boolean lastGrounded = true;
    private boolean hardFallPrimed;

    public void reset() {
        positionSpring.reset();
        rotationSpring.reset();
        bodySpring.reset();
        fovSpring.reset();

        gaitPhase = 0.0D;
        idlePhase = 0.0D;
        gaitEnergy = 0.0D;
        jumpKick = 0.0D;
        landingKick = 0.0D;
        heavyFall = 0.0D;
        footImpact = 0.0D;
        lastVerticalVelocity = 0.0D;
        lastGrounded = true;
        hardFallPrimed = false;
    }

    public CameraPose update(
            double dt,
            double localX,
            double localZ,
            double accelX,
            double accelZ,
            boolean grounded,
            double verticalVelocity,
            float fallDistance,
            double selectedSpeed,
            double yawRate,
            boolean crouching,
            boolean weaponActive,
            boolean speedIsEnhanced) {

        dt = Mth.clamp(dt, 1.0D / 240.0D, 0.05D);

        final double walk = CameraConfig.walkSway;
        final double vertical = CameraConfig.verticalBob;
        final double lateral = CameraConfig.lateralSway;
        final double yaw = CameraConfig.yawSway;
        final double pitch = CameraConfig.pitchTilt;
        final double crouch = CameraConfig.crouch;
        final double jumpLanding = CameraConfig.jumpLanding;
        double speedSensitivity = CameraConfig.speedSensitivity;

        if (!speedIsEnhanced) {
            speedSensitivity *= 0.65D;
        }

        final double speed = sqrt(localX * localX + localZ * localZ);
        final double scaledSpeed = speed * speedSensitivity;
        final double maxSpeed = Math.max(0.1D, selectedSpeed);
        final double movement = Mth.clamp(scaledSpeed / maxSpeed, 0.0D, 1.35D);

        final double gaitTarget = grounded
                ? smoothstep(0.05D, Math.max(0.24D, maxSpeed * 0.65D), movement)
                : 0.0D;

        final double gaitRate = grounded ? 12.0D : 7.0D;
        gaitEnergy += (gaitTarget - gaitEnergy) * (1.0D - fastDecay(gaitRate, dt));

        final double cycleRate = lerp(1.15D, 3.25D, movement) * gaitEnergy;
        final double previousPhase = gaitPhase;
        gaitPhase = wrap(gaitPhase + cycleRate * TAU * dt);

        idlePhase = wrap(idlePhase + TAU * (0.15D + 0.05D * (1.0D - movement)) * dt);

        if (!grounded && verticalVelocity <= HARD_FALL_SPEED) {
            hardFallPrimed = true;
        }

        if (lastGrounded && !grounded && verticalVelocity > 0.2D) {
            jumpKick = clamp(verticalVelocity * 0.01212D, 0.0202D, 0.0808D) * jumpLanding;
        }

        if (!lastGrounded && grounded) {
            final double downwardSpeed = Math.max(0.0D, -lastVerticalVelocity);
            double fallMultiplier = landingMultiplier(fallDistance);
            landingKick += clamp(downwardSpeed * 0.0202D, 0.02525D, 0.1717D)
                    * fallMultiplier * jumpLanding;

            if (hardFallPrimed) {
                heavyFall = Math.max(
                        heavyFall,
                        clamp((downwardSpeed - 6.0D) * 0.1414D, 0.0D, 1.01D)
                                * Math.max(1.0D, fallMultiplier) * jumpLanding);
            }
            hardFallPrimed = false;
        }

        if (grounded) {
            hardFallPrimed = false;
        }

        lastGrounded = grounded;
        lastVerticalVelocity = verticalVelocity;

        // Footplant is a single threshold check instead of a general-purpose phase search.
        if (grounded && movement > 0.16D
                && (crossed(previousPhase, gaitPhase, 0.24D)
                || crossed(previousPhase, gaitPhase, 3.381592653589793D))) {
            footImpact = Math.min(0.09D,
                    footImpact + 0.0102D + 0.0375D * movement + 0.0195D * gaitEnergy * walk);
        }
        footImpact = Math.max(0.0D, footImpact - footImpact * 26.0D * dt);

        final double s = TrigLut.sin(gaitPhase * 2.0D);
        final double c = TrigLut.cos(gaitPhase * 2.0D);

        final double idleS = TrigLut.sin(idlePhase);
        final double idleC = TrigLut.cos(idlePhase);

        final double squat = weaponActive ? crouch : 0.0D;
        final double crouchZ = -0.1672D * squat;
        final double crouchY = -0.055D * squat;

        final double idleWeight = (1.0D - smoothstep(0.12D, 1.0D, movement))
                * (grounded ? 1.0D : 0.0D);

        // Replace the original multi-frequency micro-noise with two LUT samples.
        final double microX = (idleS * 0.65D + idleC * 0.35D) * 0.0038D * idleWeight;
        final double microY = (idleC * 0.55D - idleS * 0.25D) * 0.0030D * idleWeight;
        final double microZ = (idleS * 0.60D + idleC * 0.20D) * 0.0024D * idleWeight;

        final double bobAmplitude = lerp(0.012D, 0.082D,
                clamp(scaledSpeed / 8.5D, 0.0D, 1.0D))
                * gaitEnergy * 2.0D * movementModifier(heavyFall);

        double verticalBob = (s - Math.abs(c) * 0.18D + TrigLut.sin(gaitPhase + 1.1D) * 0.10D)
                * bobAmplitude * 0.3232D * vertical
                * (heavyFall > 0.0D ? 0.68D : 0.82D);

        final double lateralBob = s * bobAmplitude * 0.594D * walk;
        final double forwardBob = c * bobAmplitude * 1.02D * walk;

        final double speedNorm = clamp(localX / Math.max(0.1D, selectedSpeed), -1.0D, 1.0D);
        final double speedForward = clamp(localZ / Math.max(0.1D, selectedSpeed), -1.0D, 1.0D);

        final double accelXClamped = clamp(-accelX * 0.003366D, -0.07524D, 0.07524D);
        final double accelZClamped = clamp(-accelZ * 0.0038D, -0.052D, 0.036D);

        final double bodyX = clamp(-localX * 0.01287D - accelX * 0.003366D, -0.07524D, 0.07524D);
        final double bodyY = clamp(-localZ * 0.0095D - accelZ * 0.0038D, -0.052D, 0.036D);
        final double bodyZ = clamp(-Math.abs(localZ) * 0.004D * movement, -0.01D, 0.0D);

        bodySpring.update(bodyX, bodyY, bodyZ, dt, 26.0D, 10.0D);

        double positionTargetX =
                lateralBob
                + accelXClamped * 0.8D
                + speedNorm * 0.0396D
                + microX * lateral
                + bodySpring.x;

        double positionTargetY =
                verticalBob
                + accelZClamped
                + microY
                + bodySpring.y
                + crouchY
                + (grounded ? 0.0D : clamp(verticalVelocity * 0.01D, -0.085D, 0.105D))
                + jumpKick - landingKick;

        double positionTargetZ =
                forwardBob
                + microZ
                + bodySpring.z
                + crouchZ
                - footImpact * 1.8D;

        // Impact state decays linearly; this avoids three exponential calls per frame.
        jumpKick = decay(jumpKick, 5.2D, dt);
        landingKick = decay(landingKick, 7.8D, dt);
        heavyFall = decay(heavyFall, 0.85D, dt);

        final double turnTilt = clamp(-yawRate * 0.0594D, -6.732D, 6.732D);
        final double pitchFromAccel = clamp(-accelZ * 0.1292D, -4.864D, 4.864D)
                * (grounded ? 0.0D : 1.0D);

        final double pitchTarget =
                TrigLut.sin(gaitPhase * 2.0D + 0.30D) * bobAmplitude * -109.44D * vertical * walk
                + clamp(-speedForward * 0.17D * 3.04D, -11.552D, 13.984D) * pitch
                + pitchFromAccel
                + landingKick * 45.6D
                - bodySpring.y * 27.36D
                + idleS * 0.16D * idleWeight;

        final double yawTarget =
                clamp(-accelX * 0.14D * 2.376D, -9.9792D, 9.9792D) * lateral
                + yawRate * 1.25D
                + s * bobAmplitude * 0.792D * lateral
                + bodySpring.x * 20.0D
                + turnTilt;

        final double rollTarget =
                lateralBob * 0.32D * lateral
                - speedNorm * 0.15D * lateral
                + turnTilt * 0.24D
                + footImpact * 2.0D
                + idleC * 0.08D * idleWeight;

        positionSpring.update(positionTargetX, positionTargetY, positionTargetZ, dt, 96.0D, 18.0D);
        rotationSpring.update(pitchTarget, yawTarget, rollTarget, dt, 82.0D, 16.5D);

        final double fovTarget =
                clamp(speed * 0.52D, 0.0D, 5.8D)
                + jumpKick * 12.0D
                - landingKick * 8.0D;
        final double fov = fovSpring.update(fovTarget, dt, 46.0D, 12.0D);

        return outputPose.set(
                positionSpring.x, positionSpring.y, positionSpring.z,
                rotationSpring.x, rotationSpring.y, rotationSpring.z,
                fov, movement);
    }

    private static double movementModifier(double heavy) {
        return 1.0D + heavy * 0.55D;
    }

    private static double fastDecay(double rate, double dt) {
        // First-order decay approximation: 1 - exp(-rate*dt), without exp().
        double x = rate * dt;
        return x / (1.0D + x);
    }

    private static double decay(double value, double rate, double dt) {
        return Math.max(0.0D, value - value * fastDecay(rate, dt));
    }

    private static double sqrt(double x) {
        return x > 0.0D ? Math.sqrt(x) : 0.0D;
    }

    private static double landingMultiplier(float fallDistance) {
        if (fallDistance <= 0.2F) {
            return 0.35D;
        }
        if (fallDistance < 1.0F) {
            return lerp(0.35D, 1.0D, (fallDistance - 0.2D) / 0.8D);
        }
        int blocks = Math.max(1, (int) (fallDistance + 0.25F));
        return switch (blocks) {
            case 1 -> 1.0D;
            case 2 -> 1.3D;
            case 3 -> 1.7D;
            case 4 -> 2.0D;
            case 5 -> 2.5D;
            case 6 -> 2.8D;
            case 7 -> 3.2D;
            case 8 -> 3.8D;
            case 9 -> 4.5D;
            default -> 5.0D;
        };
    }

    private static boolean crossed(double previous, double current, double phase) {
        if (current >= previous) {
            return previous < phase && current >= phase;
        }
        return previous < phase || current >= phase;
    }

    private static double smoothstep(double a, double b, double x) {
        if (a == b) {
            return x < b ? 0.0D : 1.0D;
        }
        double t = clamp((x - a) / (b - a), 0.0D, 1.0D);
        return t * t * (3.0D - 2.0D * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * clamp(t, 0.0D, 1.0D);
    }

    private static double clamp(double x, double min, double max) {
        return Mth.clamp(x, min, max);
    }

    private static double wrap(double x) {
        x %= TAU;
        return x < 0.0D ? x + TAU : x;
    }
}
