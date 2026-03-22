package com.paletiers.tiertagger.client.mixin;

import com.paletiers.tiertagger.PaleTiers;
import com.paletiers.tiertagger.client.render.TierHudRenderer;
import com.paletiers.tiertagger.version.compat.CompatBridgeFactory;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class EntityRendererMixin {

    private static boolean renderErrorLogged = false;
    private static boolean disableTierRender = false;

    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("TAIL"),
        require = 0
    )
    private void onRenderLabel(@Coerce Object renderLabelContext, Text text, MatrixStack matrices,
                               VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (disableTierRender) {
            return;
        }

        if (renderLabelContext == null) {
            return;
        }

        try {
            if (renderLabelContext instanceof AbstractClientPlayerEntity player) {
                TierHudRenderer.renderTierAboveNametag(player, matrices, vertexConsumers, light);
                return;
            }

            String playerName = CompatBridgeFactory.client().resolvePlayerName(renderLabelContext);
            if (playerName != null) {
                TierHudRenderer.renderTierAboveNametag(playerName, matrices, vertexConsumers, light);
            }
        } catch (Exception | LinkageError e) {
            disableTierRender = true;
            if (!renderErrorLogged) {
                renderErrorLogged = true;
                PaleTiers.LOGGER.error("Error rendering tier label (tier rendering disabled, further errors suppressed): {}", e.getMessage(), e);
            }
        }
    }

    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/class_10055;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
        at = @At("TAIL"),
        require = 0
    )
    private void onRenderLabelState(CallbackInfo ci,
                                    @Local(argsOnly = true, ordinal = 0) Object renderLabelState,
                                    @Local(argsOnly = true, ordinal = 1) MatrixStack matrices,
                                    @Local(argsOnly = true, ordinal = 2) Object vertexConsumersArg) {
        if (disableTierRender || renderLabelState == null) {
            return;
        }

        try {
            String playerName = CompatBridgeFactory.client().resolvePlayerName(renderLabelState);
            if (playerName == null || !(vertexConsumersArg instanceof VertexConsumerProvider vertexConsumers)) {
                return;
            }
            // New label pipeline does not pass legacy light int directly; use full-bright fallback.
            TierHudRenderer.renderTierAboveNametag(playerName, matrices, vertexConsumers, 0xF000F0);
        } catch (Exception | LinkageError e) {
            disableTierRender = true;
            if (!renderErrorLogged) {
                renderErrorLogged = true;
                PaleTiers.LOGGER.error("Error rendering tier label (tier rendering disabled, further errors suppressed): {}", e.getMessage(), e);
            }
        }
    }
}
