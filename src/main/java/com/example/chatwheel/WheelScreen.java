package com.example.chatwheel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Random;

public class WheelScreen extends Screen {

    private float angle = 0f;
    private float spinSpeed = 720f; // degrees per second initial
    private boolean spinning = true;
    private long endTime;

    protected WheelScreen() {
        super(Text.literal("Wheel Spin"));
    }

    public static void open() {
        MinecraftClient.getInstance().setScreen(new WheelScreen());
    }

    @Override
    protected void init() {
        // Spin duration (ms)
        endTime = System.currentTimeMillis() + 3000;
        // Add a close button
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> close())
                .dimensions(width / 2 - 40, height - 40, 80, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);

        int cx = width / 2;
        int cy = height / 2 - 20;
        int radius = 100;

        List<String> entries = ChatListener.wheelList;
        if (entries.isEmpty()) {
            ctx.drawText(textRenderer, "No entries!", cx - 30, cy, 0xFFFFFF, false);
            super.render(ctx, mouseX, mouseY, delta);
            return;
        }

        float slice = 360f / entries.size();

        // Draw labels for each slice (simple visualization)
        for (int i = 0; i < entries.size(); i++) {
            float start = angle + i * slice;
            drawLabel(ctx, cx, cy, radius, start + slice / 2, entries.get(i));
        }

        // Spin logic: angle increases by spinSpeed * delta (delta in seconds)
        if (spinning) {
            angle += spinSpeed * delta;
            // exponential slow down
            spinSpeed *= 0.995f;

            if (System.currentTimeMillis() >= endTime) {
                spinning = false;
                int winnerIndex = pickWinner(entries.size(), angle);
                String winner = entries.get(winnerIndex);
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("Winner: " + winner), false);
                }
                ChatListener.wheelList.clear();
            }
        }

        // Draw pointer
        ctx.drawText(textRenderer, "▼", cx - 4, cy - radius - 20, 0xFFFF00, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private int pickWinner(int size, float finalAngle) {
        float slice = 360f / size;
        // Normalize so 0 degrees points at index 0 slice boundary; pointer at top -> angle 270
        float normalized = (finalAngle + 270) % 360;
        if (normalized < 0) normalized += 360;
        int idx = (int)(normalized / slice);
        // convert to index where slice 0 is at angle 0
        return idx % size;
    }

    private void drawLabel(DrawContext ctx, int cx, int cy, int r, float angleDeg, String label) {
        double theta = Math.toRadians(angleDeg);
        int tx = (int)(cx + Math.cos(theta) * (r + 30));
        int ty = (int)(cy + Math.sin(theta) * (r + 30));
        ctx.drawText(textRenderer, label, tx - textRenderer.getWidth(label) / 2, ty - 4, 0xFFFFFF, false);
    }
}