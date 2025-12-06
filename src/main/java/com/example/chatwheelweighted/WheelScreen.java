
package com.example.chatwheelweighted;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

public class WheelScreen extends Screen {
    private float angle = 0f;
    private float spinSpeed = 720f;
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
        endTime = System.currentTimeMillis() + 3000;
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> close())
                .dimensions(width/2-40, height-40, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Set Target"), btn -> openTargetInput())
                .dimensions(width/2-60, height-70, 120, 20).build());
    }

    private void openTargetInput() {
        // Simple input: set via chat for demonstration
        MinecraftClient.getInstance().player.sendMessage(Text.literal("Type new target in chat: /settarget <number>"), false);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        int cx = width/2;
        int cy = height/2 - 20;
        int radius = 100;

        List<String> players = new ArrayList<>(ChatListener.playerAmounts.keySet());
        if(players.isEmpty()) {
            ctx.drawText(textRenderer, "No entries!", cx-30, cy, 0xFFFFFF, false);
            super.render(ctx, mouseX, mouseY, delta);
            return;
        }

        // Calculate weights (min 1/10 of target)
        long minWeight = ChatListener.targetAmount / 10;
        List<Long> weights = players.stream()
            .map(p -> Math.max(ChatListener.playerAmounts.get(p), minWeight))
            .collect(Collectors.toList());

        long totalWeight = weights.stream().mapToLong(Long::longValue).sum();

        // Draw simple slices with player names and weight
        float startAngle = angle;
        for(int i=0;i<players.size();i++){
            String label = players.get(i) + " (" + weights.get(i) + ")";
            float sliceAngle = 360f * weights.get(i)/totalWeight;
            drawLabel(ctx, cx, cy, radius, startAngle+sliceAngle/2, label);
            startAngle += sliceAngle;
        }

        // Spin logic
        if(spinning){
            angle += spinSpeed*delta;
            spinSpeed *= 0.995f;
            if(System.currentTimeMillis()>=endTime){
                spinning=false;
                String winner = pickWeightedWinner(players, weights);
                if(MinecraftClient.getInstance().player!=null){
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("Winner: "+winner), false);
                }
            }
        }

        ctx.drawText(textRenderer, "▼", cx-4, cy-radius-20, 0xFFFF00, false);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private String pickWeightedWinner(List<String> players, List<Long> weights){
        long total = weights.stream().mapToLong(Long::longValue).sum();
        long r = new Random().nextLong(total);
        long sum = 0;
        for(int i=0;i<players.size();i++){
            sum += weights.get(i);
            if(r<sum) return players.get(i);
        }
        return players.get(0);
    }

    private void drawLabel(DrawContext ctx,int cx,int cy,int r,float angleDeg,String label){
        double theta = Math.toRadians(angleDeg);
        int tx = (int)(cx+Math.cos(theta)*(r+30));
        int ty = (int)(cy+Math.sin(theta)*(r+30));
        ctx.drawText(textRenderer,label,tx-textRenderer.getWidth(label)/2,ty-4,0xFFFFFF,false);
    }
}
