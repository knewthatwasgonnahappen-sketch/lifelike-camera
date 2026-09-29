package com.knewthatwasgonnahappen.lifelikecamera.client;

final class Spring1 {
    double value;
    double velocity;

    void reset() {
        value = velocity = 0.0D;
    }

    double update(double target, double dt, double stiffness, double damping) {
        velocity += ((target - value) * stiffness - velocity * damping) * dt;
        value += velocity * dt;
        return value;
    }
}
