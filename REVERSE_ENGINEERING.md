# Reverse-engineering notes: Natural Camera 0.0.4-a

Source examined: the supplied `naturalcamera-0.0.4-a` Forge 1.20.1 jar.

## Runtime path

The camera is driven by a procedural state object that receives:

- local horizontal velocity
- local horizontal acceleration
- grounded state
- vertical velocity
- fall distance
- selected movement speed
- yaw rate
- crouch / item-use state
- a sprint/Real-Walk speed modifier

The camera then produces position, pitch/yaw/roll, and FOV offsets.

Vanilla `bobView` and `bobHurt` are cancelled when the custom camera is active. The same transform is applied to first-person held-item rendering.

## Important original behavior recovered

- Frame time is clamped to roughly 1/240–1/20 second.
- Horizontal movement is rotated into camera-local space from player yaw.
- Selected movement speed is based on the movement-speed attribute, with sprint and crouch multipliers.
- Walking uses a phase oscillator whose rate grows with movement weight.
- Gait energy is smoothed separately from the phase.
- Jump start adds a velocity-dependent kick.
- Landing adds impact based on downward velocity and fall distance.
- Falls below about -8 m/s arm a harder-impact state.
- Footplant impulses occur around two phase positions.
- Body lag, position, rotation and FOV are all spring-smoothed.
- TACZ aiming scales the resulting effect according to client aiming progress.
- Real Walk uses Left Alt and scales movement input toward about 1 m/s.

## Rewrite decisions

The rewrite intentionally preserves the *effect classes* rather than copying every expensive expression literally.

### Removed hot-loop vector allocation

The original frame preparation performed scaled/rotated/subtracted `Vec3` operations. The rewrite reads `DeltaMovement` once, rotates its x/z components into scalar doubles, and keeps previous x/z values as fields.

### Removed general-purpose trig from the render loop

A 1024-sample sine table with linear interpolation is initialized once. Gait motion, idle motion and the yaw-space velocity transform use the table instead of `Math.sin` / `Math.cos` calls per frame.

### Removed per-frame exponential decay

The original used exponential smoothing/decay in several places. The rewrite uses the rational approximation:

`1 - exp(-r*dt) ~= (r*dt)/(1+r*dt)`

This is intentionally approximate; it is used because the target is a cheap procedural camera, not a numerically exact simulation.

### Scalar springs

The rewrite keeps the original spring idea but stores x/y/z state directly instead of allocating vector objects for intermediate targets.

### Reusable output pose

`CameraPose` is mutable and owned by the client camera instance. The camera update therefore does not allocate a record plus two `Vec3` objects every render frame.

### Cached configuration

Cloth Config writes into the normal Forge client config, then the values are copied into volatile primitive fields. The render path reads those primitive fields instead of repeatedly traversing `ForgeConfigSpec` values.

## Accuracy note

The source supplied for reverse engineering is a compiled mod build, so this document records recovered behavior and the rewrite's intended equivalence. The new implementation is not claimed to be bit-for-bit numerically identical to the original. A real Minecraft run is required for visual comparison and FPS measurement.
