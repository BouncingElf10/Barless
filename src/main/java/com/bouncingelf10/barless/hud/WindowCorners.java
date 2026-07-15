package com.bouncingelf10.barless.hud;

import com.bouncingelf10.barless.mixin.accessor.WindowAccessor;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

public class WindowCorners {

    private static Boolean lastRounded = null;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        boolean rounded = !window.isFullscreen();
        if (lastRounded != null && lastRounded == rounded) return;
        lastRounded = rounded;

        long handle = ((WindowAccessor) (Object) window).barless$getHandle();
        DwmUtil.setRoundedCornersAndShadow(handle, rounded);
    }
}
