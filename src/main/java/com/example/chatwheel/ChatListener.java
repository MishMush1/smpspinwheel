package com.example.chatwheel;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener {

    private static final Pattern PAY_PATTERN =
            Pattern.compile("(\\w+) paid you \\$(\\d+(?:[KMB])?)\\.");

    public static final List<String> wheelList = new ArrayList<>();
    public static long totalAmount = 0;
    public static long targetAmount = 100000; // default target (100k). Edit as needed.

    public static void register() {
        ClientReceiveMessageCallback.EVENT.register((message, overlay) -> {
            String raw = message.getString();
            Matcher m = PAY_PATTERN.matcher(raw);

            if (m.find()) {
                String payer = m.group(1);
                String amountStr = m.group(2);

                long amount = parseAmount(amountStr);
                totalAmount += amount;
                wheelList.add(payer);

                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(
                            Text.literal("Added " + payer + " to wheel! Total: " + totalAmount), false);
                }

                if (totalAmount >= targetAmount) {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(
                                Text.literal("Target reached! Spinning wheel..."), false);
                    }
                    WheelScreen.open();
                    totalAmount = 0;
                }
            }
            return true;
        });
    }

    private static long parseAmount(String s) {
        if (s.endsWith("K")) return Long.parseLong(s.replace("K", "")) * 1000L;
        if (s.endsWith("M")) return Long.parseLong(s.replace("M", "")) * 1_000_000L;
        if (s.endsWith("B")) return Long.parseLong(s.replace("B", "")) * 1_000_000_000L;
        return Long.parseLong(s);
    }
}