package com.knewthatwasgonnahappen.lifelikecamera.mixin.client.compat.tacz;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.knewthatwasgonnahappen.lifelikecamera.client.compat.tacz.TaczAimingAccess;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements TaczAimingAccess {
    @Override
    public float lifelike$getTaczAimingProgress(float partialTick) {
        return getClientAimingProgress(partialTick);
    }

    @Shadow
    public abstract float getClientAimingProgress(float partialTick);
}
