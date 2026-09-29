package com.knewthatwasgonnahappen.lifelikecamera.client;

final class Spring3 {
    double x;
    double y;
    double z;

    double vx;
    double vy;
    double vz;

    void reset() {
        x = y = z = vx = vy = vz = 0.0D;
    }

    void update(
            double tx, double ty, double tz,
            double dt, double stiffness, double damping) {
        double ax = (tx - x) * stiffness - vx * damping;
        double ay = (ty - y) * stiffness - vy * damping;
        double az = (tz - z) * stiffness - vz * damping;

        vx += ax * dt;
        vy += ay * dt;
        vz += az * dt;

        x += vx * dt;
        y += vy * dt;
        z += vz * dt;
    }
}
