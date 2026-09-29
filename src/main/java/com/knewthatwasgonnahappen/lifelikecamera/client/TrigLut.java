package com.knewthatwasgonnahappen.lifelikecamera.client;

final class TrigLut {
    private static final int SIZE = 1024;
    private static final double MASK = SIZE / (Math.PI * 2.0D);
    private static final double[] SIN = new double[SIZE];

    static {
        for (int i = 0; i < SIZE; i++) {
            SIN[i] = Math.sin(i * (Math.PI * 2.0D / SIZE));
        }
    }

    private TrigLut() {}

    static double sin(double phase) {
        double p = phase * MASK;
        int i = (int) p;
        double f = p - i;
        int a = i & (SIZE - 1);
        int b = (a + 1) & (SIZE - 1);
        return SIN[a] + (SIN[b] - SIN[a]) * f;
    }

    static double cos(double phase) {
        return sin(phase + Math.PI * 0.5D);
    }
}
