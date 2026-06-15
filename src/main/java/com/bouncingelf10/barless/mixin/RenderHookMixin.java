package com.bouncingelf10.barless.mixin;

import com.bouncingelf10.barless.hud.MoveWindowBar;
import com.bouncingelf10.barless.hud.TopButtons;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class RenderHookMixin {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lnet/minecraft/client/gui/GuiGraphics;)V"
            )
    )
    private void barless$beforeGuiFlush(float f, long l, boolean bl, CallbackInfo ci, @Local GuiGraphics guiGraphics) {
        TopButtons.renderAndHandle(guiGraphics);
        MoveWindowBar.renderAndHandle(guiGraphics);
    }
}