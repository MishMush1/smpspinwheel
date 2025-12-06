
package com.example.chatwheelweighted;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;

public class ChatListener {
    private static final Pattern PAY_PATTERN = Pattern.compile("(\\w+) paid you \\$(\\d+)([KMB])\\.");

    public static Map<String, Long> playerAmounts = new HashMap<>();
    public static long totalAmount = 0;
    public static long targetAmount = 100000; // default, can be edited in-game

    private static Path configPath = Paths.get(System.getProperty("user.home"), ".minecraft/config/chatwheel.json");

    public static void register() {
        loadConfig();
        ClientReceiveMessageCallback.EVENT.register((message, overlay) -> {
            String raw = message.getString();
            Matcher m = PAY_PATTERN.matcher(raw);
            if (m.find()) {
                String player = m.group(1);
                long amount = parseAmount(m.group(2), m.group(3));

                playerAmounts.put(player, playerAmounts.getOrDefault(player, 0L) + amount);
                totalAmount += amount;

                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("Added " + player + " (" + amount + ") to wheel. Total: " + totalAmount), false);
                }

                if (totalAmount >= targetAmount) {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Target reached! Spinning wheel..."), false);
                    }
                    WheelScreen.open();
                    totalAmount = 0;
                    playerAmounts.clear();
                }
            }
            return true;
        });
    }

    private static long parseAmount(String number, String suffix) {
        long val = Long.parseLong(number);
        switch(suffix) {
            case "K": val *= 1_000; break;
            case "M": val *= 1_000_000; break;
            case "B": val *= 1_000_000_000; break;
        }
        return val;
    }

    public static void loadConfig() {
        try {
            if (!Files.exists(configPath.getParent())) {
                Files.createDirectories(configPath.getParent());
            }
            if (!Files.exists(configPath)) {
                saveConfig();
            } else {
                String json = Files.readString(configPath, StandardCharsets.UTF_8);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                targetAmount = obj.get("targetAmount").getAsLong();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void saveConfig() {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("targetAmount", targetAmount);
            Files.writeString(configPath, obj.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
