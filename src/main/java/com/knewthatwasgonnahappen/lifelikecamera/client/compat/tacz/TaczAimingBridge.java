package com.knewthatwasgonnahappen.lifelikecamera.client.compat.tacz;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.loading.LoadingModList;

public final class TaczAimingBridge {
    private static final boolean TACZ_LOADED =
            LoadingModList.get().getModFileById("tacz") != null;

    private TaczAimingBridge() {}

    public static float getProgress(LocalPlayer player, float partialTick) {
        if (!TACZ_LOADED || !(player instanceof TaczAimingAccess access)) {
            return 0.0F;
        }

        try {
            return Mth.clamp(access.lifelike$getTaczAimingProgress(partialTick), 0.0F, 1.0F);
        } catch (Throwable ignored) {
            return 0.0F;
        }
    }
}
