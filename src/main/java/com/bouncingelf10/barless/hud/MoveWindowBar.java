package com.bouncingelf10.barless.hud;

import com.bouncingelf10.barless.BarlessClient;
import com.bouncingelf10.barless.WindowDragLock;
import com.bouncingelf10.barless.mixin.accessor.WindowAccessor;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.sdl.SDLVideo;

public class MoveWindowBar {
    private static final int BUTTON_PADDING_SIDE = 6;
    private static final int BUTTON_PADDING_TOP = 4;

    private static final int EDGE_PADDING = 8;
    private static final int GAP_FROM_BUTTONS = 8;

    private static final int BAR_HEIGHT = 4;
    private static final int BAR_COLOR = 0x55FFFFFF;

    private static float slide = 0f;
    private static long lastTime = System.currentTimeMillis();

    private static final int SLIDE_TRIGGER_Y = BUTTON_PADDING_TOP + TopButtons.BUTTON_H + 8;
    private static final int SLIDE_DISTANCE = BUTTON_PADDING_TOP + TopButtons.BUTTON_H;

    private static boolean hovered = false;
    private static boolean dragging = false;

    private static double dragStartMouseX;
    private static double dragStartMouseY;

    public static void renderAndHandle(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        WindowAccessor acc = (WindowAccessor) (Object) window;
        if (acc.barless$isFullscreen()) return;

        long handle = acc.barless$getHandle();
        int screenW = window.getGuiScaledWidth();
        double guiScale = window.getGuiScale();

        updateSlide(handle, guiScale);
        updateHover(handle, screenW, guiScale);
        handleMouse(handle, guiScale, screenW);

        render(graphics, screenW);
    }

    private static void updateSlide(long handle, double guiScale) {
        double[] c = Sdl.cursorPos();

        int guiY = (int) (c[1] / guiScale);
        boolean nearTop = guiY <= SLIDE_TRIGGER_Y;

        long now = System.currentTimeMillis();
        float delta = (now - lastTime) * TopButtons.SLIDE_SPEED;
        lastTime = now;

        slide = nearTop ? Math.min(1f, slide + delta) : Math.max(0f, slide - delta);
    }

    private static void updateHover(long handle, int screenW, double guiScale) {
        double[] c = Sdl.cursorPos();

        int guiX = (int) (c[0] / guiScale);
        int guiY = (int) (c[1] / guiScale);

        int startX;
        int endX;

        if (BarlessClient.IS_MAC) {
            startX = BUTTON_PADDING_SIDE + TopButtons.BUTTON_W * 3 + TopButtons.BUTTON_SPACING * 2 + GAP_FROM_BUTTONS;
            endX = screenW - EDGE_PADDING;
        } else {
            startX = EDGE_PADDING;
            endX = screenW - BUTTON_PADDING_SIDE - TopButtons.BUTTON_W * 3 - TopButtons.BUTTON_SPACING * 2 - GAP_FROM_BUTTONS;
        }

        int barY = BUTTON_PADDING_TOP + (int) getSlideOffset();

        int hitboxTop = barY - 8;
        int hitboxBottom = barY + BAR_HEIGHT + 16;

        hovered = guiX >= startX && guiX <= endX && guiY >= hitboxTop && guiY <= hitboxBottom;
    }

    private static void handleMouse(long handle, double guiScale, int screenW) {
        boolean pressed = Sdl.leftMouseDown();

        double[] c = Sdl.cursorPos();
        double mx = c[0];
        double my = c[1];

        int[] win = Sdl.windowPos(handle);

        int guiY = (int) (my / guiScale);

        int barTop = BUTTON_PADDING_TOP + (int) getSlideOffset();
        int barBottom = barTop + BAR_HEIGHT + 6;

        if (pressed) {
            if (!dragging && hovered && guiY >= barTop && guiY <= barBottom) {
                if (WindowDragLock.tryAcquire(WindowDragLock.Owner.MOVE)) {
                    dragging = true;
                    dragStartMouseX = mx;
                    dragStartMouseY = my;
                }
            }
            if (dragging && WindowDragLock.isHeldBy(WindowDragLock.Owner.MOVE)) {
                SDLVideo.SDL_SetWindowPosition(handle, (int) (win[0] + (mx - dragStartMouseX)), (int) (win[1] + (my - dragStartMouseY)));
            }
        } else {
            if (dragging) {
                WindowDragLock.release(WindowDragLock.Owner.MOVE);
            }
            dragging = false;
        }
    }

    private static void render(GuiGraphicsExtractor graphics, int screenW) {
        int startX;
        int endX;

        if (BarlessClient.IS_MAC) {
            startX = BUTTON_PADDING_SIDE + TopButtons.BUTTON_W * 3 + TopButtons.BUTTON_SPACING * 2 + GAP_FROM_BUTTONS;
            endX = screenW - EDGE_PADDING;
        } else {
            startX = EDGE_PADDING;
            endX = screenW - BUTTON_PADDING_SIDE - TopButtons.BUTTON_W * 3 - TopButtons.BUTTON_SPACING * 2 - GAP_FROM_BUTTONS;
        }

        int width = endX - startX;
        if (width <= 0) return;

        float offsetY = getSlideOffset();

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(0f, offsetY);
        int y = BUTTON_PADDING_TOP + TopButtons.BUTTON_H / 2;

        graphics.fill(startX, y, startX + width, y + BAR_HEIGHT, BAR_COLOR);

        pose.popMatrix();
    }

    private static float getSlideOffset() {
        float eased = TopButtons.easeInOutCubic(slide);
        return -(1f - eased) * SLIDE_DISTANCE;
    }
}