package com.bouncingelf10.barless.mixin;

import com.bouncingelf10.barless.BarlessClient;
import com.bouncingelf10.barless.hud.MoveWindowBar;
import com.bouncingelf10.barless.hud.TopButtons;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GameRenderer.class)
public class RenderHookMixin {
    @Shadow
    @Final
    GuiRenderState guiRenderState;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"
            )
    )
    private void barless$beforeGuiFlush(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci, @Local GuiGraphics guiGraphics) {
        guiGraphics.nextStratum();
        TopButtons.renderAndHandle(guiGraphics);
        MoveWindowBar.renderAndHandle(guiGraphics);
    }
}