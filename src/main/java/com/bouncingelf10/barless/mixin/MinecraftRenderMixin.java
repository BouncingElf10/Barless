package com.bouncingelf10.barless.mixin;

import com.bouncingelf10.barless.hud.ResizeBars;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MinecraftRenderMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void barless$onRenderHead(CallbackInfo ci) {
        ResizeBars.tick();
    }
}