package com.bouncingelf10.barless.hud;

import com.bouncingelf10.barless.BarlessClient;
import com.bouncingelf10.barless.mixin.accessor.WindowHandleAccessor;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class TopButtons {
    private static final Identifier BUTTON = Identifier.fromNamespaceAndPath(BarlessClient.MOD_ID, "textures/gui/button.png");
    static final int BUTTON_W = 8;
    static final int BUTTON_H = 8;
    private static final int TEXTURE_W = 8;
    private static final int TEXTURE_H = 8;
    static final int BUTTON_PADDING_SIDE = 6;
    static final int BUTTON_PADDING_TOP = 6;
    static final int BUTTON_SPACING = 4;

    private static final int RED_LIGHT = 0xFFFF4444;
    private static final int GREEN_LIGHT = 0xFF44FF44;
    private static final int YELLOW_LIGHT = 0xFFFFFF44;

    private static final int RED_DARK = 0xFF772222;
    private static final int GREEN_DARK = 0xFF227722;
    private static final int YELLOW_DARK = 0xFF777722;

    private static final float FADE_SPEED = 0.008f;
    private static final float[] fade = {0f, 0f, 0f};
    private static long lastTime = System.currentTimeMillis();

    static final float SLIDE_SPEED = 0.006f;
    private static final float[] slideY = {0f, 0f, 0f};
    private static final int SLIDE_TRIGGER_Y = BUTTON_PADDING_TOP + BUTTON_H + 4;
    private static final int SLIDE_DISTANCE = BUTTON_PADDING_TOP + BUTTON_H;

    private static int prevMouseState = GLFW.GLFW_RELEASE;
    private static int hoveredButton = -1;

    public static void renderAndHandle(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        long handle = ((WindowHandleAccessor) (Object) window).getHandle();
        int screenW = window.getGuiScaledWidth();
        double guiScale = window.getGuiScale();

        updateSlideAndFade(handle, guiScale);
        updateHover(handle, screenW, guiScale);

        if (BarlessClient.IS_MAC) {
            renderMac(graphics, screenW);
        } else {
            renderWindows(graphics, screenW);
        }

        handleMouse(handle, screenW);
    }

    private static void updateSlideAndFade(long handle, double guiScale) {
        double[] cx = new double[1], cy = new double[1];
        GLFW.glfwGetCursorPos(handle, cx, cy);
        int guiY = (int) (cy[0] / guiScale);

        boolean nearTop = guiY <= SLIDE_TRIGGER_Y;

        long now = System.currentTimeMillis();
        float delta = (now - lastTime) * FADE_SPEED;
        float slideDelta = (now - lastTime) * SLIDE_SPEED;
        lastTime = now;

        for (int i = 0; i < 3; i++) {
            slideY[i] = nearTop ? Math.min(1f, slideY[i] + slideDelta) : Math.max(0f, slideY[i] - slideDelta);
            fade[i] = isHovered(i) ? Math.min(1f, fade[i] + delta) : Math.max(0f, fade[i] - delta);
        }
    }

    private static void updateHover(long handle, int screenW, double guiScale) {
        double[] cx = new double[1], cy = new double[1];
        GLFW.glfwGetCursorPos(handle, cx, cy);
        int guiX = (int) (cx[0] / guiScale);
        int guiY = (int) (cy[0] / guiScale);

        hoveredButton = -1;

        float floatOffset = getSlideOffset(0);
        int btnTop = (int) (BUTTON_PADDING_TOP + floatOffset);
        int btnBot = btnTop + BUTTON_H;

        if (guiY < btnTop || guiY >= btnBot) return;

        if (BarlessClient.IS_MAC) {
            int closeX = BUTTON_PADDING_SIDE;
            int minimizeX = closeX + BUTTON_W + BUTTON_SPACING;
            int fullscreenX = minimizeX + BUTTON_W + BUTTON_SPACING;

            if (guiX >= closeX && guiX < closeX + BUTTON_W) hoveredButton = 0;
            else if (guiX >= minimizeX && guiX < minimizeX + BUTTON_W) hoveredButton = 2;
            else if (guiX >= fullscreenX && guiX < fullscreenX + BUTTON_W) hoveredButton = 1;
        } else {
            int closeX = screenW - BUTTON_PADDING_SIDE - BUTTON_W;
            int fullscreenX = closeX - BUTTON_SPACING - BUTTON_W;
            int minimizeX = closeX - 2 * (BUTTON_SPACING + BUTTON_W);

            if (guiX >= closeX && guiX < closeX + BUTTON_W) hoveredButton = 0;
            else if (guiX >= fullscreenX && guiX < fullscreenX + BUTTON_W) hoveredButton = 1;
            else if (guiX >= minimizeX && guiX < minimizeX + BUTTON_W) hoveredButton = 2;
        }
    }

    private static boolean isHovered(int btn) {
        return hoveredButton == btn;
    }

    private static void renderMac(GuiGraphicsExtractor graphics, int screenW) {
        int closeX = BUTTON_PADDING_SIDE;
        int minimizeX = closeX + BUTTON_W + BUTTON_SPACING;
        int fullscreenX = minimizeX + BUTTON_W + BUTTON_SPACING;

        blitWithSlide(graphics, BUTTON, closeX, BUTTON_PADDING_TOP, 0, lerpColor(RED_DARK, RED_LIGHT, easeInOutCubic(fade[0])));
        blitWithSlide(graphics, BUTTON, minimizeX, BUTTON_PADDING_TOP, 2, lerpColor(YELLOW_DARK, YELLOW_LIGHT, easeInOutCubic(fade[2])));
        blitWithSlide(graphics, BUTTON, fullscreenX, BUTTON_PADDING_TOP, 1, lerpColor(GREEN_DARK, GREEN_LIGHT, easeInOutCubic(fade[1])));
    }

    private static void renderWindows(GuiGraphicsExtractor graphics, int screenW) {
        int closeX = screenW - BUTTON_PADDING_SIDE - BUTTON_W;
        int fullscreenX = closeX - BUTTON_SPACING - BUTTON_W;
        int minimizeX = closeX - 2 * (BUTTON_SPACING + BUTTON_W);

        blitWithSlide(graphics, BUTTON, closeX, BUTTON_PADDING_TOP, 0, lerpColor(RED_DARK, RED_LIGHT, easeInOutCubic(fade[0])));
        blitWithSlide(graphics, BUTTON, fullscreenX, BUTTON_PADDING_TOP, 1, lerpColor(GREEN_DARK, GREEN_LIGHT, easeInOutCubic(fade[1])));
        blitWithSlide(graphics, BUTTON, minimizeX, BUTTON_PADDING_TOP, 2, lerpColor(YELLOW_DARK, YELLOW_LIGHT, easeInOutCubic(fade[2])));
    }


    private static void blitWithSlide(GuiGraphicsExtractor graphics, Identifier texture, int x, int baseY, int slideIndex, int color) {
        float offsetY = getSlideOffset(slideIndex);

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(0f, offsetY);

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, baseY, 0, 0, BUTTON_W, BUTTON_H, TEXTURE_W, TEXTURE_H, color);

        pose.popMatrix();
    }

    private static float getSlideOffset(int btn) {
        float eased = easeInOutCubic(slideY[btn]);
        return -(1f - eased) * SLIDE_DISTANCE;
    }

    public static void handleMouse(long handle, int screenW) {
        int state = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT);

        if (state == GLFW.GLFW_PRESS && prevMouseState == GLFW.GLFW_RELEASE) {
            switch (hoveredButton) {
                case 0 -> GLFW.glfwSetWindowShouldClose(handle, true);
                case 1 -> Minecraft.getInstance().getWindow().toggleFullScreen();
                case 2 -> GLFW.glfwIconifyWindow(handle);
            }
        }

        prevMouseState = state;
    }

    private static int lerpColor(int dark, int light, float t) {
        int ar = (dark >> 16) & 0xFF, ag = (dark >> 8) & 0xFF, ab = dark & 0xFF;
        int br = (light >> 16) & 0xFF, bg = (light >> 8) & 0xFF, bb = light & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int b = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    static float easeInOutCubic(float t) {
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }
}