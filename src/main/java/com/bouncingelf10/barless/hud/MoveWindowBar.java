package com.bouncingelf10.barless.hud;

import com.bouncingelf10.barless.BarlessClient;
import com.bouncingelf10.barless.WindowDragLock;
import com.bouncingelf10.barless.mixin.accessor.WindowAccessor;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

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

    private static int prevMouseState = GLFW.GLFW_RELEASE;

    public static void renderAndHandle(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        if (window.isFullscreen()) return;

        long handle = ((WindowAccessor) (Object) window).barless$getHandle();
        int screenW = window.getGuiScaledWidth();
        double guiScale = window.getGuiScale();

        updateSlide(handle, guiScale);
        updateHover(handle, screenW, guiScale);
        handleMouse(handle, guiScale, screenW);

        render(graphics, screenW);
    }

    private static void updateSlide(long handle, double guiScale) {
        double[] cx = new double[1];
        double[] cy = new double[1];

        GLFW.glfwGetCursorPos(handle, cx, cy);

        int guiY = (int) (cy[0] / guiScale);
        boolean nearTop = guiY <= SLIDE_TRIGGER_Y;

        long now = System.currentTimeMillis();
        float delta = (now - lastTime) * TopButtons.SLIDE_SPEED;
        lastTime = now;

        slide = nearTop ? Math.min(1f, slide + delta) : Math.max(0f, slide - delta);
    }

    private static void updateHover(long handle, int screenW, double guiScale) {
        double[] cx = new double[1];
        double[] cy = new double[1];

        GLFW.glfwGetCursorPos(handle, cx, cy);

        int guiX = (int) (cx[0] / guiScale);
        int guiY = (int) (cy[0] / guiScale);

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
        int state = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT);

        double[] cx = new double[1];
        double[] cy = new double[1];
        GLFW.glfwGetCursorPos(handle, cx, cy);

        double mx = cx[0];
        double my = cy[0];

        int[] winX = new int[1];
        int[] winY = new int[1];
        GLFW.glfwGetWindowPos(handle, winX, winY);

        int guiY = (int) (my / guiScale);

        int barTop = BUTTON_PADDING_TOP + (int) getSlideOffset();
        int barBottom = barTop + BAR_HEIGHT + 6;

        if (state == GLFW.GLFW_PRESS) {
            if (!dragging && hovered && guiY >= barTop && guiY <= barBottom) {
                if (WindowDragLock.tryAcquire(WindowDragLock.Owner.MOVE)) {
                    dragging = true;
                    dragStartMouseX = mx;
                    dragStartMouseY = my;
                }
            }
            if (dragging && WindowDragLock.isHeldBy(WindowDragLock.Owner.MOVE)) {
                GLFW.glfwSetWindowPos(handle, (int) (winX[0] + (mx - dragStartMouseX)), (int) (winY[0] + (my - dragStartMouseY)));
            }
        } else {
            if (dragging) {
                WindowDragLock.release(WindowDragLock.Owner.MOVE);
            }
            dragging = false;
        }

        prevMouseState = state;
    }

    private static void render(GuiGraphics graphics, int screenW) {
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