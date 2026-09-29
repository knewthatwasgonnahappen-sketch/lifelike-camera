package com.knewthatwasgonnahappen.lifelikecamera.client;

import com.knewthatwasgonnahappen.lifelikecamera.LifelikeCameraMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class RealWalkController {
    private static final double PLAYER_SPEED_TO_MPS = 43.17D;
    private static final double TARGET_WALK_MPS = 1.0D;

    private static final KeyMapping REAL_WALK_KEY = new KeyMapping(
            "key.lifelikecamera.real_walk",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.categories.lifelikecamera");

    private RealWalkController() {}

    public static boolean isActive() {
        return REAL_WALK_KEY.isDown();
    }

    public static void registerKeyMapping(
            net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
        event.register(REAL_WALK_KEY);
    }

    @Mod.EventBusSubscriber(
            modid = LifelikeCameraMod.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static final class ForgeEvents {
        private ForgeEvents() {}

        @SubscribeEvent
        public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
            if (!isActive()) {
                return;
            }

            if (!(event.getEntity() instanceof LocalPlayer player) || !isSupported(player)) {
                return;
            }

            float scale = resolveWalkScale(player);
            if (scale >= 0.999F) {
                return;
            }

            Input input = event.getInput();
            input.leftImpulse *= scale;
            input.forwardImpulse *= scale;
            player.setSprinting(false);
        }
    }

    private static boolean isSupported(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == player
                && mc.screen == null
                && !player.isSpectator()
                && !player.isPassenger()
                && !player.isSleeping()
                && !player.isFallFlying()
                && !player.isAutoSpinAttack()
                && !player.getAbilities().flying;
    }

    private static float resolveWalkScale(LocalPlayer player) {
        double speed = player.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                * PLAYER_SPEED_TO_MPS;

        if (player.isCrouching()) {
            speed *= 0.3D;
        }

        if (speed <= TARGET_WALK_MPS) {
            return 1.0F;
        }

        return (float) Math.max(0.0D, Math.min(1.0D, TARGET_WALK_MPS / speed));
    }
}
