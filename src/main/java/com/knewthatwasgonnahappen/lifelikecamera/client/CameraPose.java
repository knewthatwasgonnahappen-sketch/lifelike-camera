package com.knewthatwasgonnahappen.lifelikecamera.client;

/** Reusable camera result. No per-frame Vec3/record allocation. */
public final class CameraPose {
    public double positionX;
    public double positionY;
    public double positionZ;

    public double pitch;
    public double yaw;
    public double roll;

    public double fovOffset;
    public double movementWeight;

    public CameraPose set(
            double positionX, double positionY, double positionZ,
            double pitch, double yaw, double roll,
            double fovOffset, double movementWeight) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.fovOffset = fovOffset;
        this.movementWeight = movementWeight;
        return this;
    }

    public CameraPose scaleInPlace(double scale) {
        positionX *= scale;
        positionY *= scale;
        positionZ *= scale;
        pitch *= scale;
        yaw *= scale;
        roll *= scale;
        fovOffset *= scale;
        return this;
    }

    public CameraPose reset() {
        return set(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public boolean isZero() {
        return positionX == 0.0D && positionY == 0.0D && positionZ == 0.0D
                && pitch == 0.0D && yaw == 0.0D && roll == 0.0D
                && fovOffset == 0.0D;
    }
}
