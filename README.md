# Lifelike Camera

A Forge 1.20.1 rewrite of the Natural Camera 0.0.4-a behavior, focused on keeping the same user-facing idea while making the render hot path cheaper.

## What it does

- Replaces vanilla first-person view bob with procedural walking motion.
- Adds vertical bob, lateral sway, yaw sway, pitch/roll tilt, crouch/use-item body motion, and impact motion.
- Adds jump and landing kick.
- Adds harder-fall heaviness.
- Adds a small idle breathing/micro-motion layer.
- Applies the same camera pose to the held-item transform.
- Adds the `Real Walk` keybind on Left Alt, limiting walking input toward about 1 m/s.
- Fades the camera effect while TACZ ADS is active, with an adjustable percentage.

## Performance rewrite

The supplied 0.0.4-a build contains a large monolithic camera update with several `Math.sin`, `Math.cos`, and `Math.exp` calls plus repeated `Vec3` creation and vector arithmetic.

This rewrite changes the hot path to:

- scalar x/y/z state instead of temporary vector math
- one `Math.sqrt` for horizontal speed
- LUT-backed sine/cosine for procedural motion and yaw transforms
- rational first-order decay instead of per-frame `exp`
- scalar springs with no temporary `Vec3` objects
- one reusable camera pose; no camera-created per-frame object allocation in the update path

The goal is lower CPU cost without removing the core motion design.

## Configuration

The in-game configuration screen is provided by Cloth Config API 11.1.118 for Forge 1.20.1.

Settings are grouped into Camera Motion and Compatibility. The numeric options use bounded Cloth Config double fields; Cloth Config v11 does not expose a double-slider builder.

## Compatibility

The target runtime is Minecraft 1.20.1 with Forge 47.x. TACZ is optional; the mixin for its aiming progress is applied only when TACZ is loaded.
