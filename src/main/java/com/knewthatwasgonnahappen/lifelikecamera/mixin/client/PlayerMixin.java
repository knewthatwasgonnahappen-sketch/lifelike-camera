package com.knewthatwasgonnahappen.lifelikecamera.mixin.client;

import com.knewthatwasgonnahappen.lifelikecamera.client.LifelikeCameraClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "causeFallDamage", at = @At("HEAD"))
    private void lifelike$recordFallDistance(
            float fallDistance,
            float multiplier,
            DamageSource source,
            CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (player instanceof LocalPlayer localPlayer
                && localPlayer.level().isClientSide) {
            LifelikeCameraClient.INSTANCE.recordFallDistance(fallDistance);
        }
    }
}
