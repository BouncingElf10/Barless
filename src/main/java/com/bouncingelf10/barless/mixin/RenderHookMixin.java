package com.bouncingelf10.barless.mixin;

import com.bouncingelf10.barless.hud.MoveWindowBar;
import com.bouncingelf10.barless.hud.TopButtons;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class RenderHookMixin {
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void barless$beforeGuiFlush(float f, long l, boolean bl, CallbackInfo ci) {
        PoseStack poseStack = new PoseStack();

        TopButtons.renderAndHandle(poseStack);
        MoveWindowBar.renderAndHandle(poseStack);
    }
}