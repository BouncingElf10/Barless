package com.bouncingelf10.barless.mixin;

import com.bouncingelf10.barless.hud.MoveWindowBar;
import com.bouncingelf10.barless.hud.ResizeBars;
import com.bouncingelf10.barless.hud.TopButtons;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class RenderHookMixin {

    @Shadow @Final
    private GuiRenderState renderState;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;prepare()V"
            )
    )
    private void onBeforePrepare(GpuBufferSlice fogBuffer, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        ResizeBars.tick();

        int mouseX = (int)(mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
        int mouseY = (int)(mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

        GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(mc, this.renderState, mouseX, mouseY);

        graphics.nextStratum();

        TopButtons.renderAndHandle(graphics);
        MoveWindowBar.renderAndHandle(graphics);
    }
}